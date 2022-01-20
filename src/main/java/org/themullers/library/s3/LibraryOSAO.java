package org.themullers.library.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Object Store Access Object (like a DAO, but for S3-compatible object stores)
 */
@Component
public class LibraryOSAO implements InitializingBean {

    protected AmazonS3 s3;
    protected String accessKeyId;
    protected String secretAccessKey;
    protected String bucketName;
    protected String bucketEndpoint;
    protected String bucketRegion;

    public void init() {
        // pass the AWS key id and secret key into a "credentials provider"
        var credentials = new BasicAWSCredentials(getAccessKeyId(), getSecretAccessKey());
        var credentialsProvider = new AWSStaticCredentialsProvider(credentials);

        // configure up an endpoint
        var endpoint = new AwsClientBuilder.EndpointConfiguration(getBucketEndpoint(), getBucketRegion());

        // build the client object
        s3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withEndpointConfiguration(endpoint).build();
    }

    public List<String> listObjects() {
        var objects = new LinkedList<String>();
        for (var summary: s3.listObjects(getBucketName()).getObjectSummaries()) {
            objects.add(summary.getKey());
        }
        return objects;
    }

    public S3Object readObject(String objectKey) {
        return s3.getObject(getBucketName(), objectKey);
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
