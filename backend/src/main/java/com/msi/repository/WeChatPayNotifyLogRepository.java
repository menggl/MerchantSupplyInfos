package com.msi.repository;

import com.msi.domain.WeChatPayNotifyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeChatPayNotifyLogRepository extends JpaRepository<WeChatPayNotifyLog, Long> {
}
