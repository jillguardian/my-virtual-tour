package org.apache.commons.vfs2.provider.dropbox;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class DropboxFileSystem extends AbstractFileSystem {

    private DropboxClientWrapper dropboxClientWrapper;

    protected DropboxFileSystem(final FileName rootName,
                                final FileSystemOptions fileSystemOptions,
                                final DropboxClientWrapper dropboxClientWrapper) {
        super(rootName, null, fileSystemOptions);
        this.dropboxClientWrapper = dropboxClientWrapper;
    }

    @Override
    protected void addCapabilities(Collection<Capability> collection) {
        collection.addAll(DropboxFileProvider.CAPABILITIES);
    }

    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception {
        return new DropboxFileObject(name, this);
    }

    public DropboxClientWrapper getDropboxClientWrapper() {
        return dropboxClientWrapper;
    }

}
