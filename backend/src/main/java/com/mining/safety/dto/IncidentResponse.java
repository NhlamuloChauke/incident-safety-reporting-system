package com.mining.safety.dto;

import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class IncidentResponse {
    private Long id;
    private String referenceNumber;
    private String title;
    private String description;
    private IncidentType incidentType;
    private Severity severity;
    private IncidentStatus status;
    private String location;
    private String section;
    private String shiftTime;
    private LocalDateTime incidentDateTime;
    private LocalDateTime reportedAt;
    private boolean injuryOccurred;
    private int numberOfInjured;
    private String injuryDescription;
    private boolean dmrNotified;
    private String immediateActions;
    private String rootCause;
    private String reportedByName;
    private String assignedToName;
}
