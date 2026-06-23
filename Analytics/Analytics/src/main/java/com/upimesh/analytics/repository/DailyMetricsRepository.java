package com.upimesh.analytics.repository;

import com.upimesh.analytics.model.entity.DailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMetricsRepository extends JpaRepository<DailyMetrics, Long> {

    Optional<DailyMetrics> findByDate(LocalDate date);

    List<DailyMetrics> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
