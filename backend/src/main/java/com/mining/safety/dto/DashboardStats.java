package com.mining.safety.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DashboardStats {
    private long totalIncidents;
    private long openIncidents;
    private long criticalIncidents;
    private long closedIncidents;
    private long incidentsThisMonth;
    private long nearMisses;
    private long injuries;
    private long dmrNotified;
    private long overdueActions;
    private long pendingActions;
}
