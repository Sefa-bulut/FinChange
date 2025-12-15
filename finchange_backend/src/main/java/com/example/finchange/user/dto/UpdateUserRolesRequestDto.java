package com.example.finchange.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class UpdateUserRolesRequestDto {

    @NotEmpty(message = "En az bir rol seçilmelidir")
    private List<String> roleNames;
}