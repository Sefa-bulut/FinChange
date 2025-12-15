package com.example.finchange.report.dto;

import com.example.finchange.customer.model.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSummaryDTO {
    private String customerName;
    private CustomerType customerType;
    private String customerCode;
}
