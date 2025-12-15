package com.example.finchange.user.controller;

import com.example.finchange.user.dto.InviteUserRequestDto;
import com.example.finchange.user.dto.UpdateUserStatusRequestDto;
import com.example.finchange.user.dto.UpdateUserInfoRequestDto;
import com.example.finchange.user.dto.UserResponseDto;
import com.example.finchange.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.finchange.user.dto.UpdateUserRolesRequestDto;
import com.example.finchange.common.model.dto.response.SuccessResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;

    @PostMapping("/invite")
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<SuccessResponse<UserResponseDto>> inviteUser(@Valid @RequestBody InviteUserRequestDto requestDto) {
        UserResponseDto createdUser = userService.inviteUser(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success(createdUser, "Personel başarıyla davet edildi."));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read:all')")
    public ResponseEntity<SuccessResponse<List<UserResponseDto>>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String personnelCode,
            @RequestParam(required = false) String isActive 
    ) {
        Boolean isActiveBoolean = null;
        if (isActive != null && !isActive.isBlank()) {
            if ("true".equalsIgnoreCase(isActive)) {
                isActiveBoolean = true;
            } else if ("false".equalsIgnoreCase(isActive)) {
                isActiveBoolean = false;
            }
        }

        List<UserResponseDto> users = userService.getAllUsers(name, email, personnelCode, isActiveBoolean);
        return ResponseEntity.ok(SuccessResponse.success(users));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user:update:role')")
    public ResponseEntity<SuccessResponse<UserResponseDto>> updateUserRoles(@PathVariable Integer userId, @Valid @RequestBody UpdateUserRolesRequestDto requestDto) {
        UserResponseDto updatedUser = userService.updateUserRoles(userId, requestDto);
        return ResponseEntity.ok(SuccessResponse.success(updatedUser, "Personel rolleri güncellendi."));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('user:update:status')")
    public ResponseEntity<SuccessResponse<Void>> updateUserStatus(@PathVariable Integer userId, @Valid @RequestBody UpdateUserStatusRequestDto requestDto) {
        userService.updateUserStatus(userId, requestDto.isActive());
        return ResponseEntity.ok(SuccessResponse.success("Personel durumu güncellendi."));
    }

    @PutMapping("/{userId}/info")
    @PreAuthorize("hasAuthority('user:update:role')")
    public ResponseEntity<SuccessResponse<UserResponseDto>> updateUserInfo(@PathVariable Integer userId, @Valid @RequestBody UpdateUserInfoRequestDto requestDto) {

        UserResponseDto updatedUser = userService.updateUserInfo(userId, requestDto);
        return ResponseEntity.ok(SuccessResponse.success(updatedUser, "Personel bilgileri güncellendi."));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:read:all')")
    public ResponseEntity<SuccessResponse<UserResponseDto>> getUserById(@PathVariable Integer userId) {
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(SuccessResponse.success(user));
    }
}