package com.msi.repository;

import com.msi.domain.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
  long countByWechatIdAndSendTimeBetween(String wechatId, LocalDateTime start, LocalDateTime end);
}

