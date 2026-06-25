package com.mining.safety.repository;

import com.mining.safety.entity.Incident;
import com.mining.safety.enums.IncidentStatus;
import com.mining.safety.enums.IncidentType;
import com.mining.safety.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByReferenceNumber(String referenceNumber);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findBySeverity(Severity severity);

    List<Incident> findByIncidentType(IncidentType type);

    List<Incident> findByReportedById(Long userId);

    List<Incident> findByAssignedToId(Long userId);

    List<Incident> findByIncidentDateTimeBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = :status")
    long countByStatus(IncidentStatus status);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.severity = :severity")
    long countBySeverity(Severity severity);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.incidentDateTime >= :from")
    long countSince(LocalDateTime from);

    @Query("SELECT i FROM Incident i ORDER BY i.reportedAt DESC")
    List<Incident> findAllOrderByReportedAtDesc();
}
