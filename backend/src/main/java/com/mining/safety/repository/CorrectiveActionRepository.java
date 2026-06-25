package com.mining.safety.repository;

import com.mining.safety.entity.CorrectiveAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CorrectiveActionRepository extends JpaRepository<CorrectiveAction, Long> {
    List<CorrectiveAction> findByIncidentId(Long incidentId);
    List<CorrectiveAction> findByAssignedToId(Long userId);
    List<CorrectiveAction> findByStatus(String status);
}
