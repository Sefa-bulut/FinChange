package com.example.finchange.customer.model.mapper;

import com.example.finchange.customer.dto.CustomerDetailDto;
import com.example.finchange.customer.model.Customers;
import org.apache.kafka.common.errors.ResourceNotFoundException;

public class CustomerMapper {

    public static Customers mapToEntity(CustomerDetailDto dto) {
        if (dto == null) {
            throw new ResourceNotFoundException("Müşteri bilgisi bulunamadı. DTO null geldi.");
        }

        return Customers.builder()
                .id(dto.getId())
                .customerCode(dto.getMusteriKodu())
                // kaydiAcanKullaniciId dto’da yok, dışarıdan setlemelisin
                .customerType(dto.getCustomerType())
                .status(dto.getDurum())
                .phone(dto.getTelefon())
                .email(dto.getEmail())
                .residenceAddress(dto.getAdres())
                .name(dto.getAd())
                .lastName(dto.getSoyad())
                .idNumber(dto.getTckn())
                .dateOfBirth(dto.getDogumTarihi())
                // uyruk bilgisi dto’da yok, varsa ekle
                .CompanyTitle(dto.getSirketUnvani())
                .taxIDNumber(dto.getVergiNo())
                .mersisNo(dto.getMersisNo())
                .centerAddress(null) // dto’da yok, gerekirse ekle
                .authorizedPersonNameSurname(dto.getYetkiliKisiAdSoyad())
                .build();
    }

}
