package com.example.finchange.portfoliogroup.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AddMembersRequest {
    @NotEmpty(message = "Eklenecek müşteri listesi boş olamaz.")
    private List<Integer> customerIds;
}