package org.apache.commons.vfs2.provider.dropbox;

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
import java.net.URL;

public class DropboxFileObject extends AbstractFileObject<DropboxFileSystem> implements PubliclyAccessibleFileObject {

    private static final Logger logger = LoggerFactory.getLogger(DropboxFileObject.class);
    private final DropboxFileSystem fileSystem;

    public DropboxFileObject(AbstractFileName name, DropboxFileSystem fileSystem) {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
    }

    /**
     * @return Dropbox file path
     * @see #getName()
     */
    private String getPath() {
        return getName().getPath();
    }

    /**
     * @return client responsible for handling actual operations
     */
    private DropboxClientWrapper getDropboxService() {
        return fileSystem.getDropboxClientWrapper();
    }

    @Override
    protected FileType doGetType() throws Exception {
        if (this.getDropboxService().isFile(getPath()) && this.getDropboxService().isFolder(getPath())) {
            return FileType.FILE_OR_FOLDER;
        } else if (this.getDropboxService().isFolder(getPath())) {
            return FileType.FOLDER;
        } else if (this.getDropboxService().isFile(getPath())) {
            return FileType.FILE;
        }
        return FileType.IMAGINARY;
    }

    @Override
    protected void doCreateFolder() throws Exception {
        this.getDropboxService().createFolder(getPath());
    }

    @Override
    protected long doGetContentSize() throws Exception {
        return this.getDropboxService().getSize(getPath());
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return this.getDropboxService().getLastModifiedDate(getPath()).getTime();
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        return this.getDropboxService().getInputStream(getPath());
    }

    /**
     * @throws UnsupportedOperationException if {@code boolAppend} is set to {@literal true} and the file system does
     *                                       not support appending contents
     */
    @Override
    protected OutputStream doGetOutputStream(boolean boolAppend) throws Exception {
        if (boolAppend && !getFileSystem().hasCapability(Capability.APPEND_CONTENT)) {
            throw new UnsupportedOperationException("Only overwrite operation is supported");
        }
        return this.getDropboxService().getOutputStream(getPath());
    }

    @Override
    protected String[] doListChildren() throws Exception {
        return this.getDropboxService().listChildren(getPath()).toArray(new String[]{});
    }

    @Override
    protected void doRename(FileObject newFile) throws Exception {
        this.getDropboxService().rename(getPath(), newFile.getName().getPath());
    }

    @Override
    protected void doDelete() throws Exception {
        this.getDropboxService().delete(getPath());
    }

    @Override
    public URI getPubliclyAccessibleUri() {
        URL url = null;
        try {
            url = this.getDropboxService().getDirectUrl(getPath());
            return url.toURI();
        } catch (URISyntaxException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to convert URL [" + url + "] to URI");
            }
            return null;
        }
    }
}