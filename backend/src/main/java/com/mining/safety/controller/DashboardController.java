package com.mining.safety.controller;

import com.mining.safety.dto.DashboardStats;
import com.mining.safety.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated safety KPIs and incident statistics")
public class DashboardController {

    private final IncidentService incidentService;

    @GetMapping("/stats")
    @Operation(
            summary = "Get dashboard statistics",
            description = "Returns headline counts used by the SafeMine dashboard:\n" +
                    "- Total incidents reported\n" +
                    "- Incidents by status (reported, under investigation, resolved, closed)\n" +
                    "- Open incidents count\n\n" +
                    "Accessible to all authenticated users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DashboardStats.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(incidentService.getDashboardStats());
    }
}
