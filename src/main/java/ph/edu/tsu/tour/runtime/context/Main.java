package ph.edu.tsu.tour.runtime.context;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.dropbox.DropboxFileProvider;
import org.apache.commons.vfs2.provider.dropbox.DropboxFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.s3.AmazonS3FileProvider;
import org.apache.commons.vfs2.provider.s3.AmazonS3FileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import ph.edu.tsu.tour.Project;
import ph.edu.tsu.tour.core.access.AccessManagementService;
import ph.edu.tsu.tour.core.access.AccessManagementServiceImpl;
import ph.edu.tsu.tour.core.access.AdministratorRepository;
import ph.edu.tsu.tour.core.access.PrivilegeRepository;
import ph.edu.tsu.tour.core.access.RoleRepository;
import ph.edu.tsu.tour.core.image.DiskStorageCapableImageService;
import ph.edu.tsu.tour.core.image.DiskStorageCapableImageServiceImpl;
import ph.edu.tsu.tour.core.image.ImageRepository;
import ph.edu.tsu.tour.core.image.ImageService;
import ph.edu.tsu.tour.core.image.ImageServiceImpl;
import ph.edu.tsu.tour.core.image.ToPublicImageServiceImpl;
import ph.edu.tsu.tour.core.map.DecoratedMapboxDomainMapService;
import ph.edu.tsu.tour.core.map.DefaultDomainMapService;
import ph.edu.tsu.tour.core.map.DomainMapService;
import ph.edu.tsu.tour.core.map.MapboxMapService;
import ph.edu.tsu.tour.core.map.MapService;
import ph.edu.tsu.tour.core.poi.PointOfInterestRepository;
import ph.edu.tsu.tour.core.poi.PointOfInterestService;
import ph.edu.tsu.tour.core.poi.PointOfInterestServiceImpl;
import ph.edu.tsu.tour.core.poi.PublishingPointOfInterestService;
import ph.edu.tsu.tour.core.poi.ToPublicPointOfInterestService;
import ph.edu.tsu.tour.core.storage.StorageService;
import ph.edu.tsu.tour.core.storage.StreamingStorageService;
import ph.edu.tsu.tour.core.storage.VfsStorageService;

import javax.persistence.EntityManager;
import java.net.URI;

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
                                                           AdministratorRepository administratorRepository,
                                                           PasswordEncoder passwordEncoder) {
        return new AccessManagementServiceImpl(privilegeRepository, roleRepository,
                                               administratorRepository, passwordEncoder);
    }

    @Bean
    public FileSystemManager fileSystemManager(StorageProperties storageProperties,
                                               FileSystemOptions fileSystemOptions) throws FileSystemException {
        StandardFileSystemManager fileSystemManager = new StandardFileSystemManager();
        fileSystemManager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fileSystemManager.setFilesCache(new SoftRefFilesCache());

        fileSystemManager.addProvider("dropbox", new DropboxFileProvider());
        fileSystemManager.addProvider("s3", new AmazonS3FileProvider());
        fileSystemManager.init();

        if (storageProperties.getDefaultDirectory() != null) {
            logger.info("Setting default directory to [" + storageProperties.getDefaultDirectory() + "]...");
            fileSystemManager.setBaseFile(fileSystemManager.resolveFile(
                    storageProperties.getDefaultDirectory().toASCIIString(), fileSystemOptions));
        }
        return fileSystemManager;
    }

    @Bean
    public FileSystemOptions fileSystemOptions(StorageProperties storageProperties) {
        FileSystemOptions fileSystemOptions = new FileSystemOptions();

        DropboxFileSystemConfigBuilder dropboxConfig = DropboxFileSystemConfigBuilder.getInstance();
        dropboxConfig.setClientIdentifier(fileSystemOptions, Project.getName() + "/" + Project.getVersion());
        dropboxConfig.setAccessToken(fileSystemOptions, storageProperties.getDropboxProperties().getAccessToken());

        AmazonS3FileSystemConfigBuilder amazonS3Config = AmazonS3FileSystemConfigBuilder.getInstance();
        amazonS3Config.setSecretKey(fileSystemOptions, storageProperties.getAmazonS3Properties().getSecretKey());
        amazonS3Config.setAccessKey(fileSystemOptions, storageProperties.getAmazonS3Properties().getAccessKey());
        amazonS3Config.setRegion(fileSystemOptions, storageProperties.getAmazonS3Properties().getRegion());
        amazonS3Config.setEndpoint(fileSystemOptions, storageProperties.getAmazonS3Properties().getEndpoint());
        if (storageProperties.getAmazonS3Properties().getMaxRetries() != null) {
            amazonS3Config.setMaxRetries(fileSystemOptions, storageProperties.getAmazonS3Properties().getMaxRetries());
        }

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
    public ToPublicImageServiceImpl toPublicImageUriService(StreamingStorageService<URI, URI> streamingStorageService) {
        return new ToPublicImageServiceImpl(streamingStorageService);
    }

    @Bean
    public ToPublicPointOfInterestService toPublicPointOfInterestService(ToPublicImageServiceImpl toPublicImageService) {
        return new ToPublicPointOfInterestService(toPublicImageService);
    }

    @Bean
    public MapService mapService(@Value("${application.map.mapbox.access-token}") String accessToken) {
        return new MapboxMapService(accessToken);
    }

    @Primary
    @Bean
    public DomainMapService domainMapService(MapService mapService) {
        return new DefaultDomainMapService(mapService);
    }

    @Bean
    public DecoratedMapboxDomainMapService decoratedMapboxDomainMapService(
            @Value("${application.map.mapbox.access-token}") String accessToken) {
        return new DecoratedMapboxDomainMapService(accessToken);
    }

}
