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
import org.geojson.GeoJsonObject;
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
import ph.edu.tsu.tour.core.location.Church;
import ph.edu.tsu.tour.core.location.ChurchRepository;
import ph.edu.tsu.tour.core.location.LocationService;
import ph.edu.tsu.tour.core.location.LocationServiceImpl;
import ph.edu.tsu.tour.core.location.PublishingLocationService;
import ph.edu.tsu.tour.core.location.ToPublicLocationService;
import ph.edu.tsu.tour.core.route.ChurchRouteService;
import ph.edu.tsu.tour.core.route.RouteService;
import ph.edu.tsu.tour.core.storage.StorageService;
import ph.edu.tsu.tour.core.storage.StreamingStorageService;
import ph.edu.tsu.tour.core.storage.VfsStorageService;
import ph.edu.tsu.tour.core.tour.TourRepository;
import ph.edu.tsu.tour.core.tour.TourService;
import ph.edu.tsu.tour.core.tour.TourServiceImpl;
import ph.edu.tsu.tour.core.user.NewPasswordTokenRepository;
import ph.edu.tsu.tour.core.user.NewPasswordTokenService;
import ph.edu.tsu.tour.core.user.NewPasswordTokenServiceImpl;
import ph.edu.tsu.tour.core.user.PublishingNewPasswordTokenService;
import ph.edu.tsu.tour.core.user.PublishingUserService;
import ph.edu.tsu.tour.core.user.PublishingVerificationTokenService;
import ph.edu.tsu.tour.core.user.UserRepository;
import ph.edu.tsu.tour.core.user.UserService;
import ph.edu.tsu.tour.core.user.UserServiceImpl;
import ph.edu.tsu.tour.core.user.VerificationTokenRepository;
import ph.edu.tsu.tour.core.user.VerificationTokenService;
import ph.edu.tsu.tour.core.user.VerificationTokenServiceImpl;

import javax.persistence.EntityManager;
import java.net.URI;

// TODO: Remove vanilla service beans and solely expose publishing service beans instead.
@Configuration
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Bean
    public ImageService imageService(EntityManager entityManager, ImageRepository imageRepository) {
        return new ImageServiceImpl(entityManager, imageRepository);
    }

    @Bean
    public PublishingLocationService churchLocationService(EntityManager entityManager,
                                                           ChurchRepository churchRepository) {
        LocationService<Church> locationService = new LocationServiceImpl<>(entityManager, churchRepository);
        return new PublishingLocationService<>(locationService);
    }

    @Bean
    public TourService tourService(EntityManager entityManager, TourRepository tourRepository) {
        return new TourServiceImpl(entityManager, tourRepository);
    }

    @Bean
    public AccessManagementService accessManagementService(PrivilegeRepository privilegeRepository,
                                                           RoleRepository roleRepository,
                                                           AdministratorRepository administratorRepository,
                                                           PasswordEncoder passwordEncoder) {
        return new AccessManagementServiceImpl(privilegeRepository,
                                               roleRepository,
                                               administratorRepository,
                                               passwordEncoder);
    }

    @Bean
    public PublishingUserService userService(EntityManager entityManager,
                                             UserRepository userRepository,
                                             PasswordEncoder passwordEncoder) {
        UserService userService =  new UserServiceImpl(entityManager, userRepository, passwordEncoder);
        return new PublishingUserService(userService);
    }

    @Bean
    public PublishingVerificationTokenService verificationTokenService(
            EntityManager entityManager,
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository) {
        VerificationTokenService verificationTokenService =
                new VerificationTokenServiceImpl(entityManager, userRepository, verificationTokenRepository);
        return new PublishingVerificationTokenService(verificationTokenService);
    }

    @Bean
    public PublishingNewPasswordTokenService newPasswordTokenService(
            EntityManager entityManager, NewPasswordTokenRepository newPasswordTokenRepository) {
        NewPasswordTokenService newPasswordTokenService =
                new NewPasswordTokenServiceImpl(entityManager, newPasswordTokenRepository);
        return new PublishingNewPasswordTokenService(newPasswordTokenService);
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
    public ToPublicLocationService toPublicLocationService(ToPublicImageServiceImpl toPublicImageService) {
        return new ToPublicLocationService(toPublicImageService);
    }

    @Bean
    public RouteService<GeoJsonObject, Church> churchRouteService(
            @Value("${mapbox.access-token}") String accessToken) {
        return new ChurchRouteService(accessToken);
    }

}
