package com.upimesh.aml.repository;

import com.upimesh.aml.model.entity.AmlAlert;
import com.upimesh.aml.model.enums.AlertStatus;
import com.upimesh.aml.model.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, Long> {
    Optional<AmlAlert> findByAlertId(String alertId);
    List<AmlAlert> findByUserUpiIdOrderByCreatedAtDesc(String userUpiId);
    List<AmlAlert> findByStatus(AlertStatus status);
    List<AmlAlert> findByAlertTypeAndStatus(AlertType alertType, AlertStatus status);
    long countByUserUpiIdAndCreatedAtAfter(String userUpiId, LocalDateTime createdAt);
}
