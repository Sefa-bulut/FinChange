package com.example.finchange.portfoliogroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "Grup adı boş olamaz.")
    @Size(min = 3, max = 100, message = "Grup adı 3 ile 100 karakter arasında olmalıdır.")
    private String groupName;
}