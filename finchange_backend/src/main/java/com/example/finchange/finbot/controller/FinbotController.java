package com.example.finchange.finbot.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.finbot.service.FinbotService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/finbot")
@Validated
public class FinbotController {

    private final FinbotService finbotService;

    public FinbotController(FinbotService finbotService) {
        this.finbotService = finbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> ask(@Valid @RequestBody AskRequest request) {
        Map<String, Object> result = finbotService.ask(request.getQuestion(), request.getLanguage());
        return ResponseEntity.ok(SuccessResponse.success(result));
    }

    @GetMapping("/turkish-news")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getTurkishNews() {
        Map<String, Object> result = finbotService.getTurkishNews();
        return ResponseEntity.ok(SuccessResponse.success(result));
    }

    @Data
    public static class AskRequest {
        @NotBlank
        private String question;
        @NotBlank
        private String language;
    }
}
