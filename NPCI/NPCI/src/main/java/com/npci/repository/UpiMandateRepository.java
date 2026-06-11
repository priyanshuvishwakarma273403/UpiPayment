package com.npci.repository;

import com.npci.model.entity.UpiMandate;
import com.npci.model.enums.MandateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UpiMandateRepository extends JpaRepository<UpiMandate, Long> {

    Optional<UpiMandate> findByMandateId(String mandateId);

    List<UpiMandate> findByUserUpiIdAndStatus(String userUpiId, MandateStatus status);

    // Find mandates due for execution today
    @Query("SELECT m FROM UpiMandate m WHERE m.status = 'ACTIVE' " +
            "AND m.nextExecutionDate <= :today AND m.endDate >= :today")
    List<UpiMandate> findMandatesDueForExecution(@Param("today") LocalDate today);

    // Find expired mandates to update status
    @Query("SELECT m FROM UpiMandate m WHERE m.status = 'ACTIVE' AND m.endDate < :today")
    List<UpiMandate> findExpiredMandates(@Param("today") LocalDate today);


}
