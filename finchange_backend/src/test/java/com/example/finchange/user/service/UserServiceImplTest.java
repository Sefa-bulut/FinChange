package com.example.finchange.user.service;

import com.example.finchange.user.dto.InviteUserRequestDto;
import com.example.finchange.user.dto.UpdateUserInfoRequestDto;
import com.example.finchange.user.dto.UpdateUserRolesRequestDto;
import com.example.finchange.user.dto.UserInvitationEvent;
import com.example.finchange.user.dto.UserResponseDto;
import com.example.finchange.user.exception.PersonnelCodeAlreadyExistsException;
import com.example.finchange.user.exception.RoleNotFoundException;
import com.example.finchange.user.exception.UserAlreadyExistsException;
import com.example.finchange.user.exception.UserNotFoundException;
import com.example.finchange.user.model.Role;
import com.example.finchange.user.model.User;
import com.example.finchange.user.repository.RoleRepository;
import com.example.finchange.user.repository.UserRepository;
import com.example.finchange.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, UserInvitationEvent> kafkaTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role testRole;
    private InviteUserRequestDto inviteUserRequest;
    private UpdateUserRolesRequestDto updateUserRolesRequest;
    private UpdateUserInfoRequestDto updateUserInfoRequest;

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

        inviteUserRequest = new InviteUserRequestDto();
        inviteUserRequest.setFirstName("New");
        inviteUserRequest.setLastName("User");
        inviteUserRequest.setEmail("newuser@example.com");
        inviteUserRequest.setPersonnelCode("EMP002");
        inviteUserRequest.setRoleNames(List.of("USER"));

        updateUserRolesRequest = new UpdateUserRolesRequestDto();
        updateUserRolesRequest.setRoleNames(List.of("ADMIN"));

        updateUserInfoRequest = new UpdateUserInfoRequestDto();
        updateUserInfoRequest.setFirstName("Updated");
        updateUserInfoRequest.setLastName("User");
        updateUserInfoRequest.setPersonnelCode("EMP003");
    }

    @Test
    @DisplayName("inviteUser - Başarılı kullanıcı davet etme")
    void inviteUser_Success() {
        // Given
        when(userRepository.existsByEmail(inviteUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPersonnelCode(inviteUserRequest.getPersonnelCode())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDto result = userService.inviteUser(inviteUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPersonnelCode()).isEqualTo("EMP001");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getRoles()).contains("USER");

        verify(userRepository, times(1)).existsByEmail(inviteUserRequest.getEmail());
        verify(userRepository, times(1)).existsByPersonnelCode(inviteUserRequest.getPersonnelCode());
        verify(roleRepository, times(1)).findByName("USER");
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(kafkaTemplate, times(1)).send(eq("user-invitation-events"), any(UserInvitationEvent.class));
    }

    @Test
    @DisplayName("inviteUser - Email zaten mevcut hatası")
    void inviteUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(inviteUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.inviteUser(inviteUserRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Bu e-posta adresi zaten kullanımda");

        verify(userRepository, times(1)).existsByEmail(inviteUserRequest.getEmail());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("inviteUser - Personel kodu zaten mevcut hatası")
    void inviteUser_PersonnelCodeAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(inviteUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPersonnelCode(inviteUserRequest.getPersonnelCode())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.inviteUser(inviteUserRequest))
                .isInstanceOf(PersonnelCodeAlreadyExistsException.class)
                .hasMessageContaining("Bu personel kodu zaten kullanımda");

        verify(userRepository, times(1)).existsByEmail(inviteUserRequest.getEmail());
        verify(userRepository, times(1)).existsByPersonnelCode(inviteUserRequest.getPersonnelCode());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("inviteUser - Rol bulunamadı hatası")
    void inviteUser_RoleNotFound() {
        // Given
        when(userRepository.existsByEmail(inviteUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPersonnelCode(inviteUserRequest.getPersonnelCode())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.inviteUser(inviteUserRequest))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("Rol bulunamadı");

        verify(userRepository, times(1)).existsByEmail(inviteUserRequest.getEmail());
        verify(userRepository, times(1)).existsByPersonnelCode(inviteUserRequest.getPersonnelCode());
        verify(roleRepository, times(1)).findByName("USER");
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("inviteUser - Boş rol listesi hatası")
    void inviteUser_EmptyRoles() {
        // Given
        InviteUserRequestDto requestWithEmptyRoles = new InviteUserRequestDto();
        requestWithEmptyRoles.setFirstName("New");
        requestWithEmptyRoles.setLastName("User");
        requestWithEmptyRoles.setEmail("newuser@example.com");
        requestWithEmptyRoles.setPersonnelCode("EMP002");
        requestWithEmptyRoles.setRoleNames(List.of());

        when(userRepository.existsByEmail(requestWithEmptyRoles.getEmail())).thenReturn(false);
        when(userRepository.existsByPersonnelCode(requestWithEmptyRoles.getPersonnelCode())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.inviteUser(requestWithEmptyRoles))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("Geçerli bir rol bulunamadı");

        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("getAllUsers - Başarılı kullanıcı listesi getirme")
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        // When
        List<UserResponseDto> result = userService.getAllUsers("Test", "test@example.com", "EMP001", true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Test");
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.get(0).getPersonnelCode()).isEqualTo("EMP001");

        verify(userRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getAllUsers - Boş liste")
    void getAllUsers_EmptyList() {
        // Given
        when(userRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        List<UserResponseDto> result = userService.getAllUsers(null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("updateUserRoles - Başarılı rol güncelleme")
    void updateUserRoles_Success() {
        // Given
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDto result = userService.updateUserRoles(1, updateUserRolesRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);

        verify(userRepository, times(1)).findById(1);
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRoles - Kullanıcı bulunamadı hatası")
    void updateUserRoles_UserNotFound() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserRoles(999, updateUserRolesRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Kullanıcı bulunamadı");

        verify(userRepository, times(1)).findById(999);
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserRoles - Rol bulunamadı hatası")
    void updateUserRoles_RoleNotFound() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserRoles(1, updateUserRolesRequest))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("Rol bulunamadı");

        verify(userRepository, times(1)).findById(1);
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById - Başarılı kullanıcı getirme")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        UserResponseDto result = userService.getUserById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getUserById - Kullanıcı bulunamadı hatası")
    void getUserById_UserNotFound() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Kullanıcı bulunamadı");

        verify(userRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("updateUserStatus - Başarılı durum güncelleme")
    void updateUserStatus_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserStatus(1, false);

        // Then
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserStatus - Kullanıcı bulunamadı hatası")
    void updateUserStatus_UserNotFound() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserStatus(999, false))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Kullanıcı bulunamadı");

        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserInfo - Başarılı bilgi güncelleme")
    void updateUserInfo_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPersonnelCode("EMP003")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDto result = userService.updateUserInfo(1, updateUserInfoRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).existsByPersonnelCode("EMP003");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserInfo - Kullanıcı bulunamadı hatası")
    void updateUserInfo_UserNotFound() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserInfo(999, updateUserInfoRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Kullanıcı bulunamadı");

        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserInfo - Personel kodu zaten mevcut hatası")
    void updateUserInfo_PersonnelCodeAlreadyExists() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPersonnelCode("EMP003")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUserInfo(1, updateUserInfoRequest))
                .isInstanceOf(PersonnelCodeAlreadyExistsException.class)
                .hasMessageContaining("Bu personel kodu zaten kullanımda");

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).existsByPersonnelCode("EMP003");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserInfo - Aynı personel kodu ile güncelleme")
    void updateUserInfo_SamePersonnelCode() {
        // Given
        updateUserInfoRequest.setPersonnelCode("EMP001"); // Aynı personel kodu
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDto result = userService.updateUserInfo(1, updateUserInfoRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, never()).existsByPersonnelCode(anyString()); // Aynı kod olduğu için kontrol edilmez
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Kafka mesaj gönderme hatası - kullanıcı kaydı başarılı")
    void inviteUser_KafkaError_UserStillSaved() {
        // Given
        when(userRepository.existsByEmail(inviteUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPersonnelCode(inviteUserRequest.getPersonnelCode())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(kafkaTemplate.send(anyString(), any(UserInvitationEvent.class)))
                .thenThrow(new RuntimeException("Kafka connection error"));

        // When
        UserResponseDto result = userService.inviteUser(inviteUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Test");

        verify(userRepository, times(1)).save(any(User.class));
        verify(kafkaTemplate, times(1)).send(eq("user-invitation-events"), any(UserInvitationEvent.class));
    }
}
