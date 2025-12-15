package com.example.finchange.customer.service;

import com.example.finchange.customer.dto.CreateCustomerAccountRequest;
import com.example.finchange.customer.dto.CustomerAccountResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CustomerAccountService {

    // Müşteri hesap listesini getirir.
    List<CustomerAccountResponse> getAccountsByCustomerCode(String customerCode);

    // Yeni müşteri hesabı oluşturur.
    CustomerAccountResponse createAccount(Integer clientId,CreateCustomerAccountRequest request);

    // Hesaba bakiye yükleme işlemi yapar.
    CustomerAccountResponse depositToAccount(Integer accountId, BigDecimal amount);

    // Hesaptan bakiye çekme işlemi yapar.
    CustomerAccountResponse withdrawFromAccount(Integer accountId, BigDecimal amount);

    CustomerAccountResponse getAccountById(Integer accountId);

    CustomerAccountResponse changeAccountStatus(Integer accountId, boolean isActive);

    List<CustomerAccountResponse> getAccountsByCustomerCodeAndStatus(String customerCode, Boolean active);



}
