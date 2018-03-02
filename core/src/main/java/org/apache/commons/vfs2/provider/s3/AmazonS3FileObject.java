package org.apache.commons.vfs2.provider.s3;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.PubliclyAccessibleFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class AmazonS3FileObject extends AbstractFileObject<AmazonS3FileSystem> implements PubliclyAccessibleFileObject {

    private static final Logger logger = LoggerFactory.getLogger(AmazonS3FileObject.class);

    protected AmazonS3FileObject(AbstractFileName name, AmazonS3FileSystem fs) {
        super(name, fs);
    }

    private String getBucketId() {
        return ((AmazonS3FileSystem) getFileSystem()).getBucketId();
    }

    private String getKey() {
        String key = getName().getPath();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        return key;
    }

    private AmazonS3ClientWrapper getAmazonS3Service() {
        AmazonS3FileSystem fileSystem = (AmazonS3FileSystem) getFileSystem();
        return fileSystem.getAmazonS3Service();
    }

    @Override
    protected FileType doGetType() throws Exception {
        boolean isFile = getAmazonS3Service().isFile(getBucketId(), getKey());
        boolean isFolder = getAmazonS3Service().isFolder(getBucketId(), getKey());
        if (isFile && isFolder) {
            return FileType.FILE_OR_FOLDER;
        } else if (isFolder) {
            return FileType.FOLDER;
        } else if (isFile) {
            return FileType.FILE;
        }
        return FileType.IMAGINARY;
    }

    @Override
    protected void doCreateFolder() throws Exception {
        getAmazonS3Service().createFolder(getBucketId(), getKey());
    }

    @Override
    protected long doGetContentSize() throws Exception {
        return getAmazonS3Service().getSize(getBucketId(), getKey());
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return getAmazonS3Service().getLastModifiedDate(getBucketId(), getKey()).getTime();
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        return getAmazonS3Service().getInputStream(getBucketId(), getKey());
    }

    @Override
    protected OutputStream doGetOutputStream(boolean boolAppend) throws Exception {
        if (boolAppend && !getFileSystem().hasCapability(Capability.APPEND_CONTENT)) {
            throw new UnsupportedOperationException("Only overwrite operation is supported");
        }
        return getAmazonS3Service().getOutputStream(getBucketId(), getKey());
    }

    @Override
    protected String[] doListChildren() throws Exception {
        Collection<String> children = getAmazonS3Service().listChildren(getBucketId(), getKey());
        return children != null ? children.toArray(new String[0]) : null;
    }

    @Override
    protected void doRename(FileObject newFile) throws Exception {
        getAmazonS3Service().rename(getBucketId(), getKey(), getBucketId(), newFile.getName().getPath());
    }

    @Override
    protected void doDelete() throws Exception {
        getAmazonS3Service().delete(getBucketId(), getKey());
    }

    @Override
    public URI getPubliclyAccessibleUri() {
        try {
            return getAmazonS3Service().getStreamingUrl(getBucketId(), getKey()).toURI();
        } catch (URISyntaxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Could not get public URI of [" + getBucketId() + "][" + getKey() + "]", e);
            }
            return null;
        }
    }
}
