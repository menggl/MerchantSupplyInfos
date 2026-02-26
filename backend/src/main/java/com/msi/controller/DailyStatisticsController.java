package com.msi.controller;

import com.msi.service.DailyStatisticsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class DailyStatisticsController {

    private final DailyStatisticsService dailyStatisticsService;

    @Value("${wecom.stats.token:}")
    private String statsToken;

    public DailyStatisticsController(DailyStatisticsService dailyStatisticsService) {
        this.dailyStatisticsService = dailyStatisticsService;
    }

    @GetMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerStatistics(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();

        if (statsToken == null || statsToken.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Statistics token not configured on server");
            return ResponseEntity.status(500).body(response);
        }

        if (!statsToken.equals(token)) {
            response.put("status", "error");
            response.put("message", "Invalid token");
            return ResponseEntity.status(403).body(response);
        }

        // 异步执行或者同步执行？用户说“调用后直接执行”，通常意味同步或者触发。
        // 为了方便测试看结果，这里直接同步执行。
        // 但如果耗时较长可能会超时。不过统计逻辑看起来还好。
        // 用户需求是“直接执行”，所以同步调用最符合“测试”直觉（虽然不会返回统计结果到接口，但会发消息）。
        
        try {
            dailyStatisticsService.sendDailyStatistics();
            response.put("status", "success");
            response.put("message", "Daily statistics task triggered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Execution failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
