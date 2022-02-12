package org.themullers.library.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Object Store Access Object (like a DAO, but for S3-compatible object stores).
 * The library only accesses one bucket of objects, and this API reflects this expectation.
 */
@Component
public class LibraryOSAO implements InitializingBean {

    protected AmazonS3 s3;
    protected String accessKeyId;
    protected String secretAccessKey;
    protected String bucketName;
    protected String bucketEndpoint;
    protected String bucketRegion;

    /**
     * Required setup before the other methods of this bean can be used.
     * TODO: Why not just make this the constructor?
     */
    public void init() {

        // pass the AWS key id and secret key into a "credentials provider"
        var credentials = new BasicAWSCredentials(getAccessKeyId(), getSecretAccessKey());
        var credentialsProvider = new AWSStaticCredentialsProvider(credentials);

        // configure up an endpoint
        var endpoint = new AwsClientBuilder.EndpointConfiguration(getBucketEndpoint(), getBucketRegion());

        // build the client object
        s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withEndpointConfiguration(endpoint).build();
    }

    /**
     * List the keys for all the objects in this bucket.
     * @return a list of keys
     */
    public List<String> listObjects() {
        var objects = new LinkedList<String>();

        // get a list of objects
        var batch = s3.listObjects(getBucketName());

        // while there are more objects
        boolean hasMoreObjects = true;
        while (hasMoreObjects) {

            // add each object's key to the list
            for (var summary : batch.getObjectSummaries()) {
                objects.add(summary.getKey());
            }

            // if there are more objects, fetch them
            hasMoreObjects = batch.isTruncated();
            if (hasMoreObjects) {
                batch = s3.listNextBatchOfObjects(batch);
            }
        }

        // return the list of keys
        return objects;
    }

    /**
     * Delete an object from the store.
     * @param objectKey  the key of the object to delete.
     */
    public void deleteObject(String objectKey) {
        s3.deleteObject(getBucketName(), objectKey);
    }

    /**
     * Get an object from the store.
     * TODO: implement this in a way that doesn't expose the Amazon API?
     * @param objectKey  the key of the object to fetch
     * @return  a structure representing the object in the store (can be used to download the object)
     */
    public S3Object readObject(String objectKey) {
        return s3.getObject(getBucketName(), objectKey);
    }

    /**
     * Upload an object to the store.
     * @param is  a stream from which we can read the binary content of the object to be stored
     * @param contentLength  the size of the object
     * @param objectKey  the key to use to fetch this object back from the store
     */
    public void uploadObject(InputStream is, long contentLength, String objectKey) {

        // check to make sure we're not stomping an existing object
        if (s3.doesObjectExist(getBucketName(), objectKey)) {
            throw new ObjectStoreException("object already exists in store with key " + objectKey);
        }

        // set the content length
        var metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);

        // upload the object
        s3.putObject(getBucketName(), objectKey, is, metadata);
    }

    /**
     * Upload an file to the store.
     * @param file  the file to upload to the store
     */
    public void uploadObject(File file) {

        // get the content length
        var contentLength = file.length();

        // upload the object
        try (var is = new FileInputStream(file)) {
            uploadObject(is, contentLength, file.getName());
        }
        catch (IOException e) {
            throw new ObjectStoreException(e);
        }
    }

    // SPRING INITIALIZATION

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    // ACCESSOR METHODS

    public String getAccessKeyId() {
        return accessKeyId;
    }

    @Value("${object.store.access.key.id}")
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    @Value("${object.store.secret.access.key}")
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Value("${object.store.bucket.name}")
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketEndpoint() {
        return bucketEndpoint;
    }

    @Value("${object.store.bucket.endpoint}")
    public void setBucketEndpoint(String bucketEndpoint) {
        this.bucketEndpoint = bucketEndpoint;
    }

    public String getBucketRegion() {
        return bucketRegion;
    }

    @Value("${object.store.bucket.region}")
    public void setBucketRegion(String bucketRegion) {
        this.bucketRegion = bucketRegion;
    }
}
