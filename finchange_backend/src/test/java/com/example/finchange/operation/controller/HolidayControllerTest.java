package com.example.finchange.operation.controller;

import com.example.finchange.auth.util.TestKeyGenerator;
import com.example.finchange.common.exception.handler.GlobalExceptionHandler;
import com.example.finchange.operation.dto.HolidayRequest;
import com.example.finchange.operation.dto.HolidayResponse;
import com.example.finchange.operation.exception.HolidayAlreadyException;
import com.example.finchange.operation.exception.HolidayNotFoundException;
import com.example.finchange.operation.exception.InvalidHolidayDateException;
import com.example.finchange.operation.model.enums.HolidayType;
import com.example.finchange.operation.service.HolidayService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HolidayControllerTest {

        @Mock
        private HolidayService holidayService;

        @InjectMocks
        private HolidayController holidayController;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                // Test JWT key pair'ini oluştur
                TestKeyGenerator.generateTestKeys();

                mockMvc = MockMvcBuilders.standaloneSetup(holidayController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
                objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules(); // JSR310 modülünü ekle
        }

        @Test
        @DisplayName("createHoliday - Başarılı senaryo")
        void createHoliday_Success() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(HolidayType.RESMI_TATIL);
                request.setDescription("Noel");

                HolidayResponse response = HolidayResponse.builder()
                                .id(1)
                                .holidayDate(LocalDate.of(2024, 12, 25))
                                .type("RESMI_TATIL")
                                .description("Noel")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .lastModifiedBy(1)
                                .build();

                when(holidayService.createHoliday(any(HolidayRequest.class))).thenReturn(response);

                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.id").value(1))
                                .andExpect(jsonPath("$.result.holidayDate").value("2024-12-25"))
                                .andExpect(jsonPath("$.result.type").value("RESMI_TATIL"))
                                .andExpect(jsonPath("$.result.description").value("Noel"))
                                .andExpect(jsonPath("$.message").value("Tatil başarıyla oluşturuldu."));

                verify(holidayService, times(1)).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("createHoliday - Null holidayDate ile başarısız senaryo")
        void createHoliday_NullHolidayDate() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(null);
                request.setType(HolidayType.RESMI_TATIL);
                request.setDescription("Noel");

                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value("Tatil tarihi boş olamaz"));

                verify(holidayService, never()).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("createHoliday - Null type ile başarısız senaryo")
        void createHoliday_NullType() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(null);
                request.setDescription("Noel");

                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value("Tatil türü boş olamaz"));

                verify(holidayService, never()).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("createHoliday - Boş description ile başarısız senaryo")
        void createHoliday_EmptyDescription() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(HolidayType.RESMI_TATIL);
                request.setDescription("");

                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value("Açıklama boş olamaz"));

                verify(holidayService, never()).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("createHoliday - Geçersiz tarih exception")
        void createHoliday_InvalidDateException() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(HolidayType.RESMI_TATIL);
                request.setDescription("Noel");

                when(holidayService.createHoliday(any(HolidayRequest.class)))
                                .thenThrow(new InvalidHolidayDateException("Geçersiz tatil tarihi"));

                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value("Geçersiz tatil tarihi"));

                verify(holidayService, times(1)).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("createHoliday - Tatil zaten mevcut exception")
        void createHoliday_AlreadyExistsException() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(HolidayType.RESMI_TATIL);
                request.setDescription("Noel");

                when(holidayService.createHoliday(any(HolidayRequest.class)))
                                .thenThrow(new HolidayAlreadyException(LocalDate.of(2024, 12, 25)));

                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message")
                                                .value("Bu tarih zaten tatil olarak eklenmiş: 2024-12-25"));

                verify(holidayService, times(1)).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("updateHoliday - Başarılı senaryo")
        void updateHoliday_Success() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(HolidayType.DINI_BAYRAM);
                request.setDescription("Noel Bayramı");

                HolidayResponse response = HolidayResponse.builder()
                                .id(1)
                                .holidayDate(LocalDate.of(2024, 12, 25))
                                .type("DINI_BAYRAM")
                                .description("Noel Bayramı")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .lastModifiedBy(1)
                                .build();

                when(holidayService.updateHoliday(anyInt(), any(HolidayRequest.class))).thenReturn(response);

                // When & Then
                mockMvc.perform(put("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.id").value(1))
                                .andExpect(jsonPath("$.result.holidayDate").value("2024-12-25"))
                                .andExpect(jsonPath("$.result.type").value("DINI_BAYRAM"))
                                .andExpect(jsonPath("$.result.description").value("Noel Bayramı"))
                                .andExpect(jsonPath("$.message").value("Tatil bilgisi başarıyla güncellendi."));

                verify(holidayService, times(1)).updateHoliday(1, request);
        }

        @Test
        @DisplayName("updateHoliday - Tatil bulunamadı exception")
        void updateHoliday_NotFoundException() throws Exception {
                // Given
                HolidayRequest request = new HolidayRequest();
                request.setHolidayDate(LocalDate.of(2024, 12, 25));
                request.setType(HolidayType.RESMI_TATIL);
                request.setDescription("Noel");

                when(holidayService.updateHoliday(anyInt(), any(HolidayRequest.class)))
                                .thenThrow(new HolidayNotFoundException("Bu tarihle ilgili id bulunamadı: 1"));

                // When & Then
                mockMvc.perform(put("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value("Bu tarihle ilgili id bulunamadı: 1"));

                verify(holidayService, times(1)).updateHoliday(1, request);
        }

        @Test
        @DisplayName("deleteHoliday - Başarılı senaryo")
        void deleteHoliday_Success() throws Exception {
                // Given
                doNothing().when(holidayService).deleteHoliday(1);

                // When & Then
                mockMvc.perform(delete("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                                .andExpect(jsonPath("$.message").value("Tatil başarıyla silindi."));

                verify(holidayService, times(1)).deleteHoliday(1);
        }

        @Test
        @DisplayName("deleteHoliday - Tatil bulunamadı exception")
        void deleteHoliday_NotFoundException() throws Exception {
                // Given
                doThrow(new HolidayNotFoundException(" bu tarihle ilgili id bulunamadı: 1"))
                                .when(holidayService).deleteHoliday(1);

                // When & Then
                mockMvc.perform(delete("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value(" bu tarihle ilgili id bulunamadı: 1"));

                verify(holidayService, times(1)).deleteHoliday(1);
        }

        @Test
        @DisplayName("getHolidayById - Başarılı senaryo")
        void getHolidayById_Success() throws Exception {
                // Given
                HolidayResponse response = HolidayResponse.builder()
                                .id(1)
                                .holidayDate(LocalDate.of(2024, 12, 25))
                                .type("RESMI_TATIL")
                                .description("Noel")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .lastModifiedBy(1)
                                .build();

                when(holidayService.getHolidayById(1)).thenReturn(response);

                // When & Then
                mockMvc.perform(get("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.id").value(1))
                                .andExpect(jsonPath("$.result.holidayDate").value("2024-12-25"))
                                .andExpect(jsonPath("$.result.type").value("RESMI_TATIL"))
                                .andExpect(jsonPath("$.result.description").value("Noel"));

                verify(holidayService, times(1)).getHolidayById(1);
        }

        @Test
        @DisplayName("getHolidayById - Tatil bulunamadı exception")
        void getHolidayById_NotFoundException() throws Exception {
                // Given
                when(holidayService.getHolidayById(1))
                                .thenThrow(new HolidayNotFoundException(" bu tarihle ilgili id bulunamadı: 1"));

                // When & Then
                mockMvc.perform(get("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.header").exists())
                                .andExpect(jsonPath("$.message").value(" bu tarihle ilgili id bulunamadı: 1"));

                verify(holidayService, times(1)).getHolidayById(1);
        }

        @Test
        @DisplayName("getAllHolidays - Başarılı senaryo")
        void getAllHolidays_Success() throws Exception {
                // Given
                List<HolidayResponse> responses = Arrays.asList(
                                HolidayResponse.builder()
                                                .id(1)
                                                .holidayDate(LocalDate.of(2024, 12, 25))
                                                .type("RESMI_TATIL")
                                                .description("Noel")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .lastModifiedBy(1)
                                                .build(),
                                HolidayResponse.builder()
                                                .id(2)
                                                .holidayDate(LocalDate.of(2024, 1, 1))
                                                .type("RESMI_TATIL")
                                                .description("Yılbaşı")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .lastModifiedBy(1)
                                                .build());

                when(holidayService.getAllHolidays()).thenReturn(responses);

                // When & Then
                mockMvc.perform(get("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result").isArray())
                                .andExpect(jsonPath("$.result.length()").value(2))
                                .andExpect(jsonPath("$.result[0].id").value(1))
                                .andExpect(jsonPath("$.result[0].holidayDate").value("2024-12-25"))
                                .andExpect(jsonPath("$.result[1].id").value(2))
                                .andExpect(jsonPath("$.result[1].holidayDate").value("2024-01-01"));

                verify(holidayService, times(1)).getAllHolidays();
        }

        @Test
        @DisplayName("getHolidaysByDate - Başarılı senaryo")
        void getHolidaysByDate_Success() throws Exception {
                // Given
                LocalDate searchDate = LocalDate.of(2024, 12, 25);
                List<HolidayResponse> responses = Arrays.asList(
                                HolidayResponse.builder()
                                                .id(1)
                                                .holidayDate(searchDate)
                                                .type("RESMI_TATIL")
                                                .description("Noel")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .lastModifiedBy(1)
                                                .build());

                when(holidayService.getHolidaysByDate(searchDate)).thenReturn(responses);

                // When & Then
                mockMvc.perform(get("/api/v1/holidays/by-date")
                                .param("date", "2024-12-25")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result").isArray())
                                .andExpect(jsonPath("$.result.length()").value(1))
                                .andExpect(jsonPath("$.result[0].id").value(1))
                                .andExpect(jsonPath("$.result[0].holidayDate").value("2024-12-25"))
                                .andExpect(jsonPath("$.result[0].type").value("RESMI_TATIL"))
                                .andExpect(jsonPath("$.result[0].description").value("Noel"));

                verify(holidayService, times(1)).getHolidaysByDate(searchDate);
        }

        @Test
        @DisplayName("getHolidaysByDate - Boş liste döndürme")
        void getHolidaysByDate_EmptyList() throws Exception {
                // Given
                LocalDate searchDate = LocalDate.of(2024, 12, 25);
                when(holidayService.getHolidaysByDate(searchDate)).thenReturn(List.of());

                // When & Then
                mockMvc.perform(get("/api/v1/holidays/by-date")
                                .param("date", "2024-12-25")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result").isArray())
                                .andExpect(jsonPath("$.result.length()").value(0));

                verify(holidayService, times(1)).getHolidaysByDate(searchDate);
        }

        @Test
        @DisplayName("getHolidaysByDate - Geçersiz tarih formatı")
        void getHolidaysByDate_InvalidDateFormat() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/holidays/by-date")
                                .param("date", "invalid-date")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());

                verify(holidayService, never()).getHolidaysByDate(any(LocalDate.class));
        }

        @Test
        @DisplayName("createHoliday - Geçersiz JSON ile başarısız senaryo")
        void createHoliday_InvalidJson() throws Exception {
                // When & Then
                mockMvc.perform(post("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("invalid json"))
                                .andExpect(status().isBadRequest());

                verify(holidayService, never()).createHoliday(any(HolidayRequest.class));
        }

        @Test
        @DisplayName("updateHoliday - Geçersiz JSON ile başarısız senaryo")
        void updateHoliday_InvalidJson() throws Exception {
                // When & Then
                mockMvc.perform(put("/api/v1/holidays/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("invalid json"))
                                .andExpect(status().isBadRequest());

                verify(holidayService, never()).updateHoliday(anyInt(), any(HolidayRequest.class));
        }
}
