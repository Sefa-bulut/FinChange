package com.example.finchange.operation.service;

import com.example.finchange.operation.dto.UpdateSystemTimeRequest;
import com.example.finchange.operation.model.SystemParameters;
import com.example.finchange.operation.repository.SystemParametersRepository;
import com.example.finchange.operation.service.impl.SystemDateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemDateServiceImpl Unit Tests")
class SystemDateServiceImplTest {

    @Mock
    private SystemParametersRepository repository;

    @InjectMocks
    private SystemDateServiceImpl systemDateService;

    private SystemParameters testSystemParameters;
    private LocalDateTime testSystemTime;

    @BeforeEach
    void setUp() {
        testSystemTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        testSystemParameters = new SystemParameters();
        testSystemParameters.setId(1);
        testSystemParameters.setSystemTime(testSystemTime);
        testSystemParameters.setDescription("Test sistem parametreleri");
    }

    @Test
    @DisplayName("getSystemDateTime - Başarılı sistem tarih-saat getirme")
    void getSystemDateTime_Success() {
        // Given
        // Cache'i manuel olarak set et
        ReflectionTestUtils.setField(systemDateService, "cachedSystemTime", testSystemTime);

        // When
        LocalDateTime result = systemDateService.getSystemDateTime();

        // Then
        assertThat(result).isEqualTo(testSystemTime);
    }

    @Test
    @DisplayName("getSystemDateTime - Sistem parametresi bulunamadığında şu anki zamanı döndür")
    void getSystemDateTime_SystemParametersNotFound_ReturnsCurrentTime() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();
        
        // Cache'i şu anki zaman ile set et
        ReflectionTestUtils.setField(systemDateService, "cachedSystemTime", currentTime);

        // When
        LocalDateTime result = systemDateService.getSystemDateTime();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(currentTime);
    }

    @Test
    @DisplayName("getSystemDate - Başarılı sistem tarihi getirme")
    void getSystemDate_Success() {
        // Given
        ReflectionTestUtils.setField(systemDateService, "cachedSystemTime", testSystemTime);

        // When
        LocalDate result = systemDateService.getSystemDate();

        // Then
        assertThat(result).isEqualTo(testSystemTime.toLocalDate());
        assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    @DisplayName("updateSystemTime - Başarılı sistem zamanı güncelleme")
    void updateSystemTime_Success() {
        // Given
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(newSystemTime);
        request.setDescription("Güncellenen sistem zamanı");

        when(repository.findById(1)).thenReturn(Optional.of(testSystemParameters));
        when(repository.save(any(SystemParameters.class))).thenReturn(testSystemParameters);

        // When
        systemDateService.updateSystemTime(request);

        // Then
        verify(repository, times(1)).findById(1);
        verify(repository, times(1)).save(any(SystemParameters.class));
        
        // Cache'in güncellendiğini kontrol et
        LocalDateTime updatedTime = systemDateService.getSystemDateTime();
        assertThat(updatedTime).isEqualTo(newSystemTime);
    }

    @Test
    @DisplayName("updateSystemTime - Sadece açıklama ile güncelleme")
    void updateSystemTime_WithDescriptionOnly() {
        // Given
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(newSystemTime);
        request.setDescription("Sadece açıklama güncellendi");

        when(repository.findById(1)).thenReturn(Optional.of(testSystemParameters));
        when(repository.save(any(SystemParameters.class))).thenReturn(testSystemParameters);

        // When
        systemDateService.updateSystemTime(request);

        // Then
        verify(repository, times(1)).findById(1);
        verify(repository, times(1)).save(any(SystemParameters.class));
    }

    @Test
    @DisplayName("updateSystemTime - Boş açıklama ile güncelleme")
    void updateSystemTime_WithEmptyDescription() {
        // Given
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(newSystemTime);
        request.setDescription(""); // Boş açıklama

        when(repository.findById(1)).thenReturn(Optional.of(testSystemParameters));
        when(repository.save(any(SystemParameters.class))).thenReturn(testSystemParameters);

        // When
        systemDateService.updateSystemTime(request);

        // Then
        verify(repository, times(1)).findById(1);
        verify(repository, times(1)).save(any(SystemParameters.class));
    }

    @Test
    @DisplayName("updateSystemTime - Null açıklama ile güncelleme")
    void updateSystemTime_WithNullDescription() {
        // Given
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(newSystemTime);
        request.setDescription(null); // Null açıklama

        when(repository.findById(1)).thenReturn(Optional.of(testSystemParameters));
        when(repository.save(any(SystemParameters.class))).thenReturn(testSystemParameters);

        // When
        systemDateService.updateSystemTime(request);

        // Then
        verify(repository, times(1)).findById(1);
        verify(repository, times(1)).save(any(SystemParameters.class));
    }

    @Test
    @DisplayName("updateSystemTime - Sistem parametresi bulunamadı hatası")
    void updateSystemTime_SystemParametersNotFound() {
        // Given
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(newSystemTime);
        request.setDescription("Test açıklama");

        when(repository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> systemDateService.updateSystemTime(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sistem parametreleri tablosunda id=1 olan kayıt bulunamadı!");

        verify(repository, times(1)).findById(1);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cache mekanizması - İlk yükleme ve güncelleme sonrası kontrol")
    void cacheVerification_InitialLoadAndUpdate() {
        // Given - İlk cache set
        ReflectionTestUtils.setField(systemDateService, "cachedSystemTime", testSystemTime);

        // İlk cache kontrolü
        LocalDateTime initialTime = systemDateService.getSystemDateTime();
        assertThat(initialTime).isEqualTo(testSystemTime);

        // Given - Güncelleme
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 12, 25, 15, 45, 0);
        UpdateSystemTimeRequest request = new UpdateSystemTimeRequest();
        request.setSystemTime(newSystemTime);

        when(repository.findById(1)).thenReturn(Optional.of(testSystemParameters));
        when(repository.save(any(SystemParameters.class))).thenReturn(testSystemParameters);

        // When - Güncelleme yap
        systemDateService.updateSystemTime(request);

        // Then - Cache'in güncellendiğini kontrol et
        LocalDateTime updatedTime = systemDateService.getSystemDateTime();
        assertThat(updatedTime).isEqualTo(newSystemTime);
        assertThat(updatedTime).isNotEqualTo(initialTime);

        // Tarih kontrolü
        LocalDate updatedDate = systemDateService.getSystemDate();
        assertThat(updatedDate).isEqualTo(newSystemTime.toLocalDate());
    }
}
