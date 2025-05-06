package pawa_be.bucket.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IBucketService {
    void uploadFile(String fileName, MultipartFile file);
    void removeFile(String fileKey);
    String getBase64File(String fileName);
}
