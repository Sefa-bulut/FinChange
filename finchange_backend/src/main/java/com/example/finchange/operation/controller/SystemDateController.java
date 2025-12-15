package com.example.finchange.operation.controller;


import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.operation.dto.UpdateSystemTimeRequest;
import com.example.finchange.operation.service.SystemDateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/system-date")
@RequiredArgsConstructor
public class SystemDateController {

    private final SystemDateService systemDateService;

    @GetMapping
    @PreAuthorize("hasAuthority('order:read:all')")
    public ResponseEntity<SuccessResponse<LocalDateTime>> getCurrentSystemTime() {
        LocalDateTime currentSystemTime = systemDateService.getSystemDateTime();
        return ResponseEntity
                .ok(SuccessResponse.success(currentSystemTime));
    }

    @GetMapping("/date")
    @PreAuthorize("hasAuthority('order:read:all')")
    public ResponseEntity<SuccessResponse<LocalDate>> getCurrentSystemDate() {
        LocalDate currentSystemDate = systemDateService.getSystemDate();
        return ResponseEntity
                .ok(SuccessResponse.success(currentSystemDate));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<Void>> updateSystemTime(@RequestBody @Valid UpdateSystemTimeRequest request) {
        systemDateService.updateSystemTime(request);
        return ResponseEntity
                .ok(SuccessResponse.success(null, "Sistem tarihi başarıyla güncellendi."));
    }
}