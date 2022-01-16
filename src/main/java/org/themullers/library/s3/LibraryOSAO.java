package org.themullers.library.s3;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

/**
 * Object Store Access Object (like a DAO, but for S3-compatible object stores)
 */
@Component
public class LibraryOSAO {

    private final static String BUCKET_NAME = "michaeljmuller-media";
    private final static String BUCKET_ENDPOINT = "us-east-1.linodeobjects.com";
    private final static String BUCKET_REGION = "us-east-1";

    protected AmazonS3 s3;

    public LibraryOSAO() {
        var endpoint = new AwsClientBuilder.EndpointConfiguration(BUCKET_ENDPOINT, BUCKET_REGION);
        s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(endpoint).build();
    }

    public List<String> listObjects() {
        var objects = new LinkedList<String>();
        for (var summary: s3.listObjects(BUCKET_NAME).getObjectSummaries()) {
            objects.add(summary.getKey());
        }
        return objects;
    }

    public S3Object readObject(String objectKey) {
        return s3.getObject(BUCKET_NAME, objectKey);
    }

    public static void main(String args[]) throws Exception {
        var bucket = new LibraryOSAO();
        var objId = "Ed's Head - A. D. Kissam.epub";
        try (var obj = bucket.readObject(objId)) {
            var is = obj.getObjectContent();
            var outputFile = new File("/Users/mmuller/Downloads/eh.epub");
            Files.copy(is, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        /*
        for (var obj : bucket.listObjects()) {
            System.out.println(obj);
        }
         */
    }
}
