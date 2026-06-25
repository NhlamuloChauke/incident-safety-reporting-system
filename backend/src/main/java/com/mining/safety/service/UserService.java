package com.mining.safety.service;

import com.mining.safety.dto.UserRequest;
import com.mining.safety.dto.UserResponse;
import com.mining.safety.entity.User;
import com.mining.safety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already in use: " + request.getEmail());
        if (userRepository.existsByEmployeeNumber(request.getEmployeeNumber()))
            throw new RuntimeException("Employee number already in use: " + request.getEmployeeNumber());

        String rawPassword = request.getPassword() != null && !request.getPassword().isBlank()
                ? request.getPassword() : "SafeMine@2024";

        User user = User.builder()
                .fullName(request.getFullName())
                .employeeNumber(request.getEmployeeNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .section(request.getSection())
                .active(request.isActive())
                .build();

        return toResponse(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmployeeNumber(request.getEmployeeNumber());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDepartment(request.getDepartment());
        user.setSection(request.getSection());
        user.setActive(request.isActive());

        if (request.getPassword() != null && !request.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(userRepository.save(user));
    }

    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .employeeNumber(u.getEmployeeNumber())
                .email(u.getEmail())
                .role(u.getRole())
                .phoneNumber(u.getPhoneNumber())
                .department(u.getDepartment())
                .section(u.getSection())
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
