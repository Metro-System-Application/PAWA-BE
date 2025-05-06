package pawa_be.bucket.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;

@Service
public class BucketService implements IBucketService {

    @Value("${bucket.url}")
    private String endpoint;

    @Value("${bucket.access-key}")
    private String accessKey;

    @Value("${bucket.secret-key}")
    private String secretKey;

    @Value("${bucket.bucket-name}")
    private String bucketName;

    private S3Client s3Client;

    @PostConstruct
    private void init() {
        try {
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .region(Region.US_EAST_1)  // Dummy region for MinIO
                    .forcePathStyle(true)     // Required for MinIO
                    .build();

            if (s3Client.listBuckets().buckets().stream()
                    .noneMatch(b -> b.name().equals(bucketName))) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                System.out.println("MinIO bucket created: " + bucketName);
            }

            System.out.println("MinIO connection established successfully.");
        } catch (Exception e) {
            System.err.println("Failed to connect to MinIO: " + e.getMessage());
            throw new RuntimeException("Failed to initialize MinioBucketService", e);
        }
    }

    @Override
    public void uploadFile(String fileKey, MultipartFile file) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }

    @Override
    public String getBase64File(String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = s3Object.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from MinIO", e);
        }
    }

    @Override
    public void removeFile(String fileKey) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build()
        );
    }
}
