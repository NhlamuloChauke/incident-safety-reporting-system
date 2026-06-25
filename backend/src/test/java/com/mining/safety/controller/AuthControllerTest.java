package com.mining.safety.controller;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth Controller Tests")
class AuthControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Login with valid admin credentials returns JWT token")
    void login_validAdminCredentials_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@safemine.co.za");
        request.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@safemine.co.za"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.fullName").value("System Administrator"));
    }

    @Test
    @DisplayName("Login with valid safety officer credentials returns correct role")
    void login_validSafetyCredentials_returnsCorrectRole() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("safety@safemine.co.za");
        request.setPassword("Safety@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SAFETY_OFFICER"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login with valid worker credentials returns correct role")
    void login_validWorkerCredentials_returnsCorrectRole() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("worker@safemine.co.za");
        request.setPassword("Worker@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    @DisplayName("Login with wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@safemine.co.za");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with non-existent email returns 401")
    void login_unknownEmail_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@safemine.co.za");
        request.setPassword("Any@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with blank email returns 400")
    void login_blankEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with blank password returns 400")
    void login_blankPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@safemine.co.za");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Accessing protected endpoint without token returns 403")
    void protectedEndpoint_noToken_returns403() throws Exception {
        mockMvc.perform(post("/api/incidents"))
                .andExpect(status().isForbidden());
    }
}
