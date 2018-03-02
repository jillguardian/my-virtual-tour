package org.apache.commons.vfs2.provider.s3;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

public interface AmazonS3ClientWrapper {

    void createBucket(String bucket);

    boolean isBucketExisting(String bucket);

    boolean isFile(String bucket, String key);

    boolean isFolder(String bucket, String key);

    long getSize(String bucket, String key);

    Date getLastModifiedDate(String bucket, String key);

    boolean rename(String oldBucket, String oldKey, String newBucket, String newKey);

    boolean createFolder(String bucket, String key);

    Collection<String> listChildren(String bucket, String key);

    InputStream getInputStream(String bucket, String key);

    OutputStream getOutputStream(String bucket, String key);

    boolean delete(String bucket, String key);

    URL getStreamingUrl(String bucket, String key);

}
