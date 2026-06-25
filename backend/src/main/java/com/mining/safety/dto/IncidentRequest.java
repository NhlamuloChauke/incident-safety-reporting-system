package com.mining.safety.dto;

import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentRequest {
    @NotBlank private String title;
    @NotBlank private String description;
    @NotNull  private IncidentType incidentType;
    @NotNull  private Severity severity;
    @NotBlank private String location;
    private String section;
    private String shiftTime;
    @NotNull  private LocalDateTime incidentDateTime;
    private boolean injuryOccurred;
    private int numberOfInjured;
    private String injuryDescription;
    private String immediateActions;
    private Long assignedToId;
}
