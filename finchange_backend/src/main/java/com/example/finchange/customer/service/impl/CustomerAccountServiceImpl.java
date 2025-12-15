package com.example.finchange.customer.service.impl;


import com.example.finchange.customer.dto.CreateCustomerAccountRequest;
import com.example.finchange.customer.dto.CustomerAccountResponse;
import com.example.finchange.customer.dto.CustomerDetailDto;
import com.example.finchange.customer.exception.*;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.model.mapper.CustomerAccountMapper;
import com.example.finchange.customer.model.mapper.CustomerMapper;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.customer.service.CustomerAccountService;
import com.example.finchange.customer.service.CustomerService;
import com.example.finchange.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAccountServiceImpl implements CustomerAccountService {


    private final CustomerAccountRepository accountRepository;
    private final CustomerService customerService;
    private final PortfolioService portfolioService;

    @Override
    public List<CustomerAccountResponse> getAccountsByCustomerCode(String customerCode) {
        customerCode = customerCode.trim();

        Customers customer = customerService.getCustomerByCustomerCode(customerCode);

        System.out.println("Müşteri bulundu: " + customer.getId() + " - " + customer.getCustomerCode());

        List<CustomerAccount> accounts = accountRepository.findByCustomer_Id(customer.getId());

        System.out.println("Hesap sayısı: " + accounts.size());

        if (accounts.isEmpty()) {
            CustomerAccountResponse emptyAccountResponse = new CustomerAccountResponse();
            emptyAccountResponse.setCustomerId(customer.getId());
            return List.of(emptyAccountResponse);
        }

        return accounts.stream()
                .map(CustomerAccountMapper::mapToResponse)
                .toList();
    }



    @Override
    public List<CustomerAccountResponse> getAccountsByCustomerCodeAndStatus(String customerCode, Boolean active) {
        customerCode = customerCode.trim();

        Customers customer = customerService.getCustomerByCustomerCode(customerCode);
        System.out.println("Müşteri bulundu: " + customer.getId() + " - " + customer.getCustomerCode());

        List<CustomerAccount> accounts;
        if (active == null) {
            accounts = accountRepository.findByCustomer_Id(customer.getId());
        } else {
            accounts = accountRepository.findByCustomer_IdAndActive(customer.getId(), active);
        }

        System.out.println("Hesap sayısı: " + accounts.size());

        if (accounts.isEmpty()) {
            CustomerAccountResponse emptyAccountResponse = new CustomerAccountResponse();
            emptyAccountResponse.setCustomerId(customer.getId());
            return List.of(emptyAccountResponse);
        }

        return accounts.stream()
                .map(CustomerAccountMapper::mapToResponse)
                .toList();
    }




    @Override
    public CustomerAccountResponse createAccount(Integer clientId, CreateCustomerAccountRequest request) {
        CustomerDetailDto customerDetail = customerService.getClientById(clientId);
        if (customerDetail == null) {
            throw new CustomerNotFoundException("Müşteri bulunamadı.");
        }

        String accountNumber;
        do {
            accountNumber = String.format("%08d", (int) (Math.random() * 100_000_000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        boolean accountNameExists = accountRepository.existsByCustomer_IdAndAccountName(clientId, request.getAccountName());
        if (accountNameExists) {
            throw new AccountNameAlreadyExistsException("Aynı müşteri için bu hesap adı zaten kullanılıyor.");
        }

        CustomerAccount newAccount = CustomerAccountMapper.mapToEntity(clientId,request, accountNumber);
        Customers customerEntity = CustomerMapper.mapToEntity(customerDetail);
        newAccount.setCustomer(customerEntity);

        CustomerAccount savedAccount = accountRepository.save(newAccount);

        return CustomerAccountMapper.mapToResponse(savedAccount);
    }

    @Override
    @Transactional
    public CustomerAccountResponse depositToAccount(Integer accountId, BigDecimal amount) {
        CustomerAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı: " + accountId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("İşlem tutarı pozitif olmalıdır.");
        }

        if (!account.isActive()) {
            throw new IllegalStateException("Pasif hesaba işlem yapılamaz. Hesap ID: " + accountId);
        }

        portfolioService.deposit(accountId, amount, "Manual deposit via API");

        CustomerAccount reloaded = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı: " + accountId));
        return CustomerAccountMapper.mapToResponse(reloaded);
    }

    @Override
    @Transactional
    public CustomerAccountResponse withdrawFromAccount(Integer accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException("Çekilecek tutar pozitif bir değer olmalıdır.");
        }

        CustomerAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountId));

        if (!account.isActive()) {
            throw new IllegalStateException("Pasif hesaptan para çekilemez. Hesap ID: " + accountId);
        }

        portfolioService.withdraw(accountId, amount, "Manual withdraw via API");

        CustomerAccount reloaded = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountId));
        return CustomerAccountMapper.mapToResponse(reloaded);
    }

    @Override
    public CustomerAccountResponse getAccountById(Integer accountId) {
        CustomerAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountId));
        return CustomerAccountMapper.mapToResponse(account);
    }




    @Override
    @Transactional
    public CustomerAccountResponse changeAccountStatus(Integer accountId, boolean isActive) {
        CustomerAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountId));

        if (!isActive && account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountHasBalanceException("Bakiyesi olan hesap pasif yapılamaz. Hesap ID: " + accountId);
        }

        account.setActive(isActive);
        CustomerAccount updatedAccount = accountRepository.save(account);

        return CustomerAccountMapper.mapToResponse(updatedAccount);
    }


}
