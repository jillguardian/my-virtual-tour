package org.apache.commons.vfs2.provider.s3;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * Use this class to build configuration settings for a {@link AmazonS3FileSystem}.
 */
public class AmazonS3FileSystemConfigBuilder extends FileSystemConfigBuilder {

    /**
     * Default instance.
     */
    private static final AmazonS3FileSystemConfigBuilder BUILDER = new AmazonS3FileSystemConfigBuilder();

    private static final String KEY_ACCESS_KEY = AmazonS3FileSystemConfigBuilder.class.getName() + ".ACCESS_KEY";
    private static final String KEY_SECRET_KEY = AmazonS3FileSystemConfigBuilder.class.getName() + ".SECRET_KEY";
    private static final String KEY_REGION = AmazonS3FileSystemConfigBuilder.class.getName() + ".REGION";
    private static final String KEY_ENDPOINT = AmazonS3FileSystemConfigBuilder.class.getName() + ".ENDPOINT";
    private static final String KEY_MAX_RETRIES =
            AmazonS3FileSystemConfigBuilder.class.getName() + ".DEFAULT_MAX_RETRIES";

    private static final int DEFAULT_MAX_RETRIES = 3;

    private AmazonS3FileSystemConfigBuilder() {
        super("s3.");
    }

    /**
     * Create a new config builder with the given specified prefix.
     *
     * @param prefix prefix string to use for parameters of this config builder
     */
    protected AmazonS3FileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    public static AmazonS3FileSystemConfigBuilder getInstance() {
        return AmazonS3FileSystemConfigBuilder.BUILDER;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return AmazonS3FileSystem.class;
    }

    public void setAccessKey(final FileSystemOptions fileSystemOptions, String accessKey) {
        this.setParam(fileSystemOptions, KEY_ACCESS_KEY, accessKey);
    }

    public String getAccessKey(final FileSystemOptions fileSystemOptions) {
        return this.getString(fileSystemOptions, KEY_ACCESS_KEY);
    }

    public void setSecretKey(final FileSystemOptions fileSystemOptions, String secretKey) {
        this.setParam(fileSystemOptions, KEY_SECRET_KEY, secretKey);
    }

    public String getSecretKey(final FileSystemOptions fileSystemOptions) {
        return this.getString(fileSystemOptions, KEY_SECRET_KEY);
    }

    public void setRegion(final FileSystemOptions fileSystemOptions, String region) {
        this.setParam(fileSystemOptions, KEY_REGION, region);
    }

    public String getRegion(final FileSystemOptions fileSystemOptions) {
        return this.getString(fileSystemOptions, KEY_REGION);
    }

    public void setEndpoint(final FileSystemOptions fileSystemOptions, String endpoint) {
        this.setParam(fileSystemOptions, KEY_ENDPOINT, endpoint);
    }

    public String getEndpoint(final FileSystemOptions fileSystemOptions) {
        return this.getString(fileSystemOptions, KEY_ENDPOINT);
    }

    public void setMaxRetries(final FileSystemOptions fileSystemOptions, int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Must be a positive number");
        }
        this.setParam(fileSystemOptions, KEY_MAX_RETRIES, maxRetries );
    }

    public int getMaxRetries(final FileSystemOptions fileSystemOptions) {
        return getInteger(fileSystemOptions, KEY_MAX_RETRIES, DEFAULT_MAX_RETRIES);
    }

}
