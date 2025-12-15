package com.example.finchange.user.service;

import com.example.finchange.user.dto.InviteUserRequestDto;
import com.example.finchange.user.dto.UpdateUserRolesRequestDto;
import com.example.finchange.user.dto.UpdateUserInfoRequestDto;
import com.example.finchange.user.dto.UserResponseDto;
import java.util.List;

public interface UserService {

    UserResponseDto inviteUser(InviteUserRequestDto requestDto);

    UserResponseDto updateUserRoles(Integer userId, UpdateUserRolesRequestDto requestDto);

    void updateUserStatus(Integer userId, boolean isActive);
    
    UserResponseDto updateUserInfo(Integer userId, UpdateUserInfoRequestDto requestDto);

    List<UserResponseDto> getAllUsers(String name, String email, String personnelCode, Boolean isActive);

    UserResponseDto getUserById(Integer userId);
}