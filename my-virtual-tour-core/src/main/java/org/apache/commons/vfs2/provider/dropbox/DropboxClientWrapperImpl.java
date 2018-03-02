package org.apache.commons.vfs2.provider.dropbox;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxUploader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxClientV2Base;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper for a Dropbox client.
 */
public class DropboxClientWrapperImpl implements DropboxClientWrapper {

    private static Logger logger = LoggerFactory.getLogger(DropboxClientWrapperImpl.class);

    /**
     * Client (for administrator endpoints).
     */
    private DbxClientV2Base dropboxClient;

    /**
     * @param fileSystemOptions contains configuration settings
     * @see DropboxFileSystemConfigBuilder
     */
    public DropboxClientWrapperImpl(final FileSystemOptions fileSystemOptions) {
        this.dropboxClient = createDropboxClient(fileSystemOptions);
    }

    /**
     * Creates a Dropbox client (administrator-endpoint specific) based on given configuration {@code fileSystemOptions}.
     *
     * @return Dropbox client for administrator endpoints
     * @see DropboxFileSystemConfigBuilder
     */
    private DbxClientV2Base createDropboxClient(FileSystemOptions fileSystemOptions) {
        return createDropboxClient(createDropboxRequestConfig(fileSystemOptions), fileSystemOptions);
    }

    /**
     * Creates a Dropbox client (administrator-endpoint specific) based on the given configuration ({@code fileSystemOptions}).
     *
     * @param dbxRequestConfig  describes the default request attributes to be used in Dropbox-related requests
     * @param fileSystemOptions file system options to work with
     * @return Dropbox client for administrator endpoints
     * @see DropboxFileSystemConfigBuilder
     */
    protected DbxClientV2Base createDropboxClient(DbxRequestConfig dbxRequestConfig,
                                                  FileSystemOptions fileSystemOptions) {
        String accessToken = DropboxFileSystemConfigBuilder.getInstance().getAccessToken(fileSystemOptions);
        return new DbxClientV2(dbxRequestConfig, accessToken);
    }

    /**
     * Create a Dropbox request configuration based on given file system options.
     *
     * @param fileSystemOptions file system options to work with
     * @return request configuration
     * @see DropboxFileSystemConfigBuilder
     */
    protected DbxRequestConfig createDropboxRequestConfig(FileSystemOptions fileSystemOptions) {
        DropboxFileSystemConfigBuilder builder = DropboxFileSystemConfigBuilder.getInstance();

        DbxRequestConfig.Builder configBuilder =
                DbxRequestConfig.newBuilder(builder.getClientIdentifier(fileSystemOptions));
        if (builder.getAutoRetry(fileSystemOptions)) {
            configBuilder.withAutoRetryEnabled(builder.getMaxRetries(fileSystemOptions));
        }
        configBuilder.withUserLocaleFrom(builder.getUserLocale(fileSystemOptions));

        return configBuilder.build();
    }

    /**
     * @return {@literal true} if entry exists and is a file
     */
    @Override
    public boolean isFile(String path) {
        try {
            return dropboxClient.files().getMetadata(path) instanceof FileMetadata;
        } catch (GetMetadataErrorException e) {
            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Entry [" + path + "] does not exist", e);
                }
            }
        } catch (DbxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to get metadata for entry [" + path + "]", e);
            }
        }
        return false;
    }

    /**
     * @return {@literal true} if entry exists and is a folder
     */
    @Override
    public boolean isFolder(String path) {
        try {
            return dropboxClient.files().getMetadata(path) instanceof FolderMetadata;
        } catch (GetMetadataErrorException e) {
            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Entry [" + path + "] does not exist", e);
                }
            }
        } catch (DbxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to get metadata for entry [" + path + "]", e);
            }
        }
        return false;
    }

    @Override
    public long getSize(String path) { // TODO: Support folder size
        if (isFile(path)) {
            try {
                return ((FileMetadata) dropboxClient.files().getMetadata(path)).getSize();
            } catch (DbxException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to get metadata for entry [" + path + "]", e);
                }
            }
        }
        return -1;
    }

    @Override
    public Date getLastModifiedDate(String path) { // TODO: Support folder last modification date
        if (isFile(path)) {
            try {
                return ((FileMetadata) dropboxClient.files().getMetadata(path)).getServerModified();
            } catch (DbxException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to get metadata for entry [" + path + "]", e);
                }
            }
        }
        return null;
    }

    @Override
    public boolean rename(String oldPath, String newPath) {
        try {
            dropboxClient.files().move(oldPath, newPath);
            return true;
        } catch (DbxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to move entry [" + oldPath + "] to [" + newPath + "]", e);
            }
            return false;
        }
    }

    @Override
    public boolean createFolder(String path) {
        try {
            this.dropboxClient.files().createFolder(path);
            return true;
        } catch (DbxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to create folder", e);
            }
            return false;
        }
    }

    @Override
    public Collection<String> listChildren(String path) {
        if (isFolder(path)) {
            Set<String> children = new LinkedHashSet<>();
            try {
                ListFolderResult result;
                String cursor = null;
                do {
                    result = cursor == null
                            ? dropboxClient.files().listFolder(path)
                            : dropboxClient.files().listFolderContinue(cursor);
                    children.addAll(result.getEntries().stream().map(Metadata::getName)
                            .collect(Collectors.toSet()));
                    cursor = result.getCursor();
                } while (cursor != null);

                return Collections.unmodifiableSet(children);
            } catch (DbxException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to list contents of [" + path + "]", e);
                }
            }
        } else {
            logger.debug("Entry in path [" + path + "] does not represent a folder");
        }
        return Collections.emptySet();
    }

    @Override
    public InputStream getInputStream(String path) { // TODO: Support folder downloads
        if (isFile(path)) {
            DbxDownloader downloader = null;
            try {
                downloader = dropboxClient.files().download(path);
                return new DropboxFileInputStream(downloader, downloader.getInputStream());
            } catch (DbxException e) {
                logger.debug("Unable to download [" + path + "]", e);
            }
        }
        return null;
    }

    @Override
    public OutputStream getOutputStream(String path) {
        DbxUploader uploader = null;
        try {
            uploader = dropboxClient.files().upload(path);
            return new DropboxFileOutputStream(uploader, uploader.getOutputStream());
        } catch (DbxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to create a writable stream for [" + path + "]", e);
            }
        }
        return null;
    }

    @Override
    public boolean delete(String path) {
        try {
            dropboxClient.files().delete(path);
            return true;
        } catch (DbxException e) {
            return false;
        }
    }

    @Override
    public URL getDirectUrl(String path) {
        String publicUrl = null;
        try {
            Optional<SharedLinkMetadata> existingSharedLink = dropboxClient.sharing().listSharedLinksBuilder()
                    .withPath(path)
                    .withDirectOnly(true)
                    .start()
                    .getLinks().stream()
                    .filter(metadata -> metadata.getLinkPermissions().getRequestedVisibility() == RequestedVisibility.PUBLIC)
                    .findFirst();
            if (existingSharedLink.isPresent()) {
                publicUrl = existingSharedLink.get().getUrl();
            } else {
                SharedLinkMetadata sharedLink = dropboxClient.sharing()
                        .createSharedLinkWithSettings(path, SharedLinkSettings.newBuilder()
                                .withRequestedVisibility(RequestedVisibility.PUBLIC)
                                .build());
                publicUrl = sharedLink.getUrl();
            }

            return new URIBuilder(publicUrl)
                    .removeQuery()
                    .addParameter("dl", "1") // Direct link
                    .build()
                    .toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Could not parse URI [" + publicUrl + "]", e);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Could not get shared link for [" + path + "]", e);
            }
        }
        return null;
    }

    /**
     * Represents a Dropbox file's input stream.
     */
    private static class DropboxFileInputStream extends InputStream {

        private InputStream inputStream;

        private DbxDownloader downloader;

        private DropboxFileInputStream(DbxDownloader downloader, InputStream inputStream) {
            this.inputStream = inputStream;
            if (!(inputStream instanceof BufferedInputStream)) {
                this.inputStream = new BufferedInputStream(inputStream);
            }
            this.downloader = downloader;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int available() throws IOException {
            return inputStream.available();
        }

        @Override
        public synchronized void mark(int readlimit) {
            inputStream.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            inputStream.reset();
        }

        @Override
        public boolean markSupported() {
            return inputStream.markSupported();
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
            downloader.close(); // This class had to be specifically made to call this.
        }
    }

    /**
     * Represents a Dropbox file's output stream. Write data at this output stream and call the {@link #close()} method
     * to upload the stream to Dropbox.
     */
    private static class DropboxFileOutputStream extends OutputStream {

        private OutputStream outputStream;

        private DbxUploader uploader;

        private DropboxFileOutputStream(DbxUploader uploader, OutputStream outputStream) {
            this.uploader = uploader;
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
        }

        /**
         * Upload operation executes when this method is called.
         */
        @Override
        public void close() throws IOException {
            try {
                uploader.finish();
                uploader.close();
            } catch (DbxException e) {
                throw new IOException("Unable to properly execute upload request", e);
            }
        }
    }

}
