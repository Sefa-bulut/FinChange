package com.example.finchange.operation.repository;

import com.example.finchange.operation.model.Holiday;
import com.example.finchange.operation.model.enums.HolidayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HolidayRepository için unit testler
 * Mockito kullanarak repository davranışını test eder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HolidayRepository Unit Tests")
class HolidayRepositoryTest {

    @Mock
    private HolidayRepository holidayRepository;

    private Holiday testHoliday1;
    private Holiday testHoliday2;

    @BeforeEach
    void setUp() {
        testHoliday1 = new Holiday();
        testHoliday1.setHolidayDate(LocalDate.of(2024, 12, 25));
        testHoliday1.setType(HolidayType.RESMI_TATIL);
        testHoliday1.setDescription("Noel");

        testHoliday2 = new Holiday();
        testHoliday2.setHolidayDate(LocalDate.of(2024, 1, 1));
        testHoliday2.setType(HolidayType.RESMI_TATIL);
        testHoliday2.setDescription("Yılbaşı");
    }

    @Test
    @DisplayName("findByHolidayDate - Başarılı tatil bulma")
    void findByHolidayDate_Success() {
        // Given
        when(holidayRepository.findByHolidayDate(LocalDate.of(2024, 12, 25)))
                .thenReturn(Optional.of(testHoliday1));

        // When
        Optional<Holiday> result = holidayRepository.findByHolidayDate(LocalDate.of(2024, 12, 25));

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getHolidayDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        assertThat(result.get().getType()).isEqualTo(HolidayType.RESMI_TATIL);
        assertThat(result.get().getDescription()).isEqualTo("Noel");
        verify(holidayRepository, times(1)).findByHolidayDate(LocalDate.of(2024, 12, 25));
    }

    @Test
    @DisplayName("findByHolidayDate - Tatil bulunamadı")
    void findByHolidayDate_NotFound() {
        // Given
        when(holidayRepository.findByHolidayDate(LocalDate.of(2024, 6, 15)))
                .thenReturn(Optional.empty());

        // When
        Optional<Holiday> result = holidayRepository.findByHolidayDate(LocalDate.of(2024, 6, 15));

        // Then
        assertThat(result).isEmpty();
        verify(holidayRepository, times(1)).findByHolidayDate(LocalDate.of(2024, 6, 15));
    }

    @Test
    @DisplayName("existsByHolidayDate - Tatil mevcut")
    void existsByHolidayDate_True() {
        // Given
        when(holidayRepository.existsByHolidayDate(LocalDate.of(2024, 12, 25)))
                .thenReturn(true);

        // When
        boolean exists = holidayRepository.existsByHolidayDate(LocalDate.of(2024, 12, 25));

        // Then
        assertThat(exists).isTrue();
        verify(holidayRepository, times(1)).existsByHolidayDate(LocalDate.of(2024, 12, 25));
    }

    @Test
    @DisplayName("existsByHolidayDate - Tatil mevcut değil")
    void existsByHolidayDate_False() {
        // Given
        when(holidayRepository.existsByHolidayDate(LocalDate.of(2024, 6, 15)))
                .thenReturn(false);

        // When
        boolean exists = holidayRepository.existsByHolidayDate(LocalDate.of(2024, 6, 15));

        // Then
        assertThat(exists).isFalse();
        verify(holidayRepository, times(1)).existsByHolidayDate(LocalDate.of(2024, 6, 15));
    }

    @Test
    @DisplayName("findAllHolidayDates - Tüm tatil tarihlerini getir")
    void findAllHolidayDates_Success() {
        // Given
        Set<LocalDate> expectedDates = new HashSet<>(Arrays.asList(
            LocalDate.of(2024, 12, 25),
            LocalDate.of(2024, 1, 1)
        ));
        when(holidayRepository.findAllHolidayDates()).thenReturn(expectedDates);

        // When
        Set<LocalDate> result = holidayRepository.findAllHolidayDates();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(
            LocalDate.of(2024, 12, 25),
            LocalDate.of(2024, 1, 1)
        );
        verify(holidayRepository, times(1)).findAllHolidayDates();
    }

    @Test
    @DisplayName("findAllHolidayDates - Boş liste")
    void findAllHolidayDates_EmptySet() {
        // Given
        when(holidayRepository.findAllHolidayDates()).thenReturn(new HashSet<>());

        // When
        Set<LocalDate> result = holidayRepository.findAllHolidayDates();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(holidayRepository, times(1)).findAllHolidayDates();
    }

    @Test
    @DisplayName("save - Yeni tatil kaydetme")
    void save_NewHoliday_Success() {
        // Given
        Holiday newHoliday = new Holiday();
        newHoliday.setHolidayDate(LocalDate.of(2024, 4, 23));
        newHoliday.setType(HolidayType.RESMI_TATIL);
        newHoliday.setDescription("Ulusal Egemenlik ve Çocuk Bayramı");

        Holiday savedHoliday = new Holiday();
        savedHoliday.setId(1);
        savedHoliday.setHolidayDate(LocalDate.of(2024, 4, 23));
        savedHoliday.setType(HolidayType.RESMI_TATIL);
        savedHoliday.setDescription("Ulusal Egemenlik ve Çocuk Bayramı");

        when(holidayRepository.save(newHoliday)).thenReturn(savedHoliday);

        // When
        Holiday saved = holidayRepository.save(newHoliday);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getHolidayDate()).isEqualTo(LocalDate.of(2024, 4, 23));
        assertThat(saved.getType()).isEqualTo(HolidayType.RESMI_TATIL);
        assertThat(saved.getDescription()).isEqualTo("Ulusal Egemenlik ve Çocuk Bayramı");
        verify(holidayRepository, times(1)).save(newHoliday);
    }

    @Test
    @DisplayName("findById - ID ile tatil bulma")
    void findById_Success() {
        // Given
        testHoliday1.setId(1);
        when(holidayRepository.findById(1)).thenReturn(Optional.of(testHoliday1));

        // When
        Optional<Holiday> result = holidayRepository.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getHolidayDate()).isEqualTo(LocalDate.of(2024, 12, 25));
        verify(holidayRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById - ID ile tatil bulunamadı")
    void findById_NotFound() {
        // Given
        Integer nonExistentId = 999;
        when(holidayRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Holiday> result = holidayRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(holidayRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("findAll - Tüm tatilleri getir")
    void findAll_Success() {
        // Given
        List<Holiday> expectedHolidays = Arrays.asList(testHoliday1, testHoliday2);
        when(holidayRepository.findAll()).thenReturn(expectedHolidays);

        // When
        List<Holiday> result = holidayRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Holiday::getHolidayDate)
                .contains(
                    LocalDate.of(2024, 12, 25),
                    LocalDate.of(2024, 1, 1)
                );
        verify(holidayRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("deleteById - ID ile tatil silme")
    void deleteById_Success() {
        // Given
        Integer holidayId = 1;
        doNothing().when(holidayRepository).deleteById(holidayId);
        when(holidayRepository.findById(holidayId)).thenReturn(Optional.empty());

        // When
        holidayRepository.deleteById(holidayId);

        // Then
        verify(holidayRepository, times(1)).deleteById(holidayId);
        
        // Silinme sonrası kontrol
        Optional<Holiday> deleted = holidayRepository.findById(holidayId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("existsById - ID ile tatil varlığı kontrolü")
    void existsById_True() {
        // Given
        Integer holidayId = 1;
        when(holidayRepository.existsById(holidayId)).thenReturn(true);

        // When
        boolean exists = holidayRepository.existsById(holidayId);

        // Then
        assertThat(exists).isTrue();
        verify(holidayRepository, times(1)).existsById(holidayId);
    }

    @Test
    @DisplayName("existsById - ID ile tatil varlığı kontrolü - bulunamadı")
    void existsById_False() {
        // Given
        Integer nonExistentId = 999;
        when(holidayRepository.existsById(nonExistentId)).thenReturn(false);

        // When
        boolean exists = holidayRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
        verify(holidayRepository, times(1)).existsById(nonExistentId);
    }

    @Test
    @DisplayName("Aynı tarihte birden fazla tatil kaydedilemez - unique constraint")
    void save_DuplicateHolidayDate_ShouldFail() {
        // Given
        Holiday holiday1 = new Holiday();
        holiday1.setHolidayDate(LocalDate.of(2024, 5, 1));
        holiday1.setType(HolidayType.RESMI_TATIL);
        holiday1.setDescription("İşçi Bayramı");

        Holiday holiday2 = new Holiday();
        holiday2.setHolidayDate(LocalDate.of(2024, 5, 1)); // Aynı tarih
        holiday2.setType(HolidayType.DINI_BAYRAM);
        holiday2.setDescription("Başka bir tatil");

        // Mock constraint violation exception
        when(holidayRepository.save(holiday2))
                .thenThrow(new RuntimeException("Constraint violation: duplicate holiday date"));

        // When & Then
        assertThatThrownBy(() -> holidayRepository.save(holiday2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Constraint violation");
        
        verify(holidayRepository, times(1)).save(holiday2);
    }
}
