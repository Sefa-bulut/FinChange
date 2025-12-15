package com.example.finchange.user.service.impl;

import com.example.finchange.user.dto.UserInvitationEvent;
import com.example.finchange.user.exception.RoleNotFoundException;
import com.example.finchange.user.exception.UserAlreadyExistsException;
import com.example.finchange.user.exception.UserNotFoundException; 
import com.example.finchange.user.model.Role;
import com.example.finchange.user.model.User;
import com.example.finchange.user.dto.InviteUserRequestDto;
import com.example.finchange.user.dto.UpdateUserInfoRequestDto;
import com.example.finchange.user.dto.UserResponseDto;
import com.example.finchange.user.repository.RoleRepository;
import com.example.finchange.user.repository.UserRepository;
import com.example.finchange.user.exception.PersonnelCodeAlreadyExistsException;
import com.example.finchange.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.finchange.user.dto.UpdateUserRolesRequestDto;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final KafkaTemplate<String, UserInvitationEvent> kafkaTemplate;

    @Override
    @Transactional
    public UserResponseDto inviteUser(InviteUserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyExistsException("Bu e-posta adresi zaten kullanımda: " + requestDto.getEmail());
        }
        if (userRepository.existsByPersonnelCode(requestDto.getPersonnelCode())) {
            throw new PersonnelCodeAlreadyExistsException("Bu personel kodu zaten kullanımda: " + requestDto.getPersonnelCode());
        }

        Set<Role> roles = requestDto.getRoleNames().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RoleNotFoundException("Rol bulunamadı: " + roleName)))
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            throw new RoleNotFoundException("Geçerli bir rol bulunamadı.");
        }

        String temporaryPassword = String.format("%010d", secureRandom.nextInt(999999));

        User newUser = new User();
        newUser.setFirstName(requestDto.getFirstName());
        newUser.setLastName(requestDto.getLastName());
        newUser.setEmail(requestDto.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        newUser.setRoles(roles);
        newUser.setPersonnelCode(requestDto.getPersonnelCode());
        newUser.setActive(true);

        User savedUser = userRepository.save(newUser);

        
        UserInvitationEvent event = new UserInvitationEvent(
            savedUser.getEmail(),
            savedUser.getFirstName(),
            temporaryPassword 
        );

        

        try {
            
            kafkaTemplate.send("user-invitation-events", event);
            log.info("Kullanıcı daveti Kafka'ya başarıyla gönderildi: {}", event.getEmail());
        } catch (Exception e) {
            
            log.error("Kafka'ya davet  gönderilirken hata oluştu, ancak kullanıcı kaydı başarılı: {}", event.getEmail(), e);
        }

        return toUserResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers(String name, String email, String personnelCode, Boolean isActive) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + name.toLowerCase() + "%");
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + name.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }

            if (email != null && !email.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }


            if (personnelCode != null && !personnelCode.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("personnelCode")), "%" + personnelCode.toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec).stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDto updateUserRoles(Integer userId, UpdateUserRolesRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        Set<Role> newRoles = requestDto.getRoleNames().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RoleNotFoundException("Rol bulunamadı: " + roleName)))
                .collect(Collectors.toSet());

        if (newRoles.isEmpty()) {
            throw new RoleNotFoundException("Güncelleme için geçerli bir rol bulunamadı.");
        }

        user.setRoles(newRoles);
        User updatedUser = userRepository.save(user);

        return toUserResponseDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        return toUserResponseDto(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Integer userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        user.setActive(isActive);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserInfo(Integer userId, UpdateUserInfoRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        if (!user.getPersonnelCode().equals(requestDto.getPersonnelCode()) && 
            userRepository.existsByPersonnelCode(requestDto.getPersonnelCode())) {
            throw new PersonnelCodeAlreadyExistsException("Bu personel kodu zaten kullanımda: " + requestDto.getPersonnelCode());
        }

        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setPersonnelCode(requestDto.getPersonnelCode());

        User savedUser = userRepository.save(user);
        return toUserResponseDto(savedUser);
    }

    private UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isActive(user.isActive())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .personnelCode(user.getPersonnelCode())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    }

