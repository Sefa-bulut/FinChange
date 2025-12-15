package com.example.finchange.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(MultipartFile file, String basePath) throws Exception;
}