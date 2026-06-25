package com.mining.safety.service;

import com.mining.safety.dto.DashboardStats;
import com.mining.safety.dto.IncidentRequest;
import com.mining.safety.dto.IncidentResponse;
import com.mining.safety.entity.Incident;
import com.mining.safety.entity.User;
import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import com.mining.safety.repository.CorrectiveActionRepository;
import com.mining.safety.repository.IncidentRepository;
import com.mining.safety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final CorrectiveActionRepository correctiveActionRepository;

    public IncidentResponse createIncident(IncidentRequest request, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId()).orElse(null);
        }

        Incident incident = Incident.builder()
                .referenceNumber(generateReferenceNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .incidentType(request.getIncidentType())
                .severity(request.getSeverity())
                .location(request.getLocation())
                .section(request.getSection())
                .shiftTime(request.getShiftTime())
                .incidentDateTime(request.getIncidentDateTime())
                .injuryOccurred(request.isInjuryOccurred())
                .numberOfInjured(request.getNumberOfInjured())
                .injuryDescription(request.getInjuryDescription())
                .immediateActions(request.getImmediateActions())
                .reportedBy(reporter)
                .assignedTo(assignedTo)
                .status(IncidentStatus.REPORTED)
                .build();

        return toResponse(incidentRepository.save(incident));
    }

    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAllOrderByReportedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public IncidentResponse getIncidentById(Long id) {
        return toResponse(incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found")));
    }

    public IncidentResponse updateStatus(Long id, IncidentStatus status) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        incident.setStatus(status);
        return toResponse(incidentRepository.save(incident));
    }

    public IncidentResponse updateRootCause(Long id, String rootCause) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        incident.setRootCause(rootCause);
        return toResponse(incidentRepository.save(incident));
    }

    public DashboardStats getDashboardStats() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        long overdueActions = correctiveActionRepository.findByStatus("OPEN").stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isBefore(java.time.LocalDate.now()))
                .count();

        return DashboardStats.builder()
                .totalIncidents(incidentRepository.count())
                .openIncidents(incidentRepository.countByStatus(IncidentStatus.REPORTED)
                        + incidentRepository.countByStatus(IncidentStatus.UNDER_INVESTIGATION))
                .criticalIncidents(incidentRepository.countBySeverity(Severity.CRITICAL))
                .closedIncidents(incidentRepository.countByStatus(IncidentStatus.CLOSED))
                .incidentsThisMonth(incidentRepository.countSince(startOfMonth))
                .nearMisses(incidentRepository.findByIncidentType(IncidentType.NEAR_MISS).size())
                .injuries(incidentRepository.findAll().stream().filter(Incident::isInjuryOccurred).count())
                .dmrNotified(incidentRepository.findAll().stream().filter(Incident::isDmrNotified).count())
                .overdueActions(overdueActions)
                .pendingActions(correctiveActionRepository.findByStatus("OPEN").size())
                .build();
    }

    private String generateReferenceNumber() {
        String prefix = "INC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        long count = incidentRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }

    private IncidentResponse toResponse(Incident i) {
        return IncidentResponse.builder()
                .id(i.getId())
                .referenceNumber(i.getReferenceNumber())
                .title(i.getTitle())
                .description(i.getDescription())
                .incidentType(i.getIncidentType())
                .severity(i.getSeverity())
                .status(i.getStatus())
                .location(i.getLocation())
                .section(i.getSection())
                .shiftTime(i.getShiftTime())
                .incidentDateTime(i.getIncidentDateTime())
                .reportedAt(i.getReportedAt())
                .injuryOccurred(i.isInjuryOccurred())
                .numberOfInjured(i.getNumberOfInjured())
                .injuryDescription(i.getInjuryDescription())
                .dmrNotified(i.isDmrNotified())
                .immediateActions(i.getImmediateActions())
                .rootCause(i.getRootCause())
                .reportedByName(i.getReportedBy() != null ? i.getReportedBy().getFullName() : null)
                .assignedToName(i.getAssignedTo() != null ? i.getAssignedTo().getFullName() : null)
                .build();
    }
}
