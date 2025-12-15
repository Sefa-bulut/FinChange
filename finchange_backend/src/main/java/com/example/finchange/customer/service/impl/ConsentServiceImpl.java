package com.example.finchange.customer.service.impl;

import com.example.finchange.customer.model.CustomerConsents;
import com.example.finchange.customer.repository.CustomerConsentsRepository;
import com.example.finchange.customer.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsentServiceImpl implements ConsentService {

    private final CustomerConsentsRepository customerConsentsRepository;

    @Override
    public int recordConsent(int clientId, String consentType) {
        Integer grantedByUserId = getCurrentUserId();

        CustomerConsents customerConsents = new CustomerConsents();
        customerConsents.setClientId(clientId);
        customerConsents.setConsentType(consentType);
        customerConsents.setGrantedByUserId(grantedByUserId);

        CustomerConsents savedCustomerConsents = customerConsentsRepository.save(customerConsents);
        return savedCustomerConsents.getId();
    }

    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            return Integer.parseInt(jwt.getClaimAsString("userId"));
        }
        // Üretim ortamında daha spesifik bir exception fırlatmak daha iyi olabilir.
        throw new IllegalStateException("Kullanıcı kimliği JWT'den alınamadı.");
    }
}