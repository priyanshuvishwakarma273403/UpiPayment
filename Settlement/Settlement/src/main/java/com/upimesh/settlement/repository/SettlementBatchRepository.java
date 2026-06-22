package com.upimesh.settlement.repository;

import com.upimesh.settlement.model.entity.SettlementBatch;
import com.upimesh.settlement.model.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

    Optional<SettlementBatch> findByBatchId(String batchId);

    Optional<SettlementBatch> findBySettlementDate(LocalDate settlementDate);

    List<SettlementBatch> findByStatus(SettlementStatus status);

    List<SettlementBatch> findBySettlementDateBetween(LocalDate from, LocalDate to);
}
