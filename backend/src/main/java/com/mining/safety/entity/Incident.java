package com.mining.safety.entity;

import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referenceNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private String location;
    private String section;
    private String shiftTime;

    private LocalDateTime incidentDateTime;
    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;

    private boolean injuryOccurred;
    private int numberOfInjured;
    private String injuryDescription;

    private boolean dmrNotified;
    private LocalDateTime dmrNotificationDate;

    private String immediateActions;
    private String rootCause;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_by_id")
    private User reportedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CorrectiveAction> correctiveActions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = IncidentStatus.REPORTED;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
