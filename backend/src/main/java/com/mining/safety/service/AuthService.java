package com.mining.safety.service;

import com.mining.safety.dto.LoginRequest;
import com.mining.safety.dto.LoginResponse;
import com.mining.safety.entity.User;
import com.mining.safety.enums.Role;
import com.mining.safety.repository.UserRepository;
import com.mining.safety.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new DisabledException("Account is disabled");
        }

        String token = jwtTokenProvider.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Bean
    public CommandLineRunner seedAdminUser() {
        return args -> {
            if (!userRepository.existsByEmail("admin@safemine.co.za")) {
                User admin = User.builder()
                        .fullName("System Administrator")
                        .employeeNumber("EMP001")
                        .email("admin@safemine.co.za")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .department("Safety")
                        .active(true)
                        .build();
                userRepository.save(admin);

                User safetyOfficer = User.builder()
                        .fullName("John Dlamini")
                        .employeeNumber("EMP002")
                        .email("safety@safemine.co.za")
                        .password(passwordEncoder.encode("Safety@123"))
                        .role(Role.SAFETY_OFFICER)
                        .department("Safety & Health")
                        .section("Underground")
                        .active(true)
                        .build();
                userRepository.save(safetyOfficer);

                User worker = User.builder()
                        .fullName("Sipho Nkosi")
                        .employeeNumber("EMP003")
                        .email("worker@safemine.co.za")
                        .password(passwordEncoder.encode("Worker@123"))
                        .role(Role.WORKER)
                        .department("Mining")
                        .section("Level 5")
                        .active(true)
                        .build();
                userRepository.save(worker);

                System.out.println("========================================");
                System.out.println("SAFEMINE - Default users created:");
                System.out.println("Admin:   admin@safemine.co.za / Admin@123");
                System.out.println("Safety:  safety@safemine.co.za / Safety@123");
                System.out.println("Worker:  worker@safemine.co.za / Worker@123");
                System.out.println("========================================");
            }
        };
    }
}
