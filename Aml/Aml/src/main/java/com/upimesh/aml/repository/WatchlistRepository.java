package com.upimesh.aml.repository;

import com.upimesh.aml.model.entity.WatchlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistEntry, Long> {
    List<WatchlistEntry> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    Optional<WatchlistEntry> findByEntityIdAndIsActiveTrue(String entityId);
    List<WatchlistEntry> findByEntityTypeAndIsActiveTrue(String entityType);
}
