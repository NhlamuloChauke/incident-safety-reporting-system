package com.mining.safety.dto;

import com.mining.safety.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank  private String fullName;
    @NotBlank  private String employeeNumber;
    @NotBlank @Email private String email;
    private String password;
    @NotNull   private Role role;
    private String phoneNumber;
    private String department;
    private String section;
    private boolean active = true;
}
