package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private final AmazonS3Client s3Client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        s3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, new ObjectMetadata());
    }

    @Override
    public Optional<Blob> get(String name) throws IOException, URISyntaxException {
        GetObjectRequest objectRequest = new GetObjectRequest(photoStorageBucket, name);

        if (!s3Client.doesObjectExist(photoStorageBucket, name)) {
            return Optional.empty();
        }
        S3ObjectInputStream objectContent = s3Client.getObject(objectRequest).getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectContent);
        Blob blob = new Blob(name, new ByteArrayInputStream(bytes), new Tika().detect(objectContent));

        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {

    }
}
