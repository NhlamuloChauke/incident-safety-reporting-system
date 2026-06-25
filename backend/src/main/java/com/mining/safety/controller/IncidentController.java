package com.mining.safety.controller;

import com.mining.safety.dto.IncidentRequest;
import com.mining.safety.dto.IncidentResponse;
import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentRequest request,
                                                            Authentication auth) {
        return ResponseEntity.ok(incidentService.createIncident(request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER','MANAGER','ADMIN')")
    public ResponseEntity<IncidentResponse> updateStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, String> body) {
        IncidentStatus status = IncidentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/root-cause")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER','MANAGER','ADMIN')")
    public ResponseEntity<IncidentResponse> updateRootCause(@PathVariable Long id,
                                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(incidentService.updateRootCause(id, body.get("rootCause")));
    }
}
