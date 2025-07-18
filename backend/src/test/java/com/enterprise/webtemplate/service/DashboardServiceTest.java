package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.FileRepository;
import com.enterprise.webtemplate.repository.PermissionRepository;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");
        testRole.setDescription("기본 사용자");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사용자");
        testUser.setIsActive(true);
        testUser.setApprovalStatus("APPROVED");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setRoles(Set.of(testRole));

        testRole.setUsers(Set.of(testUser));

        // SecurityContext 설정
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void testGetDashboardStats_Success() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByIsActive(true)).thenReturn(80L);
        when(userRepository.countByApprovalStatus("PENDING")).thenReturn(10L);
        when(userRepository.countByApprovalStatus("APPROVED")).thenReturn(85L);
        when(userRepository.countByApprovalStatus("REJECTED")).thenReturn(5L);
        when(roleRepository.count()).thenReturn(5L);
        when(permissionRepository.count()).thenReturn(20L);
        when(fileRepository.count()).thenReturn(500L);
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L)
                .thenReturn(25L);

        // When
        Map<String, Object> result = dashboardService.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("totalUsers"));
        assertEquals(80L, result.get("activeUsers"));
        assertEquals(20L, result.get("inactiveUsers"));
        assertEquals(10L, result.get("pendingUsers"));
        assertEquals(85L, result.get("approvedUsers"));
        assertEquals(5L, result.get("rejectedUsers"));
        assertEquals(5L, result.get("totalRoles"));
        assertEquals(20L, result.get("totalPermissions"));
        assertEquals(500L, result.get("totalFiles"));
        assertEquals(5L, result.get("todayRegistrations"));
        assertEquals(25L, result.get("weeklyRegistrations"));

        verify(userRepository).count();
        verify(userRepository).countByIsActive(true);
        verify(userRepository, times(3)).countByApprovalStatus(anyString());
        verify(roleRepository).count();
        verify(permissionRepository).count();
        verify(fileRepository).count();
        verify(userRepository, times(2)).countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetDashboardStats_Unauthorized() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.getDashboardStats();
        });
    }

    @Test
    void testGetUserStats_Success() {
        // Given
        when(roleRepository.findAll()).thenReturn(List.of(testRole));
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L, 3L, 8L, 2L, 6L, 4L, 9L, 1L, 7L, 2L, 5L, 3L);
        when(userRepository.countByApprovalStatus("PENDING")).thenReturn(10L);
        when(userRepository.countByApprovalStatus("APPROVED")).thenReturn(85L);
        when(userRepository.countByApprovalStatus("REJECTED")).thenReturn(5L);

        // When
        Map<String, Object> result = dashboardService.getUserStats();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("usersByRole"));
        assertTrue(result.containsKey("monthlyRegistrations"));
        assertTrue(result.containsKey("usersByApprovalStatus"));

        @SuppressWarnings("unchecked")
        Map<String, Long> usersByRole = (Map<String, Long>) result.get("usersByRole");
        assertEquals(1L, usersByRole.get("USER"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> monthlyRegistrations = (List<Map<String, Object>>) result.get("monthlyRegistrations");
        assertEquals(12, monthlyRegistrations.size());

        @SuppressWarnings("unchecked")
        Map<String, Long> usersByApprovalStatus = (Map<String, Long>) result.get("usersByApprovalStatus");
        assertEquals(10L, usersByApprovalStatus.get("PENDING"));
        assertEquals(85L, usersByApprovalStatus.get("APPROVED"));
        assertEquals(5L, usersByApprovalStatus.get("REJECTED"));

        verify(roleRepository).findAll();
        verify(userRepository, times(12)).countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(userRepository, times(3)).countByApprovalStatus(anyString());
    }

    @Test
    void testGetRecentActivities_Success() {
        // Given
        List<User> recentUsers = List.of(testUser);
        List<User> recentApprovedUsers = List.of(testUser);
        
        when(userRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(recentUsers);
        when(userRepository.findTop10ByApprovalStatusOrderByUpdatedAtDesc("APPROVED")).thenReturn(recentApprovedUsers);

        // When
        Map<String, Object> result = dashboardService.getRecentActivities();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("recentActivities"));
        assertTrue(result.containsKey("totalActivities"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recentActivities = (List<Map<String, Object>>) result.get("recentActivities");
        assertEquals(2, recentActivities.size());

        Map<String, Object> activity = recentActivities.get(0);
        assertTrue(activity.containsKey("type"));
        assertTrue(activity.containsKey("message"));
        assertTrue(activity.containsKey("timestamp"));
        assertTrue(activity.containsKey("user"));

        verify(userRepository).findTop10ByOrderByCreatedAtDesc();
        verify(userRepository).findTop10ByApprovalStatusOrderByUpdatedAtDesc("APPROVED");
    }

    @Test
    void testGetSystemHealth_Success() {
        // Given
        when(userRepository.count()).thenReturn(100L);

        // When
        Map<String, Object> result = dashboardService.getSystemHealth();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("systemStatus"));
        assertTrue(result.containsKey("timestamp"));
        assertTrue(result.containsKey("memory"));
        assertTrue(result.containsKey("database"));
        assertTrue(result.containsKey("uptime"));

        assertEquals("HEALTHY", result.get("systemStatus"));

        @SuppressWarnings("unchecked")
        Map<String, Object> memoryInfo = (Map<String, Object>) result.get("memory");
        assertTrue(memoryInfo.containsKey("totalMemory"));
        assertTrue(memoryInfo.containsKey("freeMemory"));
        assertTrue(memoryInfo.containsKey("usedMemory"));
        assertTrue(memoryInfo.containsKey("maxMemory"));
        assertTrue(memoryInfo.containsKey("usagePercentage"));

        @SuppressWarnings("unchecked")
        Map<String, Object> databaseInfo = (Map<String, Object>) result.get("database");
        assertEquals("HEALTHY", databaseInfo.get("status"));
        assertEquals(100L, databaseInfo.get("userCount"));
        assertEquals("SUCCESS", databaseInfo.get("connectionTest"));

        verify(userRepository).count();
    }

    @Test
    void testGetSystemHealth_DatabaseError() {
        // Given
        when(userRepository.count()).thenThrow(new RuntimeException("Database connection failed"));

        // When
        Map<String, Object> result = dashboardService.getSystemHealth();

        // Then
        assertNotNull(result);
        assertEquals("ERROR", result.get("systemStatus"));

        @SuppressWarnings("unchecked")
        Map<String, Object> databaseInfo = (Map<String, Object>) result.get("database");
        assertEquals("ERROR", databaseInfo.get("status"));
        assertEquals("FAILED", databaseInfo.get("connectionTest"));
        assertTrue(databaseInfo.containsKey("error"));

        verify(userRepository).count();
    }

    @Test
    void testValidateAccess_NoAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.getDashboardStats();
        });
    }

    @Test
    void testValidateAccess_NotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardService.getDashboardStats();
        });
    }
}