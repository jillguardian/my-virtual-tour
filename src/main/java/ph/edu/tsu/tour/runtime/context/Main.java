package ph.edu.tsu.tour.runtime.context;

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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.core.access.AccessManagementService;
import ph.edu.tsu.tour.core.access.AccessManagementServiceImpl;
import ph.edu.tsu.tour.core.access.PrivilegeRepository;
import ph.edu.tsu.tour.core.access.RoleRepository;
import ph.edu.tsu.tour.core.access.UserRepository;
import ph.edu.tsu.tour.core.image.DiskStorageCapableImageService;
import ph.edu.tsu.tour.core.image.DiskStorageCapableImageServiceImpl;
import ph.edu.tsu.tour.core.image.ImageRepository;
import ph.edu.tsu.tour.core.image.ImageService;
import ph.edu.tsu.tour.core.image.ImageServiceImpl;
import ph.edu.tsu.tour.core.image.ToPublicImageService;
import ph.edu.tsu.tour.core.poi.PointOfInterestRepository;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.core.poi.PointOfInterestServiceImpl;
import ph.edu.tsu.tour.core.poi.PublishingPointOfInterestService;
import ph.edu.tsu.tour.core.poi.ToPublicPointOfInterestService;
import ph.edu.tsu.tour.core.storage.StorageService;
import ph.edu.tsu.tour.core.storage.VfsStorageService;

import javax.persistence.EntityManager;
import java.net.URI;
import java.util.Collection;
import java.util.Observer;

@Configuration
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Bean
    public ImageService imageService(EntityManager entityManager, ImageRepository imageRepository) {
        return new ImageServiceImpl(entityManager, imageRepository);
    }

    @Bean
    public PointOfInterestService pointOfInterestService(EntityManager entityManager,
                                                         PointOfInterestRepository pointOfInterestRepository) {
        return new PointOfInterestServiceImpl(entityManager, pointOfInterestRepository);
    }

    @Bean
    public PublishingPointOfInterestService publishingPointOfInterestService(
            PointOfInterestService pointOfInterestService) {
        return new PublishingPointOfInterestService(pointOfInterestService);
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

    @Bean
    public ToPublicImageService toPublicImageUriService(FileSystemManager fileSystemManager,
                                                        FileSystemOptions fileSystemOptions) {
        return new ToPublicImageService(fileSystemManager, fileSystemOptions);
    }

    @Bean
    public ToPublicPointOfInterestService toPublicPointOfInterestService(ToPublicImageService toPublicImageService) {
        return new ToPublicPointOfInterestService(toPublicImageService);
    }

}
