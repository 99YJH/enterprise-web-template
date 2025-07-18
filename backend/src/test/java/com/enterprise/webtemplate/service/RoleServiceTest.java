package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.RoleCreateRequest;
import com.enterprise.webtemplate.dto.RoleResponse;
import com.enterprise.webtemplate.dto.RoleUpdateRequest;
import com.enterprise.webtemplate.dto.PermissionResponse;
import com.enterprise.webtemplate.entity.Permission;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RoleService roleService;

    private Role adminRole;
    private Role userRole;
    private User adminUser;
    private User testUser;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // 권한 설정
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("USER_READ");
        testPermission.setDescription("사용자 조회");
        testPermission.setCategory("USER_MANAGEMENT");

        // 역할 설정
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        adminRole.setDescription("관리자");
        adminRole.setPermissions(Set.of(testPermission));

        userRole = new Role();
        userRole.setId(2L);
        userRole.setName("USER");
        userRole.setDescription("일반 사용자");

        // 사용자 설정
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setName("관리자");
        adminUser.setRoles(Set.of(adminRole));

        testUser = new User();
        testUser.setId(2L);
        testUser.setEmail("user@example.com");
        testUser.setName("일반 사용자");
        testUser.setRoles(Set.of(userRole));

        adminRole.setUsers(Set.of(adminUser));
        userRole.setUsers(Set.of(testUser));

        // SecurityContext 설정
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@example.com");
    }

    @Test
    void testGetAllRoles_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findAll()).thenReturn(List.of(adminRole, userRole));

        // When
        List<RoleResponse> result = roleService.getAllRoles();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findAll();
    }

    @Test
    void testGetRoleById_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

        // When
        RoleResponse result = roleService.getRoleById(1L);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        assertEquals("관리자", result.getDescription());

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(1L);
    }

    @Test
    void testGetRoleById_NotFound() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            roleService.getRoleById(1L);
        });

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(1L);
    }

    @Test
    void testCreateRole_Success() {
        // Given
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("MANAGER");
        request.setDescription("매니저");
        request.setPermissionNames(Set.of("USER_READ"));

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);

        // When
        RoleResponse result = roleService.createRole(request);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).existsByName("MANAGER");
        verify(permissionRepository).findByName("USER_READ");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testCreateRole_DuplicateName() {
        // Given
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("ADMIN");
        request.setDescription("관리자");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.createRole(request);
        });

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testUpdateRole_Success() {
        // Given
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setDescription("업데이트된 설명");
        request.setPermissionNames(Set.of("USER_READ"));

        Role customRole = new Role();
        customRole.setId(3L);
        customRole.setName("CUSTOM");
        customRole.setDescription("커스텀 역할");
        customRole.setUsers(new HashSet<>());

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(customRole));
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(customRole);

        // When
        RoleResponse result = roleService.updateRole(3L, request);

        // Then
        assertNotNull(result);
        assertEquals("CUSTOM", result.getName());

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(3L);
        verify(permissionRepository).findByName("USER_READ");
        verify(roleRepository).save(customRole);
    }

    @Test
    void testUpdateRole_SystemRole() {
        // Given
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setDescription("업데이트된 설명");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.updateRole(1L, request);
        });

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(1L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testDeleteRole_Success() {
        // Given
        Role customRole = new Role();
        customRole.setId(3L);
        customRole.setName("CUSTOM");
        customRole.setDescription("커스텀 역할");
        customRole.setUsers(new HashSet<>());

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(customRole));

        // When
        roleService.deleteRole(3L);

        // Then
        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(3L);
        verify(roleRepository).delete(customRole);
    }

    @Test
    void testDeleteRole_SystemRole() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.deleteRole(1L);
        });

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(1L);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void testDeleteRole_HasUsers() {
        // Given
        Role customRole = new Role();
        customRole.setId(3L);
        customRole.setName("CUSTOM");
        customRole.setDescription("커스텀 역할");
        customRole.setUsers(Set.of(testUser));

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(customRole));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.deleteRole(3L);
        });

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findById(3L);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void testGetAllPermissions_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(permissionRepository.findAll()).thenReturn(List.of(testPermission));

        // When
        List<PermissionResponse> result = roleService.getAllPermissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER_READ", result.get(0).getName());

        verify(userRepository).findByEmail("admin@example.com");
        verify(permissionRepository).findAll();
    }

    @Test
    void testGetPermissionsByCategory_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(permissionRepository.findAll()).thenReturn(List.of(testPermission));

        // When
        Map<String, List<PermissionResponse>> result = roleService.getPermissionsByCategory();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("USER_MANAGEMENT"));
        assertEquals(1, result.get("USER_MANAGEMENT").size());

        verify(userRepository).findByEmail("admin@example.com");
        verify(permissionRepository).findAll();
    }

    @Test
    void testUpdateUserRoles_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        roleService.updateUserRoles(2L, Set.of("ADMIN"));

        // Then
        verify(userRepository).findByEmail("admin@example.com");
        verify(userRepository).findById(2L);
        verify(roleRepository).findByName("ADMIN");
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserRoles_MasterUser() {
        // Given
        Role masterRole = new Role();
        masterRole.setName("MASTER");
        
        User masterUser = new User();
        masterUser.setId(3L);
        masterUser.setRoles(Set.of(masterRole));

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(3L)).thenReturn(Optional.of(masterUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.updateUserRoles(3L, Set.of("ADMIN"));
        });

        verify(userRepository).findByEmail("admin@example.com");
        verify(userRepository).findById(3L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUsersByRole_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        // When
        List<User> result = roleService.getUsersByRole("ADMIN");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin@example.com", result.get(0).getEmail());

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).findByName("ADMIN");
    }

    @Test
    void testGetRoleStatistics_Success() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(roleRepository.count()).thenReturn(3L);
        when(permissionRepository.count()).thenReturn(10L);
        when(roleRepository.findAll()).thenReturn(List.of(adminRole, userRole));

        // When
        Map<String, Object> result = roleService.getRoleStatistics();

        // Then
        assertNotNull(result);
        assertEquals(3L, result.get("totalRoles"));
        assertEquals(10L, result.get("totalPermissions"));
        assertTrue(result.containsKey("roleUserCounts"));

        verify(userRepository).findByEmail("admin@example.com");
        verify(roleRepository).count();
        verify(permissionRepository).count();
        verify(roleRepository).findAll();
    }

    @Test
    void testValidateAdminAccess_NoAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            roleService.getAllRoles();
        });
    }

    @Test
    void testValidateAdminAccess_NotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            roleService.getAllRoles();
        });
    }

    @Test
    void testValidateAdminAccess_InsufficientPermissions() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            roleService.getAllRoles();
        });

        verify(userRepository).findByEmail("admin@example.com");
    }

    @Test
    void testValidateAdminAccess_UserNotFound() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            roleService.getAllRoles();
        });

        verify(userRepository).findByEmail("admin@example.com");
    }
}