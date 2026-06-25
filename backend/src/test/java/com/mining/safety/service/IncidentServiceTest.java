package com.mining.safety.service;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.dto.IncidentRequest;
import com.mining.safety.dto.IncidentResponse;
import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Incident Service Tests")
class IncidentServiceTest extends BaseIntegrationTest {

    @Autowired
    private IncidentService incidentService;

    private IncidentRequest buildRequest(IncidentType type, Severity severity) {
        IncidentRequest req = new IncidentRequest();
        req.setTitle("Service Test Incident");
        req.setDescription("Testing the service layer directly");
        req.setIncidentType(type);
        req.setSeverity(severity);
        req.setLocation("Test Mine Level 3");
        req.setIncidentDateTime(LocalDateTime.now().minusHours(1));
        req.setInjuryOccurred(false);
        return req;
    }

    @Test
    @DisplayName("Create incident assigns correct status REPORTED")
    void createIncident_defaultStatus_isReported() {
        IncidentResponse result = incidentService.createIncident(
                buildRequest(IncidentType.NEAR_MISS, Severity.LOW), "worker@safemine.co.za");

        assertThat(result.getStatus()).isEqualTo(IncidentStatus.REPORTED);
    }

    @Test
    @DisplayName("Reference number follows INC-YYYYMM-NNNN format")
    void createIncident_referenceNumber_matchesFormat() {
        IncidentResponse result = incidentService.createIncident(
                buildRequest(IncidentType.FIRE, Severity.HIGH), "admin@safemine.co.za");

        assertThat(result.getReferenceNumber()).matches("INC-\\d{6}-\\d{4}");
    }

    @Test
    @DisplayName("Create incident sets reporter name from email")
    void createIncident_reportedByName_isSetFromEmail() {
        IncidentResponse result = incidentService.createIncident(
                buildRequest(IncidentType.ELECTRICAL, Severity.MEDIUM), "worker@safemine.co.za");

        assertThat(result.getReportedByName()).isNotBlank();
        assertThat(result.getReportedByName()).isEqualTo("Sipho Nkosi");
    }

    @Test
    @DisplayName("Get all incidents returns list")
    void getAllIncidents_returnsNonNullList() {
        incidentService.createIncident(buildRequest(IncidentType.NEAR_MISS, Severity.LOW), "worker@safemine.co.za");

        assertThat(incidentService.getAllIncidents()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Get incident by ID returns correct incident")
    void getIncidentById_validId_returnsCorrectIncident() {
        IncidentResponse created = incidentService.createIncident(
                buildRequest(IncidentType.INJURY, Severity.CRITICAL), "safety@safemine.co.za");

        IncidentResponse found = incidentService.getIncidentById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getReferenceNumber()).isEqualTo(created.getReferenceNumber());
    }

    @Test
    @DisplayName("Get incident by non-existent ID throws exception")
    void getIncidentById_invalidId_throwsException() {
        assertThatThrownBy(() -> incidentService.getIncidentById(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Update status changes incident status correctly")
    void updateStatus_validTransition_changesStatus() {
        IncidentResponse created = incidentService.createIncident(
                buildRequest(IncidentType.NEAR_MISS, Severity.MEDIUM), "safety@safemine.co.za");

        IncidentResponse updated = incidentService.updateStatus(created.getId(), IncidentStatus.UNDER_INVESTIGATION);

        assertThat(updated.getStatus()).isEqualTo(IncidentStatus.UNDER_INVESTIGATION);
    }

    @Test
    @DisplayName("Update root cause saves and returns root cause text")
    void updateRootCause_validText_savesCorrectly() {
        IncidentResponse created = incidentService.createIncident(
                buildRequest(IncidentType.FALL_OF_GROUND, Severity.HIGH), "safety@safemine.co.za");

        String rootCause = "Lack of support installation in the hanging wall";
        IncidentResponse updated = incidentService.updateRootCause(created.getId(), rootCause);

        assertThat(updated.getRootCause()).isEqualTo(rootCause);
    }

    @Test
    @DisplayName("Dashboard stats total increases after incident creation")
    void getDashboardStats_afterNewIncident_totalIncreases() {
        long before = incidentService.getDashboardStats().getTotalIncidents();

        incidentService.createIncident(buildRequest(IncidentType.PROPERTY_DAMAGE, Severity.LOW), "admin@safemine.co.za");

        long after = incidentService.getDashboardStats().getTotalIncidents();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Critical incidents are counted in dashboard stats")
    void getDashboardStats_criticalIncident_countedCorrectly() {
        long before = incidentService.getDashboardStats().getCriticalIncidents();

        incidentService.createIncident(buildRequest(IncidentType.EXPLOSION, Severity.CRITICAL), "admin@safemine.co.za");

        long after = incidentService.getDashboardStats().getCriticalIncidents();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Closed incidents are counted correctly in stats")
    void getDashboardStats_closedIncident_countedCorrectly() {
        IncidentResponse created = incidentService.createIncident(
                buildRequest(IncidentType.NEAR_MISS, Severity.LOW), "safety@safemine.co.za");
        incidentService.updateStatus(created.getId(), IncidentStatus.CLOSED);

        long closed = incidentService.getDashboardStats().getClosedIncidents();
        assertThat(closed).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Injury incidents are counted in stats")
    void getDashboardStats_injuryIncident_countedInInjuries() {
        IncidentRequest req = buildRequest(IncidentType.INJURY, Severity.HIGH);
        req.setInjuryOccurred(true);
        req.setNumberOfInjured(1);

        long before = incidentService.getDashboardStats().getInjuries();
        incidentService.createIncident(req, "worker@safemine.co.za");

        long after = incidentService.getDashboardStats().getInjuries();
        assertThat(after).isEqualTo(before + 1);
    }
}
