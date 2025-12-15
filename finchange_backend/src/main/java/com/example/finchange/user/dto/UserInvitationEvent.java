package com.example.finchange.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInvitationEvent {
    private String email;
    private String firstName;
    private String temporaryPassword;
}