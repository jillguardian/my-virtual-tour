package org.apache.commons.vfs2.provider.dropbox;

import com.dropbox.core.DbxRequestConfig;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

import java.util.Locale;

/**
 * Use this class to build configuration settings for a {@link DropboxFileSystem}.
 */
public class DropboxFileSystemConfigBuilder extends FileSystemConfigBuilder {

    /**
     * Default instance.
     */
    private static final DropboxFileSystemConfigBuilder BUILDER = new DropboxFileSystemConfigBuilder();

    private static final String KEY_CLIENT_ID = DropboxFileSystemConfigBuilder.class.getName() + ".CLIENT_IDENTIFIER";

    private static final String KEY_ACCESS_TOKEN = DropboxFileSystemConfigBuilder.class.getName() + ".ACCESS_TOKEN";

    private static final String KEY_USER_LOCALE = DropboxFileSystemConfigBuilder.class.getName() + ".USER_LOCALE";

    private static final String KEY_AUTO_RETRY = DropboxFileSystemConfigBuilder.class.getName() + ".AUTO_RETRY";

    private static final String KEY_MAX_RETRIES = DropboxFileSystemConfigBuilder.class.getName() + ".MAX_RETRIES";

    private DropboxFileSystemConfigBuilder() {
        super("dropbox.");
    }

    /**
     * Create a new config builder with the given specified prefix.
     *
     * @param prefix prefix string to use for parameters of this config builder
     */
    protected DropboxFileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    public static DropboxFileSystemConfigBuilder getInstance() {
        return DropboxFileSystemConfigBuilder.BUILDER;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return DropboxFileSystem.class;
    }

    /**
     * Sets the client identifier for Dropbox requests. Identifiers are typically of the form
     * {@literal "Name/Version"} (e.g. {@literal "PhotoEditServer/1.3"}).
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @param clientIdentifier  HTTP user-agent identifier for the app
     */
    public void setClientIdentifier(final FileSystemOptions fileSystemOptions, String clientIdentifier) {
        setParam(fileSystemOptions, KEY_CLIENT_ID, clientIdentifier);
    }

    /**
     * Gets the assigned client identifier for Dropbox requests.
     *
     * @param fileSystemOptions file system options to work with
     * @return HTTP user-agent identifier for the app
     * @see DbxRequestConfig#getClientIdentifier()
     */
    public String getClientIdentifier(final FileSystemOptions fileSystemOptions) {
        return getString(fileSystemOptions, KEY_CLIENT_ID);
    }

    /**
     * Sets the access token which will be used in Dropbox requests.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @param accessToken       OAuth 2 access token that gives your app the ability to make Dropbox API calls
     */
    public void setAccessToken(final FileSystemOptions fileSystemOptions, String accessToken) {
        setParam(fileSystemOptions, KEY_ACCESS_TOKEN, accessToken);
    }

    /**
     * Gets the assigned access token for Dropbox requests.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @return OAuth 2 access token that gives your app the ability to make Dropbox API calls
     */
    public String getAccessToken(final FileSystemOptions fileSystemOptions) {
        return getString(fileSystemOptions, KEY_ACCESS_TOKEN);
    }

    /**
     * User-visible messages returned by the Dropbox servers will be localized to this locale.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @param locale            desired locale, or {@code null} to use the default setting
     */
    public void setUserlocale(final FileSystemOptions fileSystemOptions, Locale locale) {
        setParam(fileSystemOptions, KEY_USER_LOCALE, locale);
    }

    /**
     * Gets the assigned locale which shall be used by the Dropbox server to localize user-visible strings returned by
     * API calls.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @return assigned locale
     */
    public Locale getUserLocale(final FileSystemOptions fileSystemOptions) {
        return (Locale) getParam(fileSystemOptions, KEY_USER_LOCALE);
    }

    /**
     * Enable/disable automatic retry of Dropbox RPC and download requests in case they fail. By default, the number
     * of re-tries done is 3.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @param shouldAutoRetry   {@literal true} if auto-retry is to be enabled
     * @see #setMaxRetries(FileSystemOptions, int)
     */
    public void setAutoRetry(final FileSystemOptions fileSystemOptions, boolean shouldAutoRetry) {
        setParam(fileSystemOptions, KEY_AUTO_RETRY, shouldAutoRetry);
        setMaxRetries(fileSystemOptions, 3);
    }

    /**
     * Retrieves the automatic retry settings in the event that Dropbox RPC and download requests fail.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @return {@literal true} if auto-retry is set to enabled
     */
    public boolean getAutoRetry(final FileSystemOptions fileSystemOptions) {
        return getBoolean(fileSystemOptions, KEY_AUTO_RETRY, false);
    }

    /**
     * Set the maximum number of retries in the event of failed requests. For this to take effect, automatic retrial
     * (for failed requests) must be enabled.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @param maxRetries        maximum number of times to retry a retry-able failed request; must be positive
     * @see #setAutoRetry(FileSystemOptions, boolean)
     */
    public void setMaxRetries(final FileSystemOptions fileSystemOptions, int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Must be a positive number");
        }
        setParam(fileSystemOptions, KEY_MAX_RETRIES, maxRetries);
    }

    /**
     * Gets the assigned maximum number of retries.
     *
     * @param fileSystemOptions file system options to work with; stores the data
     * @return maximum number of retries
     */
    public int getMaxRetries(final FileSystemOptions fileSystemOptions) {
        return getInteger(fileSystemOptions, KEY_MAX_RETRIES, 0);
    }

}
