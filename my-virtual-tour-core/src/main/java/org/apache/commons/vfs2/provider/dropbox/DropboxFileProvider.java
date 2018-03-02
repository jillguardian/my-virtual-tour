package org.apache.commons.vfs2.provider.dropbox;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Collection;

public class DropboxFileProvider extends AbstractOriginatingFileProvider {

    public static final Collection<Capability> CAPABILITIES = ImmutableSet.of(
            Capability.CREATE,
            Capability.DELETE,
            Capability.RENAME,
            Capability.GET_TYPE,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT,
            Capability.GET_LAST_MODIFIED,
            Capability.WRITE_CONTENT);

    @Override
    public Collection<Capability> getCapabilities() {
        return DropboxFileProvider.CAPABILITIES;
    }

    @Override
    protected FileSystem doCreateFileSystem(FileName fileName, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new DropboxFileSystem(fileName, fileSystemOptions, new DropboxClientWrapperImpl(fileSystemOptions));
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return DropboxFileSystemConfigBuilder.getInstance();
    }

}
