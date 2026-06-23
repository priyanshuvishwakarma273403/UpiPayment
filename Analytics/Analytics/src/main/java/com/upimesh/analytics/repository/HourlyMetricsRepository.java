package com.upimesh.analytics.repository;

import com.upimesh.analytics.model.entity.HourlyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyMetricsRepository extends JpaRepository<HourlyMetrics, Long> {

    List<HourlyMetrics> findByDateOrderByHourAsc(LocalDate date);

    Optional<HourlyMetrics> findByDateAndHour(LocalDate date, int hour);
}
