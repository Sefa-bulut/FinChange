package com.example.finchange.report.controller;


import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.report.dto.FullReportResponse;
import com.example.finchange.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/details")
    @PreAuthorize("hasAnyAuthority('report:view','report:view:all','client:read','client:read:all')")
    public SuccessResponse<FullReportResponse> getFullReport(
            @RequestParam String customerCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        FullReportResponse reportData = reportService.generateFullReport(customerCode, start, end);
        return SuccessResponse.success(reportData);
    }
}
