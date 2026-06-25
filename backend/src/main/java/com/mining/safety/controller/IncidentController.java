package com.mining.safety.controller;

import com.mining.safety.dto.IncidentRequest;
import com.mining.safety.dto.IncidentResponse;
import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Report and manage mine safety incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @Operation(
            summary = "Report a new incident",
            description = "Any authenticated user can report an incident. " +
                    "The reporter is automatically linked to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody IncidentRequest request,
            Authentication auth) {
        return ResponseEntity.ok(incidentService.createIncident(request, auth.getName()));
    }

    @GetMapping
    @Operation(
            summary = "List all incidents",
            description = "Returns every incident in the system, ordered by report date descending. " +
                    "Accessible to all authenticated users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of incidents",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single incident", description = "Fetch full details for a specific incident by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Incident not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<IncidentResponse> getIncident(
            @Parameter(description = "Incident ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER','MANAGER','ADMIN')")
    @Operation(
            summary = "Update incident status",
            description = "Move an incident through its workflow. " +
                    "Requires role `SAFETY_OFFICER`, `MANAGER`, or `ADMIN`.\n\n" +
                    "Valid statuses: `REPORTED`, `UNDER_INVESTIGATION`, `RESOLVED`, `CLOSED`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "403", description = "Insufficient role — WORKER cannot update status"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> updateStatus(
            @Parameter(description = "Incident ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New status",
                    content = @Content(examples = @ExampleObject(value = "{\"status\": \"UNDER_INVESTIGATION\"}")))
            @RequestBody Map<String, String> body) {
        IncidentStatus status = IncidentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/root-cause")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER','MANAGER','ADMIN')")
    @Operation(
            summary = "Record root cause",
            description = "Document the root cause analysis outcome for an incident. " +
                    "Requires role `SAFETY_OFFICER`, `MANAGER`, or `ADMIN`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Root cause saved"),
            @ApiResponse(responseCode = "403", description = "Insufficient role"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> updateRootCause(
            @Parameter(description = "Incident ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Root cause description",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"rootCause\": \"Inadequate PPE and lack of safety briefing\"}")))
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(incidentService.updateRootCause(id, body.get("rootCause")));
    }
}
