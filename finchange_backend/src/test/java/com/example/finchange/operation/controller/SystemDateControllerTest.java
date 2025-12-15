package com.example.finchange.operation.controller;

import com.example.finchange.common.exception.handler.GlobalExceptionHandler;
import com.example.finchange.operation.dto.UpdateSystemTimeRequest;
import com.example.finchange.operation.service.SystemDateService;
import com.example.finchange.auth.util.TestKeyGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SystemDateControllerTest {

    @Mock
    private SystemDateService systemDateService;

    @InjectMocks
    private SystemDateController systemDateController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Test JWT key pair'ini oluştur
        TestKeyGenerator.generateTestKeys();

        mockMvc = MockMvcBuilders.standaloneSetup(systemDateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // JSR310 modülünü ekle
    }

    @Test
    @DisplayName("getCurrentSystemTime - Başarılı senaryo")
    void getCurrentSystemTime_Success() throws Exception {
        // Given
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        when(systemDateService.getSystemDateTime()).thenReturn(expectedDateTime);

        // When & Then
        mockMvc.perform(get("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result").exists());

        verify(systemDateService, times(1)).getSystemDateTime();
    }

    @Test
    @DisplayName("getCurrentSystemDate - Başarılı senaryo")
    void getCurrentSystemDate_Success() throws Exception {
        // Given
        LocalDate expectedDate = LocalDate.of(2024, 1, 15);
        when(systemDateService.getSystemDate()).thenReturn(expectedDate);

        // When & Then
        mockMvc.perform(get("/api/v1/system-date/date")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result").exists());

        verify(systemDateService, times(1)).getSystemDate();
    }

    @Test
    @DisplayName("updateSystemTime - Başarılı senaryo")
    void updateSystemTime_Success() throws Exception {
        // Given
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        request.setDescription("Test güncelleme");

        doNothing().when(systemDateService).updateSystemTime(any(UpdateSystemTimeRequest.class));

        // When & Then
        mockMvc.perform(put("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("Sistem tarihi başarıyla güncellendi."));

        verify(systemDateService, times(1)).updateSystemTime(any(UpdateSystemTimeRequest.class));
    }

    @Test
    @DisplayName("updateSystemTime - Null systemTime ile başarısız senaryo")
    void updateSystemTime_NullSystemTime() throws Exception {
        // Given
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(null);
        request.setDescription("Test güncelleme");

        // When & Then
        mockMvc.perform(put("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header").exists())
                .andExpect(jsonPath("$.subErrors").exists());

        verify(systemDateService, never()).updateSystemTime(any(UpdateSystemTimeRequest.class));
    }

    @Test
    @DisplayName("updateSystemTime - Boş request body ile başarısız senaryo")
    void updateSystemTime_EmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.header").exists())
                .andExpect(jsonPath("$.subErrors").exists());

        verify(systemDateService, never()).updateSystemTime(any(UpdateSystemTimeRequest.class));
    }

    @Test
    @DisplayName("updateSystemTime - Geçersiz JSON ile başarısız senaryo")
    void updateSystemTime_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(systemDateService, never()).updateSystemTime(any(UpdateSystemTimeRequest.class));
    }

    @Test
    @DisplayName("updateSystemTime - Sadece systemTime ile başarılı senaryo")
    void updateSystemTime_OnlySystemTime() throws Exception {
        // Given
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        // description null

        doNothing().when(systemDateService).updateSystemTime(any(UpdateSystemTimeRequest.class));

        // When & Then
        mockMvc.perform(put("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("Sistem tarihi başarıyla güncellendi."));

        verify(systemDateService, times(1)).updateSystemTime(any(UpdateSystemTimeRequest.class));
    }

    @Test
    @DisplayName("getCurrentSystemTime - Servis exception durumu")
    void getCurrentSystemTime_ServiceException() throws Exception {
        // Given
        when(systemDateService.getSystemDateTime()).thenThrow(new RuntimeException("Sistem hatası"));

        // When & Then
        mockMvc.perform(get("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(systemDateService, times(1)).getSystemDateTime();
    }

    @Test
    @DisplayName("getCurrentSystemDate - Servis exception durumu")
    void getCurrentSystemDate_ServiceException() throws Exception {
        // Given
        when(systemDateService.getSystemDate()).thenThrow(new RuntimeException("Sistem hatası"));

        // When & Then
        mockMvc.perform(get("/api/v1/system-date/date")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(systemDateService, times(1)).getSystemDate();
    }

    @Test
    @DisplayName("updateSystemTime - Servis exception durumu")
    void updateSystemTime_ServiceException() throws Exception {
        // Given
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        request.setDescription("Test güncelleme");

        doThrow(new RuntimeException("Güncelleme hatası")).when(systemDateService)
                .updateSystemTime(any(UpdateSystemTimeRequest.class));

        // When & Then
        mockMvc.perform(put("/api/v1/system-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(systemDateService, times(1)).updateSystemTime(any(UpdateSystemTimeRequest.class));
    }
}
