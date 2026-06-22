package com.upimesh.settlement.repository;

import com.upimesh.settlement.model.entity.SettlementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, Long> {

    List<SettlementTransaction> findBySettlementId(String settlementId);

    Optional<SettlementTransaction> findByOriginalTransactionId(String originalTransactionId);

    boolean existsByOriginalTransactionId(String originalTransactionId);
}
