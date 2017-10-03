package org.apache.commons.vfs2.provider.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class AmazonS3ClientWrapperImpl implements AmazonS3ClientWrapper {

    private static final Logger logger = LoggerFactory.getLogger(AmazonS3ClientWrapperImpl.class);

    private AmazonS3Client amazonS3Client;

    public AmazonS3ClientWrapperImpl(FileSystemOptions fileSystemOptions) {
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard();
        configureAmazonS3Client(fileSystemOptions, amazonS3ClientBuilder);
        this.amazonS3Client = AmazonS3Client.class.cast(amazonS3ClientBuilder.build());
    }

    protected void configureAmazonS3Client(FileSystemOptions fileSystemOptions,
                                           AmazonS3ClientBuilder amazonS3ClientBuilder) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(
                AmazonS3FileSystemConfigBuilder.getInstance().getAccessKey(fileSystemOptions),
                AmazonS3FileSystemConfigBuilder.getInstance().getSecretKey(fileSystemOptions));
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxErrorRetry(
                AmazonS3FileSystemConfigBuilder.getInstance().getMaxRetries(fileSystemOptions));

        amazonS3ClientBuilder
                .withCredentials(awsCredentialsProvider)
                .withClientConfiguration(clientConfiguration);

        String endpoint = AmazonS3FileSystemConfigBuilder.getInstance().getEndpoint(fileSystemOptions);
        if (endpoint != null && !endpoint.isEmpty()) {
            AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                    new AwsClientBuilder.EndpointConfiguration(
                            AmazonS3FileSystemConfigBuilder.getInstance().getEndpoint(fileSystemOptions),
                            AmazonS3FileSystemConfigBuilder.getInstance().getRegion(fileSystemOptions));
            amazonS3ClientBuilder.withEndpointConfiguration(endpointConfiguration);
        } else {
            amazonS3ClientBuilder.withRegion(
                    AmazonS3FileSystemConfigBuilder.getInstance().getRegion(fileSystemOptions));
        }
    }

    @Override
    public void createBucket(String bucket) {
        amazonS3Client.createBucket(bucket);
    }

    @Override
    public boolean isBucketExisting(String bucket) {
        return amazonS3Client.doesBucketExist(bucket);
    }

    @Override
    public boolean isFile(String bucket, String key) {
        if (amazonS3Client.doesObjectExist(bucket, key)) {
            return !key.endsWith(FileName.SEPARATOR);
        }
        logger.debug("[" + bucket + key + "] does not exist");
        return false;
    }

    @Override
    public boolean isFolder(String bucket, String key) {
        if (amazonS3Client.doesObjectExist(bucket, key)) {
            if (key.isEmpty()) {
                logger.trace("Root location of bucket [" + bucket + "] is a folder");
                return true;
            }
            ObjectMetadata objectMetadata = amazonS3Client.getObjectMetadata(bucket, key);
            return key.endsWith(FileName.SEPARATOR) && objectMetadata.getContentLength() == 0;
        }
        logger.debug("[" + bucket + key + "] does not exist");
        return false;
    }

    @Override
    public long getSize(String bucket, String key) {
        if (isFile(bucket, key)) {
            ObjectMetadata objectMetadata = amazonS3Client.getObjectMetadata(bucket, key);
            return objectMetadata.getContentLength();
        } else if (isFolder(bucket, key)) {
            ObjectListing objectListing = amazonS3Client.listObjects(bucket, key);
            Collection<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            while (objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
                summaries.addAll(objectListing.getObjectSummaries());
            }

            Collection<Long> sizes = summaries.stream()
                    .map(S3ObjectSummary::getSize)
                    .collect(Collectors.toList());
            return sizes.stream().reduce(0L, Long::sum);
        }
        return -1;
    }

    @Override
    public Date getLastModifiedDate(String bucket, String key) {
        if (isFile(bucket, key)) {
            ObjectMetadata objectMetadata = amazonS3Client.getObjectMetadata(bucket, key);
            return objectMetadata.getLastModified();
        } else if (isFolder(bucket, key)) {
            ObjectListing objectListing = amazonS3Client.listObjects(bucket, key);
            Collection<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            while (objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
                summaries.addAll(objectListing.getObjectSummaries());
            }

            Collection<Date> dates = summaries.stream()
                    .map(S3ObjectSummary::getLastModified)
                    .collect(Collectors.toList());
            Optional<Date> lastModified = dates.stream().max(Date::compareTo);
            if (lastModified.isPresent()) {
                return lastModified.get();
            }
        }
        return null;
    }

    @Override
    public boolean rename(String oldBucket, String oldKey, String newBucket, String newKey) {
        if (isFile(oldBucket, oldKey)) {
            amazonS3Client.copyObject(oldBucket, oldKey, newBucket, newKey);
            amazonS3Client.deleteObject(oldBucket, oldKey);
        } else if (isFolder(oldBucket, oldKey)) {
            ObjectListing objectListing = amazonS3Client.listObjects(oldBucket, oldKey);
            Collection<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            while (objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
                summaries.addAll(objectListing.getObjectSummaries());
            }

            for (S3ObjectSummary summary : summaries) {
                String original = summary.getKey();
                String resolved = newKey + original.substring(original.indexOf(newKey));
                amazonS3Client.copyObject(summary.getBucketName(), summary.getKey(), newBucket, resolved);
                amazonS3Client.deleteObject(summary.getBucketName(), summary.getKey());
            }
        }
        return false;
    }

    @Override
    public boolean createFolder(String bucket, String key) {
        key = key.endsWith(FileName.SEPARATOR) ? key : key + FileName.SEPARATOR;

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, metadata);
        amazonS3Client.putObject(putObjectRequest);
        return true;
    }

    @Override
    public Collection<String> listChildren(String bucket, String key) {
        if (isFolder(bucket, key)) {
            ObjectListing objectListing = amazonS3Client.listObjects(bucket, key);
            Collection<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            while (objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
                summaries.addAll(objectListing.getObjectSummaries());
            }

            return summaries.stream()
                    .map(summary -> summary.getBucketName() + summary.getKey())
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public InputStream getInputStream(String bucket, String key) {
        if (isFile(bucket, key)) {
            S3Object object = amazonS3Client.getObject(bucket, key);
            return object.getObjectContent();
        }
        return null;
    }

    @Override
    public OutputStream getOutputStream(String bucket, String key) {
        S3Object object = new S3Object();
        object.setBucketName(bucket);
        object.setKey(key);
        return new AmazonS3OutputStream(amazonS3Client, object);
    }

    @Override
    public boolean delete(String bucket, String key) {
        if (isFile(bucket, key)) {
            amazonS3Client.deleteObject(bucket, key);
        } else if (isFolder(bucket, key)) {
            ObjectListing objectListing = amazonS3Client.listObjects(bucket, key);
            Collection<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            while (objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
                summaries.addAll(objectListing.getObjectSummaries());
            }

            for (S3ObjectSummary summary : summaries) {
                amazonS3Client.deleteObject(summary.getBucketName(), summary.getKey());
            }
        }
        return true;
    }

    @Override
    public URL getStreamingUrl(String bucket, String key) {
        return amazonS3Client.getUrl(bucket, key);
    }

    private static class AmazonS3OutputStream extends OutputStream {

        private AmazonS3 service;
        private S3Object object;
        private OutputStream outputStream;

        private AmazonS3OutputStream(AmazonS3 service, S3Object object) {
            this.service = service;
            this.object = object;
            this.outputStream = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            outputStream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
        }

        @Override
        public void close() throws IOException {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    ByteArrayOutputStream.class.cast(outputStream).toByteArray());
            PutObjectRequest request = new PutObjectRequest(
                    object.getBucketName(),
                    object.getKey(),
                    inputStream,
                    object.getObjectMetadata())
                    .withCannedAcl(CannedAccessControlList.PublicRead); //TODO: Externalize.
            service.putObject(request);
            inputStream.close();
            outputStream.close();
        }
    }

}
