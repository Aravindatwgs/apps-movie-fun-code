package org.superbiz.moviefun.albums;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Optional;

public class FileStore implements  BlobStore{


    private AmazonS3Client s3Client;
    private String s3BucketName;



    public FileStore(AmazonS3Client s3Client, String s3BucketName) {

        this.s3Client = s3Client;
        this.s3BucketName = s3BucketName;

    }



    @Override
    public void put(Blob blob) throws IOException {

       // String eTag = blobStore.putBlob(containerName, blob, multipart());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);
        this.s3Client.putObject(this.s3BucketName,blob.name,blob.inputStream,metadata);


    }

    @Override
    public Optional<Blob> get(String name) throws IOException {

       // Blob blob = blobStore.blobBuilder(objectName).payload(Files.asByteSource(input))
          //      .contentType(MediaType.APPLICATION_OCTET_STREAM).contentDisposition(objectName).build();

        Blob blob = new Blob(name,s3Client.getObject(this.s3BucketName,name).getObjectContent(),s3Client.getObject(this.s3BucketName,name).getObjectMetadata().getContentType());

        System.out.println(blob.name);

        return Optional.of(blob);
    }
}
