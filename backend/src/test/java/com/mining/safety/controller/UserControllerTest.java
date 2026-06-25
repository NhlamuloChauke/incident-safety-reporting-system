package com.mining.safety.controller;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.dto.UserRequest;
import com.mining.safety.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User Controller Tests")
class UserControllerTest extends BaseIntegrationTest {

    private UserRequest buildUserRequest(String fullName, String empNo, String email, Role role) {
        UserRequest req = new UserRequest();
        req.setFullName(fullName);
        req.setEmployeeNumber(empNo);
        req.setEmail(email);
        req.setRole(role);
        req.setPassword("Test@1234");
        req.setDepartment("Mining");
        req.setSection("Underground");
        req.setActive(true);
        return req;
    }

    // ─── GET ALL USERS ───────────────────────────────────────────

    @Test
    @DisplayName("Admin can get all users")
    void getAllUsers_asAdmin_returns200WithList() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Safety Officer can get all users")
    void getAllUsers_asSafetyOfficer_returns200() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + safetyToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Worker cannot get all users (403)")
    void getAllUsers_asWorker_returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + workerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to get users returns 403")
    void getAllUsers_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // ─── CREATE USER ─────────────────────────────────────────────

    @Test
    @DisplayName("Admin creates a new worker user successfully")
    void createUser_asAdmin_validData_returns200() throws Exception {
        UserRequest req = buildUserRequest("Thabo Mokoena", "EMP010", "thabo@mine.co.za", Role.WORKER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Thabo Mokoena"))
                .andExpect(jsonPath("$.email").value("thabo@mine.co.za"))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Admin creates a safety officer successfully")
    void createUser_asAdmin_safetyOfficerRole_returns200() throws Exception {
        UserRequest req = buildUserRequest("Lindiwe Dube", "EMP011", "lindiwe@mine.co.za", Role.SAFETY_OFFICER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SAFETY_OFFICER"));
    }

    @Test
    @DisplayName("Create user with duplicate email returns 500")
    void createUser_duplicateEmail_returnsError() throws Exception {
        UserRequest req = buildUserRequest("Duplicate User", "EMP099", "admin@safemine.co.za", Role.WORKER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Create user with duplicate employee number returns 500")
    void createUser_duplicateEmployeeNumber_returnsError() throws Exception {
        UserRequest req = buildUserRequest("Another User", "EMP001", "another@mine.co.za", Role.WORKER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Worker cannot create users (403)")
    void createUser_asWorker_returns403() throws Exception {
        UserRequest req = buildUserRequest("Test User", "EMP020", "test@mine.co.za", Role.WORKER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Safety officer cannot create users (403)")
    void createUser_asSafetyOfficer_returns403() throws Exception {
        UserRequest req = buildUserRequest("Test User", "EMP021", "test2@mine.co.za", Role.WORKER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + safetyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create user with blank full name returns 400")
    void createUser_blankFullName_returns400() throws Exception {
        UserRequest req = buildUserRequest("", "EMP030", "valid@mine.co.za", Role.WORKER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create user without role returns 400")
    void createUser_nullRole_returns400() throws Exception {
        UserRequest req = new UserRequest();
        req.setFullName("No Role User");
        req.setEmployeeNumber("EMP031");
        req.setEmail("norole@mine.co.za");
        req.setActive(true);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── UPDATE USER ─────────────────────────────────────────────

    @Test
    @DisplayName("Admin updates user role successfully")
    void updateUser_asAdmin_changesRole() throws Exception {
        // First create a user
        UserRequest createReq = buildUserRequest("Update Me", "EMP040", "updateme@mine.co.za", Role.WORKER);
        String createResponse = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        // Now update the role
        UserRequest updateReq = buildUserRequest("Update Me", "EMP040", "updateme@mine.co.za", Role.SUPERVISOR);
        mockMvc.perform(put("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    @DisplayName("Update non-existent user returns 500")
    void updateUser_nonExistentId_returnsError() throws Exception {
        UserRequest req = buildUserRequest("Ghost", "EMP999", "ghost@mine.co.za", Role.WORKER);

        mockMvc.perform(put("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError());
    }

    // ─── TOGGLE STATUS ───────────────────────────────────────────

    @Test
    @DisplayName("Admin can toggle user active status")
    void toggleStatus_asAdmin_returns200() throws Exception {
        // Create user
        UserRequest req = buildUserRequest("Toggle Me", "EMP050", "toggle@mine.co.za", Role.WORKER);
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/users/" + userId + "/toggle-status")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deactivated user cannot log in")
    void deactivatedUser_cannotLogin() throws Exception {
        // Create user
        UserRequest req = buildUserRequest("Deactivate Me", "EMP060", "deactivate@mine.co.za", Role.WORKER);
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // Deactivate
        mockMvc.perform(patch("/api/users/" + userId + "/toggle-status")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());

        // Try to login — should fail
        com.mining.safety.dto.LoginRequest loginReq = new com.mining.safety.dto.LoginRequest();
        loginReq.setEmail("deactivate@mine.co.za");
        loginReq.setPassword("Test@1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    // ─── DELETE USER ─────────────────────────────────────────────

    @Test
    @DisplayName("Admin can delete a user")
    void deleteUser_asAdmin_returns204() throws Exception {
        UserRequest req = buildUserRequest("Delete Me", "EMP070", "deleteme@mine.co.za", Role.WORKER);
        String response = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Worker cannot delete users (403)")
    void deleteUser_asWorker_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", "Bearer " + workerToken()))
                .andExpect(status().isForbidden());
    }
}
