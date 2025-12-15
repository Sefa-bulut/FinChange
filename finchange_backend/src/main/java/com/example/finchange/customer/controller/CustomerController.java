package com.example.finchange.customer.controller;

import com.example.finchange.customer.dto.CustomerCreateRequest;
import com.example.finchange.customer.dto.CustomerDetailDto;
import com.example.finchange.customer.dto.CustomerListDto;
import com.example.finchange.customer.dto.EligibleCustomerDto;
import com.example.finchange.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.common.model.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.finchange.customer.dto.CustomerUpdateRequest;


import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<?> createClient(
            @RequestParam("clientData") String clientDataString,
            @RequestParam(value = "vergiLevhasi", required = false) MultipartFile vergiLevhasi,
            @RequestParam(value = "kvkkBelgesi", required = false) MultipartFile kvkkBelgesi,
            @RequestParam(value = "portfoyYonetimSozlesmesi", required = false) MultipartFile portfoyYonetimSozlesmesi,
            @RequestParam(value = "elektronikBildirimIzni", required = false) MultipartFile elektronikBildirimIzni) {
        
        System.out.println("========== CustomerController.createClient() method BAŞLADI! ==========");
        System.out.println("clientDataString: " + clientDataString);
        System.out.println("vergiLevhasi: " + (vergiLevhasi != null ? vergiLevhasi.getOriginalFilename() : "null"));
        System.out.println("kvkkBelgesi: " + (kvkkBelgesi != null ? kvkkBelgesi.getOriginalFilename() : "null"));
        System.out.println("portfoyYonetimSozlesmesi: " + (portfoyYonetimSozlesmesi != null ? portfoyYonetimSozlesmesi.getOriginalFilename() : "null"));
        System.out.println("elektronikBildirimIzni: " + (elektronikBildirimIzni != null ? elektronikBildirimIzni.getOriginalFilename() : "null"));
        
        try {
            CustomerCreateRequest request = objectMapper.readValue(clientDataString, CustomerCreateRequest.class);
            Integer clientId = customerService.createClient(request, vergiLevhasi, kvkkBelgesi, portfoyYonetimSozlesmesi, elektronikBildirimIzni);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(SuccessResponse.success(clientId, "Müşteri başarıyla oluşturuldu."));
        } catch (Exception e) {
            System.out.println("ERROR parsing JSON: " + e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .header(ErrorResponse.Header.BAD_REQUEST_ERROR.getName())
                    .message("JSON parse hatası: " + e.getMessage())
                    .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
    }

    @GetMapping({"", "/"})
    @PreAuthorize("hasAuthority('client:read:all')")
    public ResponseEntity<SuccessResponse<List<CustomerListDto>>> getAllClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String clientCode,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String status
    ) {
        List<CustomerListDto> clients = customerService.getAllClients(name, email, clientCode, customerType, status);
        return ResponseEntity.ok(SuccessResponse.success(clients));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('client:read:all')")
    public ResponseEntity<SuccessResponse<CustomerDetailDto>>  getClientById(@PathVariable Integer id) {
        CustomerDetailDto client = customerService.getClientById(id);
        return ResponseEntity.ok(SuccessResponse.success(client));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('client:update:status')") // Güncelleme yetkisi kontrolü
    public ResponseEntity<SuccessResponse<CustomerDetailDto>> updateClient(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        
        System.out.println("========== CustomerController.updateClient() method BAŞLADI! Client ID: " + id + " ==========");
        
        CustomerDetailDto updatedClient = customerService.updateClient(id, request);
        
        return ResponseEntity.ok(SuccessResponse.success(updatedClient, "Müşteri başarıyla güncellendi."));
    }

    @GetMapping("/eligible-for-bulk-order")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<List<EligibleCustomerDto>> getEligibleClients() {
        List<EligibleCustomerDto> clients = customerService.getEligibleClients();
        return SuccessResponse.success(clients);
    }

}