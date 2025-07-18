package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.annotation.RequirePermission;
import com.enterprise.webtemplate.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @RequirePermission("DASHBOARD_VIEW")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "DASHBOARD_FETCH_FAILED", "message", "대시보드 통계 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/user-stats")
    @RequirePermission("DASHBOARD_VIEW")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> userStats = dashboardService.getUserStats();
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "USER_STATS_FETCH_FAILED", "message", "사용자 통계 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/recent-activities")
    @RequirePermission("DASHBOARD_VIEW")
    public ResponseEntity<?> getRecentActivities() {
        try {
            Map<String, Object> activities = dashboardService.getRecentActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "ACTIVITIES_FETCH_FAILED", "message", "최근 활동 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/system-health")
    @RequirePermission("DASHBOARD_VIEW")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = dashboardService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "SYSTEM_HEALTH_FETCH_FAILED", "message", "시스템 상태 조회 중 오류가 발생했습니다."));
        }
    }
}