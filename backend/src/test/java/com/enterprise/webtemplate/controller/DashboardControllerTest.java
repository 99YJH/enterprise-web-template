package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private Map<String, Object> mockStats;

    @BeforeEach
    void setUp() {
        mockStats = Map.of(
                "totalUsers", 100L,
                "activeUsers", 80L,
                "totalFiles", 500L
        );
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetDashboardStats_Success() throws Exception {
        // Given
        when(dashboardService.getDashboardStats()).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.activeUsers").value(80))
                .andExpect(jsonPath("$.totalFiles").value(500));

        verify(dashboardService).getDashboardStats();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetDashboardStats_Error() throws Exception {
        // Given
        when(dashboardService.getDashboardStats()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("DASHBOARD_FETCH_FAILED"))
                .andExpect(jsonPath("$.message").value("대시보드 통계 조회 중 오류가 발생했습니다."));

        verify(dashboardService).getDashboardStats();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetUserStats_Success() throws Exception {
        // Given
        Map<String, Object> userStats = Map.of(
                "usersByRole", Map.of("ADMIN", 5L, "USER", 95L),
                "monthlyRegistrations", List.of()
        );
        when(dashboardService.getUserStats()).thenReturn(userStats);

        // When & Then
        mockMvc.perform(get("/api/dashboard/user-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usersByRole").exists())
                .andExpect(jsonPath("$.monthlyRegistrations").exists());

        verify(dashboardService).getUserStats();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetUserStats_Error() throws Exception {
        // Given
        when(dashboardService.getUserStats()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/dashboard/user-stats"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("USER_STATS_FETCH_FAILED"))
                .andExpect(jsonPath("$.message").value("사용자 통계 조회 중 오류가 발생했습니다."));

        verify(dashboardService).getUserStats();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetRecentActivities_Success() throws Exception {
        // Given
        Map<String, Object> activities = Map.of(
                "recentActivities", List.of(),
                "totalActivities", 0
        );
        when(dashboardService.getRecentActivities()).thenReturn(activities);

        // When & Then
        mockMvc.perform(get("/api/dashboard/recent-activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentActivities").exists())
                .andExpect(jsonPath("$.totalActivities").value(0));

        verify(dashboardService).getRecentActivities();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetRecentActivities_Error() throws Exception {
        // Given
        when(dashboardService.getRecentActivities()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/dashboard/recent-activities"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("ACTIVITIES_FETCH_FAILED"))
                .andExpect(jsonPath("$.message").value("최근 활동 조회 중 오류가 발생했습니다."));

        verify(dashboardService).getRecentActivities();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetSystemHealth_Success() throws Exception {
        // Given
        Map<String, Object> health = Map.of(
                "systemStatus", "HEALTHY",
                "memory", Map.of("usagePercentage", 65.0),
                "database", Map.of("status", "HEALTHY")
        );
        when(dashboardService.getSystemHealth()).thenReturn(health);

        // When & Then
        mockMvc.perform(get("/api/dashboard/system-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.database").exists());

        verify(dashboardService).getSystemHealth();
    }

    @Test
    @WithMockUser(authorities = "DASHBOARD_VIEW")
    void testGetSystemHealth_Error() throws Exception {
        // Given
        when(dashboardService.getSystemHealth()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/dashboard/system-health"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("SYSTEM_HEALTH_FETCH_FAILED"))
                .andExpect(jsonPath("$.message").value("시스템 상태 조회 중 오류가 발생했습니다."));

        verify(dashboardService).getSystemHealth();
    }

    @Test
    void testGetDashboardStats_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());

        verify(dashboardService, never()).getDashboardStats();
    }

    @Test
    @WithMockUser(authorities = "READ_ONLY")
    void testGetDashboardStats_InsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden());

        verify(dashboardService, never()).getDashboardStats();
    }
}