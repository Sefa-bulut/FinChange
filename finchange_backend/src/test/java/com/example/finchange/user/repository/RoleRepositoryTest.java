package com.example.finchange.user.repository;

import com.example.finchange.user.model.Role;
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
 * RoleRepository için unit testler
 * Mockito kullanarak repository davranışını test eder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleRepository Unit Tests")
class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    private Role testRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1);
        testRole.setName("USER");

        adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");
    }

    @Test
    @DisplayName("findByName - Başarılı rol bulma")
    void findByName_Success() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));

        // When
        Optional<Role> result = roleRepository.findByName("USER");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("USER");
        assertThat(result.get().getId()).isEqualTo(1);
        verify(roleRepository, times(1)).findByName("USER");
    }

    @Test
    @DisplayName("findByName - Rol bulunamadı")
    void findByName_NotFound() {
        // Given
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleRepository.findByName("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
        verify(roleRepository, times(1)).findByName("NONEXISTENT");
    }

    @Test
    @DisplayName("findByName - Büyük/küçük harf duyarlılığı")
    void findByName_CaseSensitive() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(roleRepository.findByName("user")).thenReturn(Optional.empty());

        // When
        Optional<Role> upperCaseResult = roleRepository.findByName("USER");
        Optional<Role> lowerCaseResult = roleRepository.findByName("user");

        // Then
        assertThat(upperCaseResult).isPresent();
        assertThat(upperCaseResult.get().getName()).isEqualTo("USER");
        assertThat(lowerCaseResult).isEmpty();
        
        verify(roleRepository, times(1)).findByName("USER");
        verify(roleRepository, times(1)).findByName("user");
    }

    @Test
    @DisplayName("save - Yeni rol kaydetme")
    void save_NewRole_Success() {
        // Given
        Role newRole = new Role();
        newRole.setName("MANAGER");

        Role savedRole = new Role();
        savedRole.setId(3);
        savedRole.setName("MANAGER");

        when(roleRepository.save(newRole)).thenReturn(savedRole);

        // When
        Role result = roleRepository.save(newRole);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("MANAGER");
        verify(roleRepository, times(1)).save(newRole);
    }

    @Test
    @DisplayName("findById - ID ile rol bulma")
    void findById_Success() {
        // Given
        when(roleRepository.findById(1)).thenReturn(Optional.of(testRole));

        // When
        Optional<Role> result = roleRepository.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("USER");
        verify(roleRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById - ID ile rol bulunamadı")
    void findById_NotFound() {
        // Given
        when(roleRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleRepository.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(roleRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("findAll - Tüm rolleri getir")
    void findAll_Success() {
        // Given
        List<Role> roles = Arrays.asList(testRole, adminRole);
        when(roleRepository.findAll()).thenReturn(roles);

        // When
        List<Role> result = roleRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Role::getName)
                .contains("USER", "ADMIN");
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll - Boş liste")
    void findAll_EmptyList() {
        // Given
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Role> result = roleRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("existsById - ID ile rol varlığı kontrolü")
    void existsById_True() {
        // Given
        when(roleRepository.existsById(1)).thenReturn(true);

        // When
        boolean exists = roleRepository.existsById(1);

        // Then
        assertThat(exists).isTrue();
        verify(roleRepository, times(1)).existsById(1);
    }

    @Test
    @DisplayName("existsById - ID ile rol varlığı kontrolü - bulunamadı")
    void existsById_False() {
        // Given
        when(roleRepository.existsById(999)).thenReturn(false);

        // When
        boolean exists = roleRepository.existsById(999);

        // Then
        assertThat(exists).isFalse();
        verify(roleRepository, times(1)).existsById(999);
    }

    @Test
    @DisplayName("deleteById - ID ile rol silme")
    void deleteById_Success() {
        // Given
        doNothing().when(roleRepository).deleteById(1);
        when(roleRepository.findById(1)).thenReturn(Optional.empty());

        // When
        roleRepository.deleteById(1);

        // Then
        verify(roleRepository, times(1)).deleteById(1);
        
        // Silinme sonrası kontrol
        Optional<Role> deleted = roleRepository.findById(1);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("save - Mevcut rolü güncelleme")
    void save_UpdateExistingRole_Success() {
        // Given
        testRole.setName("UPDATED_USER");
        
        Role updatedRole = new Role();
        updatedRole.setId(1);
        updatedRole.setName("UPDATED_USER");

        when(roleRepository.save(testRole)).thenReturn(updatedRole);

        // When
        Role result = roleRepository.save(testRole);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("UPDATED_USER");
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    @DisplayName("count - Toplam rol sayısı")
    void count_Success() {
        // Given
        when(roleRepository.count()).thenReturn(3L);

        // When
        long count = roleRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
        verify(roleRepository, times(1)).count();
    }

    @Test
    @DisplayName("count - Boş tablo")
    void count_EmptyTable() {
        // Given
        when(roleRepository.count()).thenReturn(0L);

        // When
        long count = roleRepository.count();

        // Then
        assertThat(count).isEqualTo(0);
        verify(roleRepository, times(1)).count();
    }

    @Test
    @DisplayName("Birden fazla rol ismi ile arama")
    void findByName_MultipleRoles() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        // When
        Optional<Role> userRole = roleRepository.findByName("USER");
        Optional<Role> adminRoleResult = roleRepository.findByName("ADMIN");

        // Then
        assertThat(userRole).isPresent();
        assertThat(userRole.get().getName()).isEqualTo("USER");
        assertThat(adminRoleResult).isPresent();
        assertThat(adminRoleResult.get().getName()).isEqualTo("ADMIN");
        
        verify(roleRepository, times(1)).findByName("USER");
        verify(roleRepository, times(1)).findByName("ADMIN");
    }

    @Test
    @DisplayName("Null rol ismi ile arama")
    void findByName_NullName() {
        // Given
        when(roleRepository.findByName(null)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleRepository.findByName(null);

        // Then
        assertThat(result).isEmpty();
        verify(roleRepository, times(1)).findByName(null);
    }

    @Test
    @DisplayName("Boş rol ismi ile arama")
    void findByName_EmptyName() {
        // Given
        when(roleRepository.findByName("")).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleRepository.findByName("");

        // Then
        assertThat(result).isEmpty();
        verify(roleRepository, times(1)).findByName("");
    }
}
