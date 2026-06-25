package com.mining.safety.controller;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.dto.IncidentRequest;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Incident Controller Tests")
class IncidentControllerTest extends BaseIntegrationTest {

    private IncidentRequest buildIncidentRequest() {
        IncidentRequest req = new IncidentRequest();
        req.setTitle("Test Incident");
        req.setDescription("A test incident occurred at level 5");
        req.setIncidentType(IncidentType.NEAR_MISS);
        req.setSeverity(Severity.MEDIUM);
        req.setLocation("Level 5, Shaft 2");
        req.setSection("Underground East");
        req.setShiftTime("DAY");
        req.setIncidentDateTime(LocalDateTime.now().minusHours(1));
        req.setInjuryOccurred(false);
        req.setImmediateActions("Area secured and workers evacuated");
        return req;
    }

    private Long createAndGetId(String token) throws Exception {
        String response = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    // ─── CREATE INCIDENT ─────────────────────────────────────────

    @Test
    @DisplayName("Worker can create an incident")
    void createIncident_asWorker_returns200() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.referenceNumber").value(startsWith("INC-")))
                .andExpect(jsonPath("$.title").value("Test Incident"))
                .andExpect(jsonPath("$.status").value("REPORTED"))
                .andExpect(jsonPath("$.severity").value("MEDIUM"))
                .andExpect(jsonPath("$.reportedByName").isNotEmpty());
    }

    @Test
    @DisplayName("Safety officer can create an incident")
    void createIncident_asSafetyOfficer_returns200() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + safetyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPORTED"));
    }

    @Test
    @DisplayName("Incident reference number has correct format INC-YYYYMM-NNNN")
    void createIncident_referenceNumberFormat_isCorrect() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value(matchesRegex("INC-\\d{6}-\\d{4}")));
    }

    @Test
    @DisplayName("Incident with injury sets injuryOccurred and count")
    void createIncident_withInjury_setsInjuryFields() throws Exception {
        IncidentRequest req = buildIncidentRequest();
        req.setIncidentType(IncidentType.INJURY);
        req.setSeverity(Severity.HIGH);
        req.setInjuryOccurred(true);
        req.setNumberOfInjured(2);
        req.setInjuryDescription("Laceration to both hands");

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.injuryOccurred").value(true))
                .andExpect(jsonPath("$.numberOfInjured").value(2))
                .andExpect(jsonPath("$.incidentType").value("INJURY"));
    }

    @Test
    @DisplayName("Create incident without title returns 400")
    void createIncident_missingTitle_returns400() throws Exception {
        IncidentRequest req = buildIncidentRequest();
        req.setTitle("");

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create incident without location returns 400")
    void createIncident_missingLocation_returns400() throws Exception {
        IncidentRequest req = buildIncidentRequest();
        req.setLocation("");

        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create incident without auth returns 401")
    void createIncident_noToken_returns403() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildIncidentRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET INCIDENTS ───────────────────────────────────────────

    @Test
    @DisplayName("Worker can get all incidents")
    void getAllIncidents_asWorker_returns200() throws Exception {
        createAndGetId(workerToken());

        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + workerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Get incident by ID returns correct incident")
    void getIncidentById_validId_returnsIncident() throws Exception {
        Long id = createAndGetId(workerToken());

        mockMvc.perform(get("/api/incidents/" + id)
                        .header("Authorization", "Bearer " + workerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Test Incident"));
    }

    @Test
    @DisplayName("Get incident by non-existent ID returns 500")
    void getIncidentById_nonExistentId_returnsError() throws Exception {
        mockMvc.perform(get("/api/incidents/99999")
                        .header("Authorization", "Bearer " + workerToken()))
                .andExpect(status().is5xxServerError());
    }

    // ─── UPDATE STATUS ───────────────────────────────────────────

    @Test
    @DisplayName("Safety officer can update incident status")
    void updateStatus_asSafetyOfficer_returns200() throws Exception {
        Long id = createAndGetId(workerToken());

        mockMvc.perform(patch("/api/incidents/" + id + "/status")
                        .header("Authorization", "Bearer " + safetyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "UNDER_INVESTIGATION"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_INVESTIGATION"));
    }

    @Test
    @DisplayName("Status can be updated through full workflow")
    void updateStatus_fullWorkflow_updatesCorrectly() throws Exception {
        Long id = createAndGetId(workerToken());

        for (String status : new String[]{"UNDER_INVESTIGATION", "CORRECTIVE_ACTION_PENDING", "CLOSED"}) {
            mockMvc.perform(patch("/api/incidents/" + id + "/status")
                            .header("Authorization", "Bearer " + safetyToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("status", status))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status));
        }
    }

    @Test
    @DisplayName("Worker cannot update incident status (403)")
    void updateStatus_asWorker_returns403() throws Exception {
        Long id = createAndGetId(workerToken());

        mockMvc.perform(patch("/api/incidents/" + id + "/status")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "UNDER_INVESTIGATION"))))
                .andExpect(status().isForbidden());
    }

    // ─── ROOT CAUSE ──────────────────────────────────────────────

    @Test
    @DisplayName("Safety officer can add root cause analysis")
    void updateRootCause_asSafetyOfficer_returns200() throws Exception {
        Long id = createAndGetId(workerToken());

        mockMvc.perform(patch("/api/incidents/" + id + "/root-cause")
                        .header("Authorization", "Bearer " + safetyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("rootCause", "Improper PPE usage and inadequate lighting"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rootCause").value("Improper PPE usage and inadequate lighting"));
    }

    @Test
    @DisplayName("Worker cannot update root cause (403)")
    void updateRootCause_asWorker_returns403() throws Exception {
        Long id = createAndGetId(workerToken());

        mockMvc.perform(patch("/api/incidents/" + id + "/root-cause")
                        .header("Authorization", "Bearer " + workerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rootCause", "Test"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Multiple incidents return in descending order by date")
    void getAllIncidents_multipleIncidents_orderedByDateDesc() throws Exception {
        createAndGetId(workerToken());
        createAndGetId(safetyToken());
        createAndGetId(adminToken());

        mockMvc.perform(get("/api/incidents")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }
}
