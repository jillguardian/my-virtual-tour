package org.apache.commons.vfs2.provider.s3;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class AmazonS3FileSystem extends AbstractFileSystem {

    private AmazonS3ClientWrapper amazonS3ClientWrapper;
    private String bucketId;

    protected AmazonS3FileSystem(final AmazonS3Filename filename,
                                 final FileSystemOptions fileSystemOptions,
                                 final AmazonS3ClientWrapper amazonS3ClientWrapper) {
        super(filename, null, fileSystemOptions);
        this.amazonS3ClientWrapper = amazonS3ClientWrapper;
        this.bucketId = filename.getBucketId();
        if (!amazonS3ClientWrapper.isBucketExisting(bucketId)) {
            amazonS3ClientWrapper.createBucket(bucketId);
        }
    }

    protected String getBucketId() {
        return bucketId;
    }

    protected AmazonS3ClientWrapper getAmazonS3Service() {
        return amazonS3ClientWrapper;
    }

    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception {
        return new AmazonS3FileObject(name, this);
    }

    @Override
    protected void addCapabilities(Collection<Capability> capabilities) {
        capabilities.addAll(AmazonS3FileProvider.CAPABILITIES);
    }

}
