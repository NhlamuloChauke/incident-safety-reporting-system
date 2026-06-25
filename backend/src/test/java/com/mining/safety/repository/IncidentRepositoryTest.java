package com.mining.safety.repository;

import com.mining.safety.BaseIntegrationTest;
import com.mining.safety.entity.Incident;
import com.mining.safety.entity.User;
import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Incident Repository Tests")
class IncidentRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    private Incident saveIncident(String refNo, IncidentType type, Severity severity,
                                  IncidentStatus status, User reporter) {
        Incident incident = Incident.builder()
                .referenceNumber(refNo)
                .title("Repo Test: " + refNo)
                .description("Repository level test incident")
                .incidentType(type)
                .severity(severity)
                .status(status)
                .location("Test Location")
                .incidentDateTime(LocalDateTime.now().minusHours(1))
                .reportedBy(reporter)
                .build();
        return incidentRepository.save(incident);
    }

    private User getAdmin() {
        return userRepository.findByEmail("admin@safemine.co.za").orElseThrow();
    }

    @Test
    @DisplayName("Save incident persists to database")
    void saveIncident_persistsToDatabase() {
        Incident saved = saveIncident("INC-TEST-0001", IncidentType.NEAR_MISS, Severity.LOW,
                IncidentStatus.REPORTED, getAdmin());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getReferenceNumber()).isEqualTo("INC-TEST-0001");
    }

    @Test
    @DisplayName("Find by reference number returns correct incident")
    void findByReferenceNumber_validRef_returnsIncident() {
        saveIncident("INC-REF-0001", IncidentType.FIRE, Severity.HIGH, IncidentStatus.REPORTED, getAdmin());

        Optional<Incident> found = incidentRepository.findByReferenceNumber("INC-REF-0001");
        assertThat(found).isPresent();
        assertThat(found.get().getIncidentType()).isEqualTo(IncidentType.FIRE);
    }

    @Test
    @DisplayName("Find by reference number returns empty for unknown reference")
    void findByReferenceNumber_unknownRef_returnsEmpty() {
        assertThat(incidentRepository.findByReferenceNumber("INC-UNKNOWN-9999")).isEmpty();
    }

    @Test
    @DisplayName("Find by status returns only matching incidents")
    void findByStatus_reported_returnsOnlyReported() {
        saveIncident("INC-STA-001", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, getAdmin());
        saveIncident("INC-STA-002", IncidentType.INJURY, Severity.HIGH, IncidentStatus.CLOSED, getAdmin());

        List<Incident> reported = incidentRepository.findByStatus(IncidentStatus.REPORTED);
        assertThat(reported).isNotEmpty();
        assertThat(reported).allMatch(i -> i.getStatus() == IncidentStatus.REPORTED);
    }

    @Test
    @DisplayName("Find by severity returns only matching incidents")
    void findBySeverity_critical_returnsOnlyCritical() {
        saveIncident("INC-SEV-001", IncidentType.EXPLOSION, Severity.CRITICAL, IncidentStatus.REPORTED, getAdmin());
        saveIncident("INC-SEV-002", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, getAdmin());

        List<Incident> critical = incidentRepository.findBySeverity(Severity.CRITICAL);
        assertThat(critical).isNotEmpty();
        assertThat(critical).allMatch(i -> i.getSeverity() == Severity.CRITICAL);
    }

    @Test
    @DisplayName("Find by incident type returns only matching incidents")
    void findByIncidentType_nearMiss_returnsOnlyNearMiss() {
        saveIncident("INC-TYP-001", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, getAdmin());
        saveIncident("INC-TYP-002", IncidentType.FIRE, Severity.HIGH, IncidentStatus.REPORTED, getAdmin());

        List<Incident> nearMisses = incidentRepository.findByIncidentType(IncidentType.NEAR_MISS);
        assertThat(nearMisses).isNotEmpty();
        assertThat(nearMisses).allMatch(i -> i.getIncidentType() == IncidentType.NEAR_MISS);
    }

    @Test
    @DisplayName("Find by reporter ID returns reporter's incidents")
    void findByReportedById_validUser_returnsIncidents() {
        User admin = getAdmin();
        saveIncident("INC-REP-001", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, admin);

        List<Incident> incidents = incidentRepository.findByReportedById(admin.getId());
        assertThat(incidents).isNotEmpty();
        assertThat(incidents).allMatch(i -> i.getReportedBy().getId().equals(admin.getId()));
    }

    @Test
    @DisplayName("countByStatus counts correctly")
    void countByStatus_reported_returnsCorrectCount() {
        long before = incidentRepository.countByStatus(IncidentStatus.REPORTED);
        saveIncident("INC-CNT-001", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, getAdmin());

        assertThat(incidentRepository.countByStatus(IncidentStatus.REPORTED)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("countBySeverity counts correctly")
    void countBySeverity_high_returnsCorrectCount() {
        long before = incidentRepository.countBySeverity(Severity.HIGH);
        saveIncident("INC-SVC-001", IncidentType.INJURY, Severity.HIGH, IncidentStatus.REPORTED, getAdmin());

        assertThat(incidentRepository.countBySeverity(Severity.HIGH)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("countSince returns incidents after given date")
    void countSince_startOfDay_returnsRecentIncidents() {
        saveIncident("INC-SNS-001", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, getAdmin());

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0);
        long count = incidentRepository.countSince(startOfDay);
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("findAllOrderByReportedAtDesc returns list ordered descending")
    void findAllOrderByReportedAtDesc_returnsOrderedList() {
        saveIncident("INC-ORD-001", IncidentType.NEAR_MISS, Severity.LOW, IncidentStatus.REPORTED, getAdmin());
        saveIncident("INC-ORD-002", IncidentType.FIRE, Severity.HIGH, IncidentStatus.REPORTED, getAdmin());

        List<Incident> ordered = incidentRepository.findAllOrderByReportedAtDesc();
        assertThat(ordered).isNotEmpty();

        for (int i = 0; i < ordered.size() - 1; i++) {
            assertThat(ordered.get(i).getReportedAt())
                    .isAfterOrEqualTo(ordered.get(i + 1).getReportedAt());
        }
    }
}
