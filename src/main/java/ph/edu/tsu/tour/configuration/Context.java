package ph.edu.tsu.tour.configuration;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.dropbox.DropboxFileProvider;
import org.apache.commons.vfs2.provider.dropbox.DropboxFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.repository.ImageRepository;
import ph.edu.tsu.tour.repository.PointOfInterestRepository;
import ph.edu.tsu.tour.repository.PrivilegeRepository;
import ph.edu.tsu.tour.repository.RoleRepository;
import ph.edu.tsu.tour.repository.UserRepository;
import ph.edu.tsu.tour.service.AccessManagementService;
import ph.edu.tsu.tour.service.DiskStorageCapableImageService;
import ph.edu.tsu.tour.service.ImageService;
import ph.edu.tsu.tour.service.PointOfInterestService;
import ph.edu.tsu.tour.service.StorageService;
import ph.edu.tsu.tour.service.impl.AccessManagementServiceImpl;
import ph.edu.tsu.tour.service.impl.DiskStorageCapableImageServiceImpl;
import ph.edu.tsu.tour.service.impl.ImageServiceImpl;
import ph.edu.tsu.tour.service.impl.PointOfInterestServiceImpl;
import ph.edu.tsu.tour.service.impl.ToPublicImageUriService;
import ph.edu.tsu.tour.service.impl.VfsStorageService;

import java.net.URI;

@Configuration
public class Context {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    @Bean
    public ImageService imageService(ImageRepository imageRepository) {
        return new ImageServiceImpl(imageRepository);
    }

    @Bean
    public PointOfInterestService pointOfInterestService(PointOfInterestRepository pointOfInterestRepository) {
        return new PointOfInterestServiceImpl(pointOfInterestRepository);
    }

    @Bean
    public AccessManagementService accessManagementService(PrivilegeRepository privilegeRepository,
                                                           RoleRepository roleRepository,
                                                           UserRepository userRepository,
                                                           PasswordEncoder passwordEncoder) {
        return new AccessManagementServiceImpl(privilegeRepository, roleRepository, userRepository, passwordEncoder);
    }

    @Bean
    public FileSystemManager fileSystemManager(VfsProperties vfsProperties,
                                               FileSystemOptions fileSystemOptions) throws FileSystemException {
        StandardFileSystemManager fileSystemManager = new StandardFileSystemManager();
        fileSystemManager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fileSystemManager.setFilesCache(new WeakRefFilesCache());

        fileSystemManager.addProvider("dropbox", new DropboxFileProvider());
        fileSystemManager.init();

        if (vfsProperties.getBaseUri() != null) {
            fileSystemManager.setBaseFile(fileSystemManager.resolveFile(
                    vfsProperties.getBaseUri().toASCIIString(), fileSystemOptions));
        }
        return fileSystemManager;
    }

    @Bean
    public FileSystemOptions fileSystemOptions(VfsProperties vfsProperties) {
        FileSystemOptions fileSystemOptions = new FileSystemOptions();

        DropboxFileSystemConfigBuilder builder = DropboxFileSystemConfigBuilder.getInstance();
        builder.setClientIdentifier(fileSystemOptions, Project.getName() + "/" + Project.getVersion());
        builder.setAccessToken(fileSystemOptions, vfsProperties.getDropboxVfsProperties().getAccessToken());

        return fileSystemOptions;
    }

    @Bean
    public VfsStorageService vfsStorageService(FileSystemManager fileSystemManager,
                                               FileSystemOptions fileSystemOptions) {
        return new VfsStorageService(fileSystemManager, fileSystemOptions);
    }

    @Bean
    public ToPublicImageUriService toPublicImageUriService(FileSystemManager fileSystemManager,
                                                           FileSystemOptions fileSystemOptions) {
        return new ToPublicImageUriService(fileSystemManager, fileSystemOptions);
    }

    @Primary
    @Bean
    public DiskStorageCapableImageService diskStorageCapableImageService(ImageService imageService,
                                                                         StorageService<URI> storageService,
                                                                         ImageProperties imageProperties) {
        return new DiskStorageCapableImageServiceImpl(
                imageService,
                storageService,
                DiskStorageCapableImageServiceImpl.ImageSettings.builder()
                        .autoResize(imageProperties.getMain().isAutoResize())
                        .maxWidth(imageProperties.getMain().getMaxWidth())
                        .maxHeight(imageProperties.getMain().getMaxHeight())
                        .quality(imageProperties.getMain().getQuality())
                        .build(),
                DiskStorageCapableImageServiceImpl.ImageSettings.builder()
                        .autoResize(imageProperties.getPreview().isAutoResize())
                        .maxWidth(imageProperties.getPreview().getMaxWidth())
                        .maxHeight(imageProperties.getPreview().getMaxHeight())
                        .quality(imageProperties.getPreview().getQuality())
                        .build());
    }

}
