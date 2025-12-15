package com.example.finchange.user.repository;

import com.example.finchange.user.model.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PermissionRepository için unit testler
 * Mockito kullanarak repository davranışını test eder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionRepository Unit Tests")
class PermissionRepositoryTest {

    @Mock
    private PermissionRepository permissionRepository;

    private Permission testPermission;
    private Permission readPermission;
    private Permission writePermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId(1);
        testPermission.setName("READ_USERS");
        testPermission.setDescription("Read users permission");

        readPermission = new Permission();
        readPermission.setId(2);
        readPermission.setName("READ_DATA");
        readPermission.setDescription("Read data permission");

        writePermission = new Permission();
        writePermission.setId(3);
        writePermission.setName("WRITE_DATA");
        writePermission.setDescription("Write data permission");
    }

    @Test
    @DisplayName("save - Yeni permission kaydetme")
    void save_NewPermission_Success() {
        // Given
        Permission newPermission = new Permission();
        newPermission.setName("DELETE_USERS");
        newPermission.setDescription("Delete users permission");

        Permission savedPermission = new Permission();
        savedPermission.setId(4);
        savedPermission.setName("DELETE_USERS");
        savedPermission.setDescription("Delete users permission");

        when(permissionRepository.save(newPermission)).thenReturn(savedPermission);

        // When
        Permission result = permissionRepository.save(newPermission);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("DELETE_USERS");
        assertThat(result.getDescription()).isEqualTo("Delete users permission");
        verify(permissionRepository, times(1)).save(newPermission);
    }

    @Test
    @DisplayName("findById - ID ile permission bulma")
    void findById_Success() {
        // Given
        when(permissionRepository.findById(1)).thenReturn(Optional.of(testPermission));

        // When
        Optional<Permission> result = permissionRepository.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("READ_USERS");
        assertThat(result.get().getDescription()).isEqualTo("Read users permission");
        verify(permissionRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById - ID ile permission bulunamadı")
    void findById_NotFound() {
        // Given
        when(permissionRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Permission> result = permissionRepository.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(permissionRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("findAll - Tüm permissionları getir")
    void findAll_Success() {
        // Given
        List<Permission> permissions = Arrays.asList(testPermission, readPermission, writePermission);
        when(permissionRepository.findAll()).thenReturn(permissions);

        // When
        List<Permission> result = permissionRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Permission::getName)
                .contains("READ_USERS", "READ_DATA", "WRITE_DATA");
        verify(permissionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll - Boş liste")
    void findAll_EmptyList() {
        // Given
        when(permissionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Permission> result = permissionRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(permissionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("existsById - ID ile permission varlığı kontrolü")
    void existsById_True() {
        // Given
        when(permissionRepository.existsById(1)).thenReturn(true);

        // When
        boolean exists = permissionRepository.existsById(1);

        // Then
        assertThat(exists).isTrue();
        verify(permissionRepository, times(1)).existsById(1);
    }

    @Test
    @DisplayName("existsById - ID ile permission varlığı kontrolü - bulunamadı")
    void existsById_False() {
        // Given
        when(permissionRepository.existsById(999)).thenReturn(false);

        // When
        boolean exists = permissionRepository.existsById(999);

        // Then
        assertThat(exists).isFalse();
        verify(permissionRepository, times(1)).existsById(999);
    }

    @Test
    @DisplayName("deleteById - ID ile permission silme")
    void deleteById_Success() {
        // Given
        doNothing().when(permissionRepository).deleteById(1);
        when(permissionRepository.findById(1)).thenReturn(Optional.empty());

        // When
        permissionRepository.deleteById(1);

        // Then
        verify(permissionRepository, times(1)).deleteById(1);
        
        // Silinme sonrası kontrol
        Optional<Permission> deleted = permissionRepository.findById(1);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("save - Mevcut permissioni güncelleme")
    void save_UpdateExistingPermission_Success() {
        // Given
        testPermission.setName("UPDATED_READ_USERS");
        testPermission.setDescription("Updated read users permission");
        
        Permission updatedPermission = new Permission();
        updatedPermission.setId(1);
        updatedPermission.setName("UPDATED_READ_USERS");
        updatedPermission.setDescription("Updated read users permission");

        when(permissionRepository.save(testPermission)).thenReturn(updatedPermission);

        // When
        Permission result = permissionRepository.save(testPermission);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("UPDATED_READ_USERS");
        assertThat(result.getDescription()).isEqualTo("Updated read users permission");
        verify(permissionRepository, times(1)).save(testPermission);
    }

    @Test
    @DisplayName("count - Toplam permission sayısı")
    void count_Success() {
        // Given
        when(permissionRepository.count()).thenReturn(5L);

        // When
        long count = permissionRepository.count();

        // Then
        assertThat(count).isEqualTo(5);
        verify(permissionRepository, times(1)).count();
    }

    @Test
    @DisplayName("count - Boş tablo")
    void count_EmptyTable() {
        // Given
        when(permissionRepository.count()).thenReturn(0L);

        // When
        long count = permissionRepository.count();

        // Then
        assertThat(count).isEqualTo(0);
        verify(permissionRepository, times(1)).count();
    }

    @Test
    @DisplayName("Birden fazla permission kaydetme")
    void save_MultiplePermissions() {
        // Given
        when(permissionRepository.save(readPermission)).thenReturn(readPermission);
        when(permissionRepository.save(writePermission)).thenReturn(writePermission);

        // When
        Permission savedRead = permissionRepository.save(readPermission);
        Permission savedWrite = permissionRepository.save(writePermission);

        // Then
        assertThat(savedRead).isNotNull();
        assertThat(savedRead.getName()).isEqualTo("READ_DATA");
        assertThat(savedWrite).isNotNull();
        assertThat(savedWrite.getName()).isEqualTo("WRITE_DATA");
        
        verify(permissionRepository, times(1)).save(readPermission);
        verify(permissionRepository, times(1)).save(writePermission);
    }

    @Test
    @DisplayName("Null değerler ile permission kaydetme")
    void save_WithNullValues() {
        // Given
        Permission nullPermission = new Permission();
        nullPermission.setName(null);
        nullPermission.setDescription(null);

        Permission savedNullPermission = new Permission();
        savedNullPermission.setId(5);
        savedNullPermission.setName(null);
        savedNullPermission.setDescription(null);

        when(permissionRepository.save(nullPermission)).thenReturn(savedNullPermission);

        // When
        Permission result = permissionRepository.save(nullPermission);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getName()).isNull();
        assertThat(result.getDescription()).isNull();
        verify(permissionRepository, times(1)).save(nullPermission);
    }

    @Test
    @DisplayName("Boş string değerler ile permission kaydetme")
    void save_WithEmptyValues() {
        // Given
        Permission emptyPermission = new Permission();
        emptyPermission.setName("");
        emptyPermission.setDescription("");

        Permission savedEmptyPermission = new Permission();
        savedEmptyPermission.setId(6);
        savedEmptyPermission.setName("");
        savedEmptyPermission.setDescription("");

        when(permissionRepository.save(emptyPermission)).thenReturn(savedEmptyPermission);

        // When
        Permission result = permissionRepository.save(emptyPermission);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(6);
        assertThat(result.getName()).isEmpty();
        assertThat(result.getDescription()).isEmpty();
        verify(permissionRepository, times(1)).save(emptyPermission);
    }
}
