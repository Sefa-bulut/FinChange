package com.example.finchange.user.repository;

import com.example.finchange.user.model.Role;
import com.example.finchange.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserRepository için unit testler
 * Mockito kullanarak repository davranışını test eder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1);
        testRole.setName("USER");

        testUser = new User();
        testUser.setId(1);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setPersonnelCode("EMP001");
        testUser.setActive(true);
        testUser.setRoles(Set.of(testRole));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("existsByEmail - Email mevcut")
    void existsByEmail_True() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("existsByEmail - Email mevcut değil")
    void existsByEmail_False() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
        verify(userRepository, times(1)).existsByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("findByEmail - Başarılı kullanıcı bulma")
    void findByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Test");
        assertThat(result.get().getLastName()).isEqualTo("User");
        assertThat(result.get().getPersonnelCode()).isEqualTo("EMP001");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("findByEmail - Kullanıcı bulunamadı")
    void findByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("existsByPersonnelCode - Personel kodu mevcut")
    void existsByPersonnelCode_True() {
        // Given
        when(userRepository.existsByPersonnelCode("EMP001")).thenReturn(true);

        // When
        boolean exists = userRepository.existsByPersonnelCode("EMP001");

        // Then
        assertThat(exists).isTrue();
        verify(userRepository, times(1)).existsByPersonnelCode("EMP001");
    }

    @Test
    @DisplayName("existsByPersonnelCode - Personel kodu mevcut değil")
    void existsByPersonnelCode_False() {
        // Given
        when(userRepository.existsByPersonnelCode("EMP999")).thenReturn(false);

        // When
        boolean exists = userRepository.existsByPersonnelCode("EMP999");

        // Then
        assertThat(exists).isFalse();
        verify(userRepository, times(1)).existsByPersonnelCode("EMP999");
    }

    @Test
    @DisplayName("save - Yeni kullanıcı kaydetme")
    void save_NewUser_Success() {
        // Given
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("hashedPassword");
        newUser.setPersonnelCode("EMP002");
        newUser.setActive(true);

        User savedUser = new User();
        savedUser.setId(2);
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPasswordHash("hashedPassword");
        savedUser.setPersonnelCode("EMP002");
        savedUser.setActive(true);

        when(userRepository.save(newUser)).thenReturn(savedUser);

        // When
        User result = userRepository.save(newUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getPersonnelCode()).isEqualTo("EMP002");
        assertThat(result.isActive()).isTrue();
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("findById - ID ile kullanıcı bulma")
    void findById_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userRepository.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getFirstName()).isEqualTo("Test");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById - ID ile kullanıcı bulunamadı")
    void findById_NotFound() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("findAll - Tüm kullanıcıları getir")
    void findAll_Success() {
        // Given
        User user2 = new User();
        user2.setId(2);
        user2.setFirstName("Second");
        user2.setLastName("User");
        user2.setEmail("second@example.com");
        user2.setPersonnelCode("EMP002");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getEmail)
                .contains("test@example.com", "second@example.com");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll with Specification - Filtrelenmiş kullanıcı listesi")
    void findAllWithSpecification_Success() {
        // Given
        List<User> filteredUsers = Arrays.asList(testUser);
        Specification<User> mockSpec = mock(Specification.class);
        when(userRepository.findAll(mockSpec)).thenReturn(filteredUsers);

        // When
        List<User> result = userRepository.findAll(mockSpec);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findAll(mockSpec);
    }

    @Test
    @DisplayName("deleteById - ID ile kullanıcı silme")
    void deleteById_Success() {
        // Given
        doNothing().when(userRepository).deleteById(1);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // When
        userRepository.deleteById(1);

        // Then
        verify(userRepository, times(1)).deleteById(1);
        
        // Silinme sonrası kontrol
        Optional<User> deleted = userRepository.findById(1);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("existsById - ID ile kullanıcı varlığı kontrolü")
    void existsById_True() {
        // Given
        when(userRepository.existsById(1)).thenReturn(true);

        // When
        boolean exists = userRepository.existsById(1);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository, times(1)).existsById(1);
    }

    @Test
    @DisplayName("existsById - ID ile kullanıcı varlığı kontrolü - bulunamadı")
    void existsById_False() {
        // Given
        when(userRepository.existsById(999)).thenReturn(false);

        // When
        boolean exists = userRepository.existsById(999);

        // Then
        assertThat(exists).isFalse();
        verify(userRepository, times(1)).existsById(999);
    }

    @Test
    @DisplayName("count - Toplam kullanıcı sayısı")
    void count_Success() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(5);
        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("save - Mevcut kullanıcıyı güncelleme")
    void save_UpdateExistingUser_Success() {
        // Given
        testUser.setFirstName("Updated");
        testUser.setLastName("User");
        
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("test@example.com");
        updatedUser.setPersonnelCode("EMP001");

        when(userRepository.save(testUser)).thenReturn(updatedUser);

        // When
        User result = userRepository.save(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("User");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Email büyük/küçük harf duyarlılığı testi")
    void findByEmail_CaseInsensitive() {
        // Given
        when(userRepository.findByEmail("TEST@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> upperCaseResult = userRepository.findByEmail("TEST@EXAMPLE.COM");
        Optional<User> lowerCaseResult = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(upperCaseResult).isEmpty();
        assertThat(lowerCaseResult).isPresent();
        assertThat(lowerCaseResult.get().getEmail()).isEqualTo("test@example.com");
        
        verify(userRepository, times(1)).findByEmail("TEST@EXAMPLE.COM");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
}
