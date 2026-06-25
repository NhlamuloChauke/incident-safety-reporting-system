package com.mining.safety.repository;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.entity.User;
import com.mining.safety.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Repository Tests")
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find by email returns seeded admin user")
    void findByEmail_seededAdmin_returnsUser() {
        Optional<User> user = userRepository.findByEmail("admin@safemine.co.za");
        assertThat(user).isPresent();
        assertThat(user.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Find by email returns empty for unknown email")
    void findByEmail_unknownEmail_returnsEmpty() {
        Optional<User> user = userRepository.findByEmail("unknown@mine.co.za");
        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Find by employee number returns correct user")
    void findByEmployeeNumber_validEmpNo_returnsUser() {
        Optional<User> user = userRepository.findByEmployeeNumber("EMP001");
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("admin@safemine.co.za");
    }

    @Test
    @DisplayName("existsByEmail returns true for existing email")
    void existsByEmail_existingEmail_returnsTrue() {
        assertThat(userRepository.existsByEmail("admin@safemine.co.za")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail returns false for non-existing email")
    void existsByEmail_nonExistingEmail_returnsFalse() {
        assertThat(userRepository.existsByEmail("ghost@mine.co.za")).isFalse();
    }

    @Test
    @DisplayName("existsByEmployeeNumber returns true for existing emp number")
    void existsByEmployeeNumber_existing_returnsTrue() {
        assertThat(userRepository.existsByEmployeeNumber("EMP001")).isTrue();
    }

    @Test
    @DisplayName("existsByEmployeeNumber returns false for non-existing emp number")
    void existsByEmployeeNumber_nonExisting_returnsFalse() {
        assertThat(userRepository.existsByEmployeeNumber("EMP999")).isFalse();
    }

    @Test
    @DisplayName("Find all returns at least 3 seeded users")
    void findAll_returnsSeededUsers() {
        assertThat(userRepository.findAll()).hasSizeGreaterThanOrEqualTo(3);
    }
}
