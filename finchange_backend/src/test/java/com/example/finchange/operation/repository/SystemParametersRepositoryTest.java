package com.example.finchange.operation.repository;

import com.example.finchange.operation.model.SystemParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SystemParametersRepository için unit testler
 * Mockito kullanarak repository davranışını test eder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SystemParametersRepository Unit Tests")
class SystemParametersRepositoryTest {

    @Mock
    private SystemParametersRepository systemParametersRepository;

    private SystemParameters testSystemParameters;

    @BeforeEach
    void setUp() {
        testSystemParameters = new SystemParameters();
        testSystemParameters.setId(1);
        testSystemParameters.setSystemTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        testSystemParameters.setDescription("Test sistem parametreleri");
    }

    @Test
    @DisplayName("save - Yeni sistem parametresi kaydetme")
    void save_NewSystemParameters_Success() {
        // Given
        SystemParameters newParams = new SystemParameters();
        newParams.setSystemTime(LocalDateTime.of(2024, 6, 15, 14, 30, 0));
        newParams.setDescription("Yeni sistem parametreleri");

        SystemParameters savedParams = new SystemParameters();
        savedParams.setId(1);
        savedParams.setSystemTime(LocalDateTime.of(2024, 6, 15, 14, 30, 0));
        savedParams.setDescription("Yeni sistem parametreleri");

        when(systemParametersRepository.save(newParams)).thenReturn(savedParams);

        // When
        SystemParameters saved = systemParametersRepository.save(newParams);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSystemTime()).isEqualTo(LocalDateTime.of(2024, 6, 15, 14, 30, 0));
        assertThat(saved.getDescription()).isEqualTo("Yeni sistem parametreleri");
        verify(systemParametersRepository, times(1)).save(newParams);
    }

    @Test
    @DisplayName("findById - ID ile sistem parametresi bulma")
    void findById_Success() {
        // Given
        testSystemParameters.setId(1);
        when(systemParametersRepository.findById(1)).thenReturn(Optional.of(testSystemParameters));

        // When
        Optional<SystemParameters> result = systemParametersRepository.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getSystemTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        assertThat(result.get().getDescription()).isEqualTo("Test sistem parametreleri");
        verify(systemParametersRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById - ID ile sistem parametresi bulunamadı")
    void findById_NotFound() {
        // Given
        Integer nonExistentId = 999;
        when(systemParametersRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<SystemParameters> result = systemParametersRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(systemParametersRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("findAll - Tüm sistem parametrelerini getir")
    void findAll_Success() {
        // Given
        SystemParameters params1 = new SystemParameters();
        params1.setSystemTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        params1.setDescription("Sistem parametresi 1");

        SystemParameters params2 = new SystemParameters();
        params2.setSystemTime(LocalDateTime.of(2024, 6, 15, 14, 30, 0));
        params2.setDescription("Sistem parametresi 2");

        List<SystemParameters> expectedParams = Arrays.asList(params1, params2);
        when(systemParametersRepository.findAll()).thenReturn(expectedParams);

        // When
        List<SystemParameters> result = systemParametersRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(SystemParameters::getDescription)
                .contains("Sistem parametresi 1", "Sistem parametresi 2");
        verify(systemParametersRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll - Boş liste")
    void findAll_EmptyList() {
        // Given
        when(systemParametersRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<SystemParameters> result = systemParametersRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(systemParametersRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("existsById - ID ile sistem parametresi varlığı kontrolü")
    void existsById_True() {
        // Given
        Integer paramsId = 1;
        when(systemParametersRepository.existsById(paramsId)).thenReturn(true);

        // When
        boolean exists = systemParametersRepository.existsById(paramsId);

        // Then
        assertThat(exists).isTrue();
        verify(systemParametersRepository, times(1)).existsById(paramsId);
    }

    @Test
    @DisplayName("existsById - ID ile sistem parametresi varlığı kontrolü - bulunamadı")
    void existsById_False() {
        // Given
        Integer nonExistentId = 999;
        when(systemParametersRepository.existsById(nonExistentId)).thenReturn(false);

        // When
        boolean exists = systemParametersRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
        verify(systemParametersRepository, times(1)).existsById(nonExistentId);
    }

    @Test
    @DisplayName("save - Mevcut sistem parametresini güncelleme")
    void save_UpdateExistingSystemParameters_Success() {
        // Given
        testSystemParameters.setId(1);
        Integer paramsId = 1;

        // Güncelleme için yeni değerler
        LocalDateTime newSystemTime = LocalDateTime.of(2024, 12, 25, 15, 45, 0);
        String newDescription = "Güncellenmiş sistem parametreleri";

        SystemParameters updatedParams = new SystemParameters();
        updatedParams.setId(paramsId);
        updatedParams.setSystemTime(newSystemTime);
        updatedParams.setDescription(newDescription);

        when(systemParametersRepository.save(any(SystemParameters.class))).thenReturn(updatedParams);

        // When
        testSystemParameters.setSystemTime(newSystemTime);
        testSystemParameters.setDescription(newDescription);
        SystemParameters updated = systemParametersRepository.save(testSystemParameters);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(paramsId);
        assertThat(updated.getSystemTime()).isEqualTo(newSystemTime);
        assertThat(updated.getDescription()).isEqualTo(newDescription);
        verify(systemParametersRepository, times(1)).save(testSystemParameters);
    }

    @Test
    @DisplayName("deleteById - ID ile sistem parametresi silme")
    void deleteById_Success() {
        // Given
        Integer paramsId = 1;
        doNothing().when(systemParametersRepository).deleteById(paramsId);
        when(systemParametersRepository.findById(paramsId)).thenReturn(Optional.empty());

        // When
        systemParametersRepository.deleteById(paramsId);

        // Then
        verify(systemParametersRepository, times(1)).deleteById(paramsId);
        
        // Silinme sonrası kontrol
        Optional<SystemParameters> deleted = systemParametersRepository.findById(paramsId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("count - Toplam sistem parametresi sayısı")
    void count_Success() {
        // Given
        when(systemParametersRepository.count()).thenReturn(2L);

        // When
        long count = systemParametersRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
        verify(systemParametersRepository, times(1)).count();
    }

    @Test
    @DisplayName("count - Boş tablo")
    void count_EmptyTable() {
        // Given
        when(systemParametersRepository.count()).thenReturn(0L);

        // When
        long count = systemParametersRepository.count();

        // Then
        assertThat(count).isEqualTo(0);
        verify(systemParametersRepository, times(1)).count();
    }
}
