package com.mining.safety.controller;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.dto.IncidentRequest;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Dashboard Controller Tests")
class DashboardControllerTest extends BaseIntegrationTest {

    private void createIncident(String token, IncidentType type, Severity severity, boolean injury) throws Exception {
        IncidentRequest req = new IncidentRequest();
        req.setTitle("Incident " + type);
        req.setDescription("Test description for " + type);
        req.setIncidentType(type);
        req.setSeverity(severity);
        req.setLocation("Level 3");
        req.setIncidentDateTime(LocalDateTime.now().minusHours(2));
        req.setInjuryOccurred(injury);
        if (injury) { req.setNumberOfInjured(1); req.setInjuryDescription("Minor cut"); }

        mockMvc.perform(post("/api/incidents")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    @Test
    @DisplayName("Dashboard stats returns all required fields")
    void getDashboardStats_returnsAllFields() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").isNumber())
                .andExpect(jsonPath("$.openIncidents").isNumber())
                .andExpect(jsonPath("$.criticalIncidents").isNumber())
                .andExpect(jsonPath("$.closedIncidents").isNumber())
                .andExpect(jsonPath("$.incidentsThisMonth").isNumber())
                .andExpect(jsonPath("$.nearMisses").isNumber())
                .andExpect(jsonPath("$.injuries").isNumber())
                .andExpect(jsonPath("$.dmrNotified").isNumber())
                .andExpect(jsonPath("$.overdueActions").isNumber())
                .andExpect(jsonPath("$.pendingActions").isNumber());
    }

    @Test
    @DisplayName("Dashboard stats reflects newly created incident")
    void getDashboardStats_afterCreateIncident_totalIncrementsBy1() throws Exception {
        String statsJson = mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken()))
                .andReturn().getResponse().getContentAsString();
        long before = objectMapper.readTree(statsJson).get("totalIncidents").asLong();

        createIncident(workerToken(), IncidentType.NEAR_MISS, Severity.LOW, false);

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncidents").value(before + 1));
    }

    @Test
    @DisplayName("Dashboard stats correctly counts near misses")
    void getDashboardStats_nearMissCount_isAccurate() throws Exception {
        createIncident(workerToken(), IncidentType.NEAR_MISS, Severity.LOW, false);
        createIncident(workerToken(), IncidentType.NEAR_MISS, Severity.MEDIUM, false);
        createIncident(workerToken(), IncidentType.INJURY, Severity.HIGH, true);

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nearMisses").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Dashboard stats correctly counts injuries")
    void getDashboardStats_injuryCount_isAccurate() throws Exception {
        createIncident(workerToken(), IncidentType.INJURY, Severity.CRITICAL, true);
        createIncident(workerToken(), IncidentType.INJURY, Severity.HIGH, true);

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.injuries").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Dashboard stats correctly counts critical incidents")
    void getDashboardStats_criticalCount_isAccurate() throws Exception {
        createIncident(workerToken(), IncidentType.EXPLOSION, Severity.CRITICAL, false);

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalIncidents").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Dashboard stats accessible by worker")
    void getDashboardStats_asWorker_returns200() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + workerToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dashboard stats without token returns 401")
    void getDashboardStats_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
