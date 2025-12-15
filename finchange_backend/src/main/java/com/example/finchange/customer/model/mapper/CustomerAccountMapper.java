package com.example.finchange.customer.model.mapper;

import com.example.finchange.customer.dto.CreateCustomerAccountRequest;
import com.example.finchange.customer.dto.CustomerAccountResponse;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.model.Customers;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerAccountMapper {


    public static CustomerAccountResponse mapToResponse(CustomerAccount account) {
        return CustomerAccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomer().getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .isActive(account.isActive())
                .blockedBalance(account.getBlockedBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .lastModifiedBy(account.getLastModifiedBy())
                .build();
    }

    public static List<CustomerAccountResponse> mapToResponseList(List<CustomerAccount> accounts) {
        return accounts.stream()
                .map(CustomerAccountMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    public static CustomerAccount mapToEntity(Integer clientId,CreateCustomerAccountRequest request, String accountNumber) {
        return CustomerAccount.builder()
                .customer(Customers.builder().id(clientId).build())
                .accountNumber(accountNumber)
                .accountName(request.getAccountName())
                .currency(request.getCurrency())
                .balance(request.getInitialBalance())
                .active(true)
                .blockedBalance(BigDecimal.ZERO)
                .version(0L)
                .build();
    }
}

