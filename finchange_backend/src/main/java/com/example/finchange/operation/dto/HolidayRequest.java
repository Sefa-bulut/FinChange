package com.example.finchange.operation.dto;

import com.example.finchange.operation.model.enums.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {

    @NotNull(message = "Tatil tarihi boş olamaz")
    private LocalDate holidayDate; // Sadece tarih bilgisi, saat yok.

    @NotNull(message = "Tatil türü boş olamaz")
    private HolidayType type;

    @NotBlank(message = "Açıklama boş olamaz")
    @Size(max = 255, message = "Açıklama en fazla 255 karakter olabilir")
    private String description;
}
