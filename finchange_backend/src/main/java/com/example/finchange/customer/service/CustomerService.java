package com.example.finchange.customer.service;

import com.example.finchange.customer.dto.CustomerCreateRequest;
import com.example.finchange.customer.dto.CustomerDetailDto;
import com.example.finchange.customer.dto.CustomerListDto;
import com.example.finchange.customer.dto.EligibleCustomerDto;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.model.CustomerAccount;
import org.springframework.web.multipart.MultipartFile;
import com.example.finchange.customer.dto.CustomerUpdateRequest;


import java.util.List;

public interface CustomerService {
    Integer createClient(CustomerCreateRequest request, MultipartFile vergiLevhasi, MultipartFile kvkkBelgesi,
                         MultipartFile portfoyYonetimSozlesmesi, MultipartFile elektronikBildirimIzni);

    List<CustomerListDto> getAllClients(String name, String email, String clientCode, String customerType, String status);

    CustomerDetailDto getClientById(Integer id);

    CustomerDetailDto updateClient(Integer id, CustomerUpdateRequest request);

    Customers getCustomerByCustomerCode(String customerCode);


    List<EligibleCustomerDto> getEligibleClients();

    // Yeni: Müşteri hesaplarını döndür
    List<CustomerAccount> getCustomerAccounts(Integer customerId);
}
