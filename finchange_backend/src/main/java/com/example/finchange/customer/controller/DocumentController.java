package com.example.finchange.customer.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.customer.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clients/{clientId}/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;


    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<SuccessResponse<Integer>> uploadDocument(
            @PathVariable int clientId,
            @RequestParam MultipartFile file,
            @RequestParam String documentType) throws Exception {


        int documentId = documentService.uploadDocument(clientId, file, documentType);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success(documentId, "Belge başarıyla yüklendi."));
    }
}
