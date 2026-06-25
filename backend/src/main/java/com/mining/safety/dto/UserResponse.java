package com.mining.safety.dto;

import com.mining.safety.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String employeeNumber;
    private String email;
    private Role role;
    private String phoneNumber;
    private String department;
    private String section;
    private boolean active;
    private LocalDateTime createdAt;
}
