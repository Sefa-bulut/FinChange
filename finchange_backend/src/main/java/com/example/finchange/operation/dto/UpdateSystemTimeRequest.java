package com.example.finchange.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateSystemTimeRequest {

    @NotNull(message = "New system time cannot be null.")
    private LocalDateTime systemTime;

    private String description; // isteğe bağlı alan

}