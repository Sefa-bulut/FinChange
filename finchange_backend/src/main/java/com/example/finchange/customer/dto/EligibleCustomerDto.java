package com.example.finchange.customer.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EligibleCustomerDto {
    private Integer id;
    private String musteriKodu;
    private String gorunenAd;
    private List<EligibleCustomerAccountDto> accounts;
}