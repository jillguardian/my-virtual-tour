package org.apache.commons.vfs2.provider.s3;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.FileNameParser;

import java.util.Collection;

public class AmazonS3FileProvider extends AbstractOriginatingFileProvider {

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
    protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions) throws FileSystemException {
        AmazonS3ClientWrapper amazonS3Service = new AmazonS3ClientWrapperImpl(fileSystemOptions);
        return new AmazonS3FileSystem((AmazonS3Filename) rootName, fileSystemOptions, amazonS3Service);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return AmazonS3FileProvider.CAPABILITIES;
    }

    @Override
    protected FileNameParser getFileNameParser() {
        return AmazonS3FilenameParser.getInstance();
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return AmazonS3FileSystemConfigBuilder.getInstance();
    }
}
