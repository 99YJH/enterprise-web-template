package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.UserRepository;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.PermissionRepository;
import com.enterprise.webtemplate.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private FileRepository fileRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        validateAccess();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long inactiveUsers = totalUsers - activeUsers;
        long pendingUsers = userRepository.countByApprovalStatus("PENDING");
        long approvedUsers = userRepository.countByApprovalStatus("APPROVED");
        long rejectedUsers = userRepository.countByApprovalStatus("REJECTED");
        
        long totalRoles = roleRepository.count();
        long totalPermissions = permissionRepository.count();
        long totalFiles = fileRepository.count();
        
        // 오늘 가입한 사용자 수
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        long todayRegistrations = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        
        // 이번 주 가입한 사용자 수
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        long weeklyRegistrations = userRepository.countByCreatedAtBetween(startOfWeek, LocalDateTime.now());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);
        stats.put("pendingUsers", pendingUsers);
        stats.put("approvedUsers", approvedUsers);
        stats.put("rejectedUsers", rejectedUsers);
        stats.put("totalRoles", totalRoles);
        stats.put("totalPermissions", totalPermissions);
        stats.put("totalFiles", totalFiles);
        stats.put("todayRegistrations", todayRegistrations);
        stats.put("weeklyRegistrations", weeklyRegistrations);
        
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStats() {
        validateAccess();
        
        // 역할별 사용자 수
        Map<String, Long> usersByRole = roleRepository.findAll().stream()
                .collect(Collectors.toMap(
                        role -> role.getName(),
                        role -> (long) role.getUsers().size()
                ));
        
        // 월별 가입 사용자 수 (최근 12개월)
        List<Map<String, Object>> monthlyRegistrations = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);
            
            long count = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthData.put("monthName", date.format(DateTimeFormatter.ofPattern("yyyy년 MM월")));
            monthData.put("count", count);
            monthlyRegistrations.add(monthData);
        }
        
        // 승인 상태별 사용자 수
        Map<String, Long> usersByApprovalStatus = Map.of(
                "PENDING", userRepository.countByApprovalStatus("PENDING"),
                "APPROVED", userRepository.countByApprovalStatus("APPROVED"),
                "REJECTED", userRepository.countByApprovalStatus("REJECTED")
        );
        
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("usersByRole", usersByRole);
        userStats.put("monthlyRegistrations", monthlyRegistrations);
        userStats.put("usersByApprovalStatus", usersByApprovalStatus);
        
        return userStats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRecentActivities() {
        validateAccess();
        
        // 최근 등록된 사용자 (10명)
        List<User> recentUsers = userRepository.findTop10ByOrderByCreatedAtDesc();
        List<Map<String, Object>> recentRegistrations = recentUsers.stream()
                .map(user -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("type", "USER_REGISTRATION");
                    activity.put("message", user.getName() + "님이 가입했습니다.");
                    activity.put("timestamp", user.getCreatedAt());
                    activity.put("user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "profileImageUrl", user.getProfileImageUrl()
                    ));
                    return activity;
                })
                .collect(Collectors.toList());
        
        // 최근 승인된 사용자 (10명)
        List<User> recentApprovedUsers = userRepository.findTop10ByApprovalStatusOrderByUpdatedAtDesc("APPROVED");
        List<Map<String, Object>> recentApprovals = recentApprovedUsers.stream()
                .map(user -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("type", "USER_APPROVAL");
                    activity.put("message", user.getName() + "님이 승인되었습니다.");
                    activity.put("timestamp", user.getUpdatedAt());
                    activity.put("user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "profileImageUrl", user.getProfileImageUrl()
                    ));
                    return activity;
                })
                .collect(Collectors.toList());
        
        // 모든 활동을 합치고 시간순 정렬
        List<Map<String, Object>> allActivities = new ArrayList<>();
        allActivities.addAll(recentRegistrations);
        allActivities.addAll(recentApprovals);
        
        allActivities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        // 최근 20개 활동만 반환
        List<Map<String, Object>> recentActivities = allActivities.stream()
                .limit(20)
                .collect(Collectors.toList());
        
        Map<String, Object> activities = new HashMap<>();
        activities.put("recentActivities", recentActivities);
        activities.put("totalActivities", allActivities.size());
        
        return activities;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemHealth() {
        validateAccess();
        
        // 시스템 상태 정보
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
        
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("totalMemory", totalMemory);
        memoryInfo.put("freeMemory", freeMemory);
        memoryInfo.put("usedMemory", usedMemory);
        memoryInfo.put("maxMemory", maxMemory);
        memoryInfo.put("usagePercentage", Math.round(memoryUsagePercentage * 100.0) / 100.0);
        
        // 데이터베이스 상태
        Map<String, Object> databaseInfo = new HashMap<>();
        try {
            long userCount = userRepository.count();
            databaseInfo.put("status", "HEALTHY");
            databaseInfo.put("userCount", userCount);
            databaseInfo.put("connectionTest", "SUCCESS");
        } catch (Exception e) {
            databaseInfo.put("status", "ERROR");
            databaseInfo.put("error", e.getMessage());
            databaseInfo.put("connectionTest", "FAILED");
        }
        
        // 전체 시스템 상태
        String systemStatus = "HEALTHY";
        if (memoryUsagePercentage > 90) {
            systemStatus = "WARNING";
        }
        if (!databaseInfo.get("status").equals("HEALTHY")) {
            systemStatus = "ERROR";
        }
        
        Map<String, Object> health = new HashMap<>();
        health.put("systemStatus", systemStatus);
        health.put("timestamp", LocalDateTime.now());
        health.put("memory", memoryInfo);
        health.put("database", databaseInfo);
        health.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        
        return health;
    }

    private void validateAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증된 사용자를 찾을 수 없습니다.");
        }
    }
}