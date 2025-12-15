package com.example.finchange.operation.controller;
import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.operation.dto.HolidayRequest;
import com.example.finchange.operation.dto.HolidayResponse;
import com.example.finchange.operation.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;



@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<HolidayResponse>> createHoliday(@RequestBody @Valid HolidayRequest request) {
        HolidayResponse response = holidayService.createHoliday(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success(response, "Tatil başarıyla oluşturuldu."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<HolidayResponse>> updateHoliday(@PathVariable Integer id,
                                                                          @RequestBody @Valid HolidayRequest request) {
        HolidayResponse response = holidayService.updateHoliday(id, request);
        return ResponseEntity
                .ok(SuccessResponse.success(response, "Tatil bilgisi başarıyla güncellendi."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<Void>> deleteHoliday(@PathVariable Integer id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity
                .ok(SuccessResponse.success(null, "Tatil başarıyla silindi."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:read:all')")
    public ResponseEntity<SuccessResponse<HolidayResponse>> getHolidayById(@PathVariable Integer id) {
        HolidayResponse response = holidayService.getHolidayById(id);
        return ResponseEntity
                .ok(SuccessResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('order:read:all')")
    public ResponseEntity<SuccessResponse<List<HolidayResponse>>> getAllHolidays() {
        List<HolidayResponse> responseList = holidayService.getAllHolidays();
        return ResponseEntity
                .ok(SuccessResponse.success(responseList));
    }

    @GetMapping("/by-date")
    @PreAuthorize("hasAuthority('order:read:all')")
    public ResponseEntity<SuccessResponse<List<HolidayResponse>>> getHolidaysByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<HolidayResponse> responseList = holidayService.getHolidaysByDate(date);
        return ResponseEntity
                .ok(SuccessResponse.success(responseList));
    }
}