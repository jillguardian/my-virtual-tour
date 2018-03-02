package org.apache.commons.vfs2.provider.s3;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

public class AmazonS3Filename extends AbstractFileName {

    private String bucketId;

    protected AmazonS3Filename(String scheme, String bucketId, String key, FileType type) {
        super(scheme, key, type);
        this.bucketId = bucketId;
    }

    @Override
    public FileName createName(String key, FileType type) {
        return new AmazonS3Filename(getScheme(), bucketId, key, type);
    }

    @Override
    protected void appendRootUri(StringBuilder buffer, boolean addPassword) {
        buffer.append(getScheme());
        buffer.append("://");
        buffer.append(bucketId);
    }

    public String getBucketId() {
        return bucketId;
    }

}
