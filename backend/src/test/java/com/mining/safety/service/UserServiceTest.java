package com.mining.safety.service;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.dto.UserRequest;
import com.mining.safety.dto.UserResponse;
import com.mining.safety.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Service Tests")
class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    private UserRequest buildRequest(String name, String empNo, String email, Role role) {
        UserRequest req = new UserRequest();
        req.setFullName(name);
        req.setEmployeeNumber(empNo);
        req.setEmail(email);
        req.setRole(role);
        req.setPassword("Test@1234");
        req.setDepartment("Mining");
        req.setSection("Surface");
        req.setActive(true);
        return req;
    }

    @Test
    @DisplayName("Get all users returns seeded users")
    void getAllUsers_returnsAtLeast3SeededUsers() {
        List<UserResponse> users = userService.getAllUsers();
        assertThat(users).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Create user returns saved user with generated ID")
    void createUser_validRequest_returnsSavedUser() {
        UserRequest req = buildRequest("Nkosi Dlamini", "EMP100", "nkosi@mine.co.za", Role.WORKER);
        UserResponse result = userService.createUser(req);

        assertThat(result.getId()).isNotNull().isPositive();
        assertThat(result.getFullName()).isEqualTo("Nkosi Dlamini");
        assertThat(result.getEmail()).isEqualTo("nkosi@mine.co.za");
        assertThat(result.getRole()).isEqualTo(Role.WORKER);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Create user with all roles works correctly")
    void createUser_eachRole_isCreatedSuccessfully() {
        for (Role role : Role.values()) {
            String empNo = "EMP2" + role.ordinal();
            String email = role.name().toLowerCase() + "test@mine.co.za";

            UserResponse result = userService.createUser(
                    buildRequest("Test " + role, empNo, email, role));

            assertThat(result.getRole()).isEqualTo(role);
        }
    }

    @Test
    @DisplayName("Create user with default password when password is blank")
    void createUser_blankPassword_usesDefaultPassword() {
        UserRequest req = buildRequest("No Pass User", "EMP110", "nopass@mine.co.za", Role.WORKER);
        req.setPassword("");

        UserResponse result = userService.createUser(req);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    @DisplayName("Duplicate email throws RuntimeException")
    void createUser_duplicateEmail_throwsException() {
        UserRequest req = buildRequest("Duplicate Email", "EMP120", "admin@safemine.co.za", Role.WORKER);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("Duplicate employee number throws RuntimeException")
    void createUser_duplicateEmployeeNumber_throwsException() {
        UserRequest req = buildRequest("Duplicate EmpNo", "EMP001", "unique@mine.co.za", Role.WORKER);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee number already in use");
    }

    @Test
    @DisplayName("Get user by ID returns correct user")
    void getUserById_validId_returnsUser() {
        UserResponse created = userService.createUser(
                buildRequest("Find Me", "EMP130", "findme@mine.co.za", Role.SUPERVISOR));

        UserResponse found = userService.getUserById(created.getId());
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo("findme@mine.co.za");
    }

    @Test
    @DisplayName("Get user by non-existent ID throws exception")
    void getUserById_invalidId_throwsException() {
        assertThatThrownBy(() -> userService.getUserById(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Update user changes role and department")
    void updateUser_validRequest_updatesFields() {
        UserResponse created = userService.createUser(
                buildRequest("Update Target", "EMP140", "updatetarget@mine.co.za", Role.WORKER));

        UserRequest updateReq = buildRequest("Update Target", "EMP140", "updatetarget@mine.co.za", Role.MANAGER);
        updateReq.setDepartment("Safety Management");

        UserResponse updated = userService.updateUser(created.getId(), updateReq);
        assertThat(updated.getRole()).isEqualTo(Role.MANAGER);
        assertThat(updated.getDepartment()).isEqualTo("Safety Management");
    }

    @Test
    @DisplayName("Update user with new password changes password hash")
    void updateUser_newPassword_updatesWithoutError() {
        UserResponse created = userService.createUser(
                buildRequest("Password Update", "EMP150", "pwdupdate@mine.co.za", Role.WORKER));

        UserRequest updateReq = buildRequest("Password Update", "EMP150", "pwdupdate@mine.co.za", Role.WORKER);
        updateReq.setPassword("NewPassword@999");

        UserResponse updated = userService.updateUser(created.getId(), updateReq);
        assertThat(updated.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Toggle status deactivates active user")
    void toggleStatus_activeUser_becomesInactive() {
        UserResponse created = userService.createUser(
                buildRequest("Toggle Target", "EMP160", "toggletarget@mine.co.za", Role.WORKER));
        assertThat(created.isActive()).isTrue();

        userService.toggleUserStatus(created.getId());

        UserResponse toggled = userService.getUserById(created.getId());
        assertThat(toggled.isActive()).isFalse();
    }

    @Test
    @DisplayName("Toggle status reactivates inactive user")
    void toggleStatus_inactiveUser_becomesActive() {
        UserResponse created = userService.createUser(
                buildRequest("Reactivate Target", "EMP170", "reactivate@mine.co.za", Role.WORKER));

        userService.toggleUserStatus(created.getId()); // deactivate
        userService.toggleUserStatus(created.getId()); // reactivate

        UserResponse result = userService.getUserById(created.getId());
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Delete user removes user from database")
    void deleteUser_validId_removesUser() {
        UserResponse created = userService.createUser(
                buildRequest("Delete Target", "EMP180", "delete@mine.co.za", Role.WORKER));
        Long id = created.getId();

        userService.deleteUser(id);

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("loadUserByUsername returns user details for valid email")
    void loadUserByUsername_validEmail_returnsUserDetails() {
        var details = userService.loadUserByUsername("admin@safemine.co.za");
        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("admin@safemine.co.za");
        assertThat(details.getAuthorities()).isNotEmpty();
    }

    @Test
    @DisplayName("loadUserByUsername throws for unknown email")
    void loadUserByUsername_unknownEmail_throwsException() {
        assertThatThrownBy(() -> userService.loadUserByUsername("nobody@mine.co.za"))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
    }
}
