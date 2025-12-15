package com.example.finchange.operation.service;

import com.example.finchange.operation.dto.HolidayRequest;
import com.example.finchange.operation.dto.HolidayResponse;
import com.example.finchange.operation.exception.HolidayAlreadyException;
import com.example.finchange.operation.exception.HolidayNotFoundException;
import com.example.finchange.operation.exception.InvalidHolidayDateException;
import com.example.finchange.operation.model.Holiday;
import com.example.finchange.operation.model.enums.HolidayType;
import com.example.finchange.operation.repository.HolidayRepository;
import com.example.finchange.operation.service.impl.HolidayServiceImpl;
import com.example.finchange.operation.service.impl.SystemDateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HolidayServiceImpl Unit Tests")
class HolidayServiceImplTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private SystemDateServiceImpl systemDateService;

    @InjectMocks
    private HolidayServiceImpl holidayService;

    private HolidayRequest validRequest;
    private Holiday testHoliday;
    private LocalDate currentSystemDate;

    @BeforeEach
    void setUp() {
        currentSystemDate = LocalDate.of(2024, 1, 15);
        
        validRequest = new HolidayRequest();
        validRequest.setHolidayDate(LocalDate.of(2024, 12, 25));
        validRequest.setType(HolidayType.RESMI_TATIL);
        validRequest.setDescription("Noel");

        testHoliday = new Holiday();
        testHoliday.setId(1);
        testHoliday.setHolidayDate(LocalDate.of(2024, 12, 25));
        testHoliday.setType(HolidayType.RESMI_TATIL);
        testHoliday.setDescription("Noel");
    }

    @Test
    @DisplayName("createHoliday - Başarılı tatil oluşturma")
    void createHoliday_Success() {
        // Given
        when(systemDateService.getSystemDate()).thenReturn(currentSystemDate);
        when(holidayRepository.existsByHolidayDate(validRequest.getHolidayDate())).thenReturn(false);
        when(holidayRepository.save(any(Holiday.class))).thenReturn(testHoliday);

        // When
        HolidayResponse result = holidayService.createHoliday(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getHolidayDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        assertThat(result.getType()).isEqualTo("RESMI_TATIL");
        assertThat(result.getDescription()).isEqualTo("Noel");

        verify(systemDateService, times(1)).getSystemDate();
        verify(holidayRepository, times(1)).existsByHolidayDate(validRequest.getHolidayDate());
        verify(holidayRepository, times(1)).save(any(Holiday.class));
    }

    @Test
    @DisplayName("createHoliday - Geçersiz tarih hatası (geçmiş tarih)")
    void createHoliday_InvalidDate_PastDate() {
        // Given
        validRequest.setHolidayDate(LocalDate.of(2024, 1, 10)); // Sistem tarihinden önce
        when(systemDateService.getSystemDate()).thenReturn(currentSystemDate);

        // When & Then
        assertThatThrownBy(() -> holidayService.createHoliday(validRequest))
                .isInstanceOf(InvalidHolidayDateException.class)
                .hasMessageContaining("Geçersiz tatil tarihi");

        verify(systemDateService, times(1)).getSystemDate();
        verify(holidayRepository, never()).existsByHolidayDate(any());
        verify(holidayRepository, never()).save(any());
    }

    @Test
    @DisplayName("createHoliday - Geçersiz tarih hatası (aynı tarih)")
    void createHoliday_InvalidDate_SameDate() {
        // Given
        validRequest.setHolidayDate(currentSystemDate); // Sistem tarihi ile aynı
        when(systemDateService.getSystemDate()).thenReturn(currentSystemDate);

        // When & Then
        assertThatThrownBy(() -> holidayService.createHoliday(validRequest))
                .isInstanceOf(InvalidHolidayDateException.class)
                .hasMessageContaining("Geçersiz tatil tarihi");

        verify(systemDateService, times(1)).getSystemDate();
        verify(holidayRepository, never()).existsByHolidayDate(any());
        verify(holidayRepository, never()).save(any());
    }

    @Test
    @DisplayName("createHoliday - Tatil zaten mevcut hatası")
    void createHoliday_HolidayAlreadyExists() {
        // Given
        when(systemDateService.getSystemDate()).thenReturn(currentSystemDate);
        when(holidayRepository.existsByHolidayDate(validRequest.getHolidayDate())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> holidayService.createHoliday(validRequest))
                .isInstanceOf(HolidayAlreadyException.class);

        verify(systemDateService, times(1)).getSystemDate();
        verify(holidayRepository, times(1)).existsByHolidayDate(validRequest.getHolidayDate());
        verify(holidayRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateHoliday - Başarılı tatil güncelleme")
    void updateHoliday_Success() {
        // Given
        Integer holidayId = 1;
        when(holidayRepository.findById(holidayId)).thenReturn(Optional.of(testHoliday));
        when(systemDateService.getSystemDate()).thenReturn(currentSystemDate);
        when(holidayRepository.save(any(Holiday.class))).thenReturn(testHoliday);

        // When
        HolidayResponse result = holidayService.updateHoliday(holidayId, validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);

        verify(holidayRepository, times(1)).findById(holidayId);
        verify(systemDateService, times(1)).getSystemDate();
        verify(holidayRepository, times(1)).save(any(Holiday.class));
    }

    @Test
    @DisplayName("updateHoliday - Tatil bulunamadı hatası")
    void updateHoliday_HolidayNotFound() {
        // Given
        Integer holidayId = 999;
        when(holidayRepository.findById(holidayId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> holidayService.updateHoliday(holidayId, validRequest))
                .isInstanceOf(HolidayNotFoundException.class)
                .hasMessageContaining("Bu tarihle ilgili id bulunamadı: " + holidayId);

        verify(holidayRepository, times(1)).findById(holidayId);
        verify(systemDateService, never()).getSystemDate();
        verify(holidayRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateHoliday - Geçersiz tarih hatası")
    void updateHoliday_InvalidDate() {
        // Given
        Integer holidayId = 1;
        validRequest.setHolidayDate(LocalDate.of(2024, 1, 10)); // Geçmiş tarih
        when(holidayRepository.findById(holidayId)).thenReturn(Optional.of(testHoliday));
        when(systemDateService.getSystemDate()).thenReturn(currentSystemDate);

        // When & Then
        assertThatThrownBy(() -> holidayService.updateHoliday(holidayId, validRequest))
                .isInstanceOf(InvalidHolidayDateException.class)
                .hasMessageContaining("Geçersiz tatil tarihi");

        verify(holidayRepository, times(1)).findById(holidayId);
        verify(systemDateService, times(1)).getSystemDate();
        verify(holidayRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteHoliday - Başarılı tatil silme")
    void deleteHoliday_Success() {
        // Given
        Integer holidayId = 1;
        when(holidayRepository.existsById(holidayId)).thenReturn(true);
        doNothing().when(holidayRepository).deleteById(holidayId);

        // When
        holidayService.deleteHoliday(holidayId);

        // Then
        verify(holidayRepository, times(1)).existsById(holidayId);
        verify(holidayRepository, times(1)).deleteById(holidayId);
    }

    @Test
    @DisplayName("deleteHoliday - Tatil bulunamadı hatası")
    void deleteHoliday_HolidayNotFound() {
        // Given
        Integer holidayId = 999;
        when(holidayRepository.existsById(holidayId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> holidayService.deleteHoliday(holidayId))
                .isInstanceOf(HolidayNotFoundException.class)
                .hasMessageContaining("bu tarihle ilgili id bulunamadı: " + holidayId);

        verify(holidayRepository, times(1)).existsById(holidayId);
        verify(holidayRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("getHolidayById - Başarılı tatil getirme")
    void getHolidayById_Success() {
        // Given
        Integer holidayId = 1;
        when(holidayRepository.findById(holidayId)).thenReturn(Optional.of(testHoliday));

        // When
        HolidayResponse result = holidayService.getHolidayById(holidayId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getHolidayDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        assertThat(result.getType()).isEqualTo("RESMI_TATIL");
        assertThat(result.getDescription()).isEqualTo("Noel");

        verify(holidayRepository, times(1)).findById(holidayId);
    }

    @Test
    @DisplayName("getHolidayById - Tatil bulunamadı hatası")
    void getHolidayById_HolidayNotFound() {
        // Given
        Integer holidayId = 999;
        when(holidayRepository.findById(holidayId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> holidayService.getHolidayById(holidayId))
                .isInstanceOf(HolidayNotFoundException.class)
                .hasMessageContaining("bu tarihle ilgili id bulunamadı: " + holidayId);

        verify(holidayRepository, times(1)).findById(holidayId);
    }

    @Test
    @DisplayName("getAllHolidays - Başarılı tüm tatilleri getirme")
    void getAllHolidays_Success() {
        // Given
        Holiday holiday2 = new Holiday();
        holiday2.setId(2);
        holiday2.setHolidayDate(LocalDate.of(2024, 1, 1));
        holiday2.setType(HolidayType.RESMI_TATIL);
        holiday2.setDescription("Yılbaşı");

        List<Holiday> holidays = Arrays.asList(testHoliday, holiday2);
        when(holidayRepository.findAll()).thenReturn(holidays);

        // When
        List<HolidayResponse> result = holidayService.getAllHolidays();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);

        verify(holidayRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllHolidays - Boş liste")
    void getAllHolidays_EmptyList() {
        // Given
        when(holidayRepository.findAll()).thenReturn(List.of());

        // When
        List<HolidayResponse> result = holidayService.getAllHolidays();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(holidayRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getHolidaysByDate - Başarılı tarihle tatil getirme")
    void getHolidaysByDate_Success() {
        // Given
        LocalDate searchDate = LocalDate.of(2024, 12, 25);
        when(holidayRepository.findByHolidayDate(searchDate)).thenReturn(Optional.of(testHoliday));

        // When
        List<HolidayResponse> result = holidayService.getHolidaysByDate(searchDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getHolidayDate()).isEqualTo(searchDate);

        verify(holidayRepository, times(1)).findByHolidayDate(searchDate);
    }

    @Test
    @DisplayName("getHolidaysByDate - Tatil bulunamadı")
    void getHolidaysByDate_NotFound() {
        // Given
        LocalDate searchDate = LocalDate.of(2024, 6, 15);
        when(holidayRepository.findByHolidayDate(searchDate)).thenReturn(Optional.empty());

        // When
        List<HolidayResponse> result = holidayService.getHolidaysByDate(searchDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(holidayRepository, times(1)).findByHolidayDate(searchDate);
    }
}
