package com.example.finchange.customer.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.customer.dto.ConsentRequestDto;
import com.example.finchange.customer.service.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clients/{clientId}/consents")
@RequiredArgsConstructor
public class ConsentController {
    private final ConsentService consentService;

    @PostMapping
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<SuccessResponse<Integer>> recordConsent(
            @PathVariable int clientId,
            @RequestBody ConsentRequestDto request) {

        
        int consentId = consentService.recordConsent(clientId, request.getConsentType());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success(consentId, "Onay başarıyla kaydedildi."));
    }
}
