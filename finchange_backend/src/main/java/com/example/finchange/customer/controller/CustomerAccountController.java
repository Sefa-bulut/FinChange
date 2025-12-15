package com.example.finchange.customer.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.customer.dto.BalanceTransactionRequest;
import com.example.finchange.customer.dto.CreateCustomerAccountRequest;
import com.example.finchange.customer.dto.CustomerAccountResponse;
import com.example.finchange.customer.service.CustomerAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class CustomerAccountController {

    private final CustomerAccountService customerAccountService;

    @GetMapping("/code/{customerCode}/accounts")
    @PreAuthorize("hasAuthority('client:read:all')")
    public ResponseEntity<SuccessResponse<List<CustomerAccountResponse>>> getAccountsByCustomerCodeAndStatus(
            @PathVariable String customerCode,
            @RequestParam(required = false) Boolean active) {
        List<CustomerAccountResponse> accounts = customerAccountService.getAccountsByCustomerCodeAndStatus(customerCode, active);
        return ResponseEntity.ok(SuccessResponse.success(accounts));
    }


    // Yeni müşteri hesabı oluştur
    @PostMapping("/{clientId}/accounts")
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<SuccessResponse<CustomerAccountResponse>> createAccount(
            @PathVariable Integer clientId,
            @RequestBody @Valid CreateCustomerAccountRequest request) {
        CustomerAccountResponse response = customerAccountService.createAccount(clientId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success(response, "Müşteri hesabı başarıyla oluşturuldu."));
    }

    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAuthority('client:read:all')")
    public ResponseEntity<SuccessResponse<CustomerAccountResponse>> getAccountById(
            @PathVariable Integer accountId) {
        CustomerAccountResponse response = customerAccountService.getAccountById(accountId);
        return ResponseEntity
                .ok(SuccessResponse.success(response));
    }

    @PostMapping("/accounts/{accountId}/deposit")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<CustomerAccountResponse>> depositToAccount(
            @PathVariable Integer accountId,
            @RequestBody @Valid BalanceTransactionRequest request) {
        CustomerAccountResponse response = customerAccountService.depositToAccount(accountId, request.getAmount());
        return ResponseEntity
                .ok(SuccessResponse.success(response, "Para yatırma işlemi başarılı."));
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<CustomerAccountResponse>> withdrawFromAccount(
            @PathVariable Integer accountId,
            @RequestBody @Valid BalanceTransactionRequest request) {
        CustomerAccountResponse response = customerAccountService.withdrawFromAccount(accountId, request.getAmount());
        return ResponseEntity
                .ok(SuccessResponse.success(response, "Para çekme işlemi başarılı."));
    }

    @PatchMapping("/accounts/{accountId}/status")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<CustomerAccountResponse>> changeAccountStatus(
            @PathVariable Integer accountId,
            @RequestParam boolean active) {
        CustomerAccountResponse response = customerAccountService.changeAccountStatus(accountId, active);
        String statusText = active ? "aktif" : "pasif";
        return ResponseEntity.ok(SuccessResponse.success(response, "Hesap " + statusText + " duruma getirildi."));
    }

}
