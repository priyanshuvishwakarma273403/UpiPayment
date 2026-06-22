package com.upimesh.bankgateway.repository;

import com.upimesh.bankgateway.model.entity.UpiHandleResolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UpiHandleResolutionRepository extends JpaRepository<UpiHandleResolution, Long> {

    Optional<UpiHandleResolution> findByUpiHandle(String upiHandle);

    @Query("SELECT u FROM UpiHandleResolution u WHERE u.lastResolvedAt < :cutoff AND u.isActive = true")
    List<UpiHandleResolution> findStaleResolutions(@Param("cutoff") LocalDateTime cutoff);

    boolean existsByUpiHandle(String upiHandle);
}
