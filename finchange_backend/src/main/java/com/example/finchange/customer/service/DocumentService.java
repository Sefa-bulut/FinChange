package com.example.finchange.customer.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    int uploadDocument(Integer clientId, MultipartFile file, String documentType) throws Exception;
}