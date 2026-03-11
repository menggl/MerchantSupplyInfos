package com.msi.repository;

import com.msi.domain.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, String> {
    Optional<DailyStatistics> findByStatisticsDate(String statisticsDate);
}
