package com.example.finchange.customer.service.impl;

import com.example.finchange.customer.model.CustomerDocuments;
import com.example.finchange.customer.repository.CustomerDocumentsRepository;
import com.example.finchange.customer.service.DocumentService;
import com.example.finchange.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final CustomerDocumentsRepository customerDocumentsRepository;
    private final StorageService storageService;

    @Override
    public int uploadDocument(Integer clientId, MultipartFile file, String documentType) throws Exception {
        Integer uploaderUserId = getCurrentUserId();

        String basePath = "musteri-belgeleri/" + clientId;
        String filePath = storageService.uploadFile(file, basePath);

        CustomerDocuments customerDocuments = new CustomerDocuments();
        customerDocuments.setClientId(clientId);
        customerDocuments.setUploaderUserId(uploaderUserId);
        customerDocuments.setDocumentType(documentType);
        customerDocuments.setFilePath(filePath);

        CustomerDocuments savedCustomerDocuments = customerDocumentsRepository.save(customerDocuments);
        return savedCustomerDocuments.getId();
    }

    /**
     * Güvenlik bağlamından (Security Context) mevcut kullanıcının kimliğini alır.
     * @return JWT'den alınan kullanıcı ID'si
     * @throws IllegalStateException Kullanıcı kimliği JWT'den alınamazsa
     */
    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            return Integer.parseInt(jwt.getClaimAsString("userId"));
        }
        throw new IllegalStateException("Kullanıcı kimliği JWT'den alınamadı.");
    }
}