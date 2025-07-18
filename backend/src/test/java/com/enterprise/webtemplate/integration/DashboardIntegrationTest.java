package com.enterprise.webtemplate.integration;

import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import com.enterprise.webtemplate.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@Transactional
class DashboardIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        
        // Create admin role
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("관리자");
        adminRole = roleRepository.save(adminRole);

        // Create admin user
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setName("테스트 관리자");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setIsActive(true);
        adminUser.setApprovalStatus("APPROVED");
        adminUser.setRoles(Set.of(adminRole));
        adminUser = userRepository.save(adminUser);

        // Generate admin token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            adminUser.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        adminToken = jwtTokenProvider.generateToken(authentication);
    }

    @Test
    void testDashboardStatsEndpoint() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/dashboard/stats",
            HttpMethod.GET,
            entity,
            Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> stats = response.getBody();
        assertThat(stats).containsKeys(
            "totalUsers",
            "activeUsers",
            "inactiveUsers",
            "pendingUsers",
            "approvedUsers",
            "rejectedUsers",
            "totalRoles",
            "totalPermissions",
            "totalFiles",
            "todayRegistrations",
            "weeklyRegistrations"
        );
        
        // Verify values
        assertThat(stats.get("totalUsers")).isEqualTo(1);
        assertThat(stats.get("activeUsers")).isEqualTo(1);
        assertThat(stats.get("totalRoles")).isEqualTo(1);
    }

    @Test
    void testUserStatsEndpoint() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/dashboard/user-stats",
            HttpMethod.GET,
            entity,
            Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> userStats = response.getBody();
        assertThat(userStats).containsKeys(
            "usersByRole",
            "monthlyRegistrations",
            "usersByApprovalStatus"
        );
        
        // Verify role distribution
        @SuppressWarnings("unchecked")
        Map<String, Integer> usersByRole = (Map<String, Integer>) userStats.get("usersByRole");
        assertThat(usersByRole).containsKey("ADMIN");
        assertThat(usersByRole.get("ADMIN")).isEqualTo(1);
    }

    @Test
    void testRecentActivitiesEndpoint() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/dashboard/recent-activities",
            HttpMethod.GET,
            entity,
            Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> activities = response.getBody();
        assertThat(activities).containsKeys(
            "recentActivities",
            "totalActivities"
        );
        
        // Verify activities structure
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recentActivities = (List<Map<String, Object>>) activities.get("recentActivities");
        assertThat(recentActivities).isNotNull();
        assertThat(activities.get("totalActivities")).isNotNull();
    }

    @Test
    void testSystemHealthEndpoint() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/dashboard/system-health",
            HttpMethod.GET,
            entity,
            Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> health = response.getBody();
        assertThat(health).containsKeys(
            "systemStatus",
            "timestamp",
            "memory",
            "database",
            "uptime"
        );
        
        // Verify system status
        assertThat(health.get("systemStatus")).isIn("HEALTHY", "WARNING", "ERROR");
        
        // Verify memory info
        @SuppressWarnings("unchecked")
        Map<String, Object> memoryInfo = (Map<String, Object>) health.get("memory");
        assertThat(memoryInfo).containsKeys(
            "totalMemory",
            "freeMemory",
            "usedMemory",
            "maxMemory",
            "usagePercentage"
        );
        
        // Verify database info
        @SuppressWarnings("unchecked")
        Map<String, Object> databaseInfo = (Map<String, Object>) health.get("database");
        assertThat(databaseInfo).containsKeys(
            "status",
            "connectionTest"
        );
        assertThat(databaseInfo.get("status")).isEqualTo("HEALTHY");
        assertThat(databaseInfo.get("connectionTest")).isEqualTo("SUCCESS");
    }

    @Test
    void testDashboardEndpointsWithoutToken() {
        // Given
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        // When & Then
        ResponseEntity<Map> response1 = restTemplate.exchange(
            baseUrl + "/dashboard/stats",
            HttpMethod.GET,
            entity,
            Map.class
        );
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> response2 = restTemplate.exchange(
            baseUrl + "/dashboard/user-stats",
            HttpMethod.GET,
            entity,
            Map.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> response3 = restTemplate.exchange(
            baseUrl + "/dashboard/recent-activities",
            HttpMethod.GET,
            entity,
            Map.class
        );
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> response4 = restTemplate.exchange(
            baseUrl + "/dashboard/system-health",
            HttpMethod.GET,
            entity,
            Map.class
        );
        assertThat(response4.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testDashboardEndpointsWithInvalidToken() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When & Then
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/dashboard/stats",
            HttpMethod.GET,
            entity,
            Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testDashboardEndpointsWithInsufficientPermissions() {
        // Given
        // Create user role
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("일반 사용자");
        userRole = roleRepository.save(userRole);

        // Create regular user
        User regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setName("일반 사용자");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setIsActive(true);
        regularUser.setApprovalStatus("APPROVED");
        regularUser.setRoles(Set.of(userRole));
        regularUser = userRepository.save(regularUser);

        // Generate user token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            regularUser.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String userToken = jwtTokenProvider.generateToken(authentication);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When & Then
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/dashboard/stats",
            HttpMethod.GET,
            entity,
            Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testDashboardDataConsistency() {
        // Given
        // Create additional users for testing
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("일반 사용자");
        userRole = roleRepository.save(userRole);

        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setName("사용자 1");
        user1.setPassword(passwordEncoder.encode("password"));
        user1.setIsActive(true);
        user1.setApprovalStatus("APPROVED");
        user1.setRoles(Set.of(userRole));
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setName("사용자 2");
        user2.setPassword(passwordEncoder.encode("password"));
        user2.setIsActive(false);
        user2.setApprovalStatus("PENDING");
        user2.setRoles(Set.of(userRole));
        userRepository.save(user2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<Map> statsResponse = restTemplate.exchange(
            baseUrl + "/dashboard/stats",
            HttpMethod.GET,
            entity,
            Map.class
        );

        ResponseEntity<Map> userStatsResponse = restTemplate.exchange(
            baseUrl + "/dashboard/user-stats",
            HttpMethod.GET,
            entity,
            Map.class
        );

        // Then
        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userStatsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> stats = statsResponse.getBody();
        Map<String, Object> userStats = userStatsResponse.getBody();

        // Verify data consistency
        assertThat(stats.get("totalUsers")).isEqualTo(3); // admin + 2 users
        assertThat(stats.get("activeUsers")).isEqualTo(2); // admin + user1
        assertThat(stats.get("inactiveUsers")).isEqualTo(1); // user2
        assertThat(stats.get("pendingUsers")).isEqualTo(1); // user2
        assertThat(stats.get("approvedUsers")).isEqualTo(2); // admin + user1

        // Verify user role distribution
        @SuppressWarnings("unchecked")
        Map<String, Integer> usersByRole = (Map<String, Integer>) userStats.get("usersByRole");
        assertThat(usersByRole.get("ADMIN")).isEqualTo(1);
        assertThat(usersByRole.get("USER")).isEqualTo(2);
    }
}