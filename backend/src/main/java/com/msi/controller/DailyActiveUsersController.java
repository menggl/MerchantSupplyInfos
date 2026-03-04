package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.service.DailyStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class DailyActiveUsersController {

    private final DailyStatisticsService dailyStatisticsService;

    public DailyActiveUsersController(DailyStatisticsService dailyStatisticsService) {
        this.dailyStatisticsService = dailyStatisticsService;
    }

    @PostMapping("/active")
    public ResponseEntity<Map<String, Object>> recordActiveUser(@RequestAttribute("merchant") Merchant merchant) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 记录活跃用户
            dailyStatisticsService.recordActiveUser(merchant.getId());
            
            response.put("status", "success");
            response.put("message", "Active user recorded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to record active user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
