package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.annotation.RequirePermission;
import com.enterprise.webtemplate.dto.RoleResponse;
import com.enterprise.webtemplate.dto.RoleCreateRequest;
import com.enterprise.webtemplate.dto.RoleUpdateRequest;
import com.enterprise.webtemplate.dto.PermissionResponse;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<RoleResponse> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "ROLES_FETCH_FAILED", "message", "역할 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{roleId}")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> getRoleById(@PathVariable Long roleId) {
        try {
            RoleResponse role = roleService.getRoleById(roleId);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ROLE_NOT_FOUND", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "ROLE_FETCH_FAILED", "message", "역할 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleCreateRequest request) {
        try {
            RoleResponse role = roleService.createRole(request);
            return ResponseEntity.ok(Map.of(
                    "message", "역할이 성공적으로 생성되었습니다.",
                    "role", role
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ROLE_CREATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "역할 생성 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{roleId}")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleUpdateRequest request) {
        try {
            RoleResponse role = roleService.updateRole(roleId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "역할이 성공적으로 수정되었습니다.",
                    "role", role
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ROLE_UPDATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "역할 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{roleId}")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> deleteRole(@PathVariable Long roleId) {
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.ok(Map.of("message", "역할이 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "DELETE_FORBIDDEN", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ROLE_DELETE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "역할 삭제 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/permissions")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> getAllPermissions() {
        try {
            List<PermissionResponse> permissions = roleService.getAllPermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "PERMISSIONS_FETCH_FAILED", "message", "권한 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/permissions/by-category")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> getPermissionsByCategory() {
        try {
            Map<String, List<PermissionResponse>> permissionsByCategory = roleService.getPermissionsByCategory();
            return ResponseEntity.ok(permissionsByCategory);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "PERMISSIONS_FETCH_FAILED", "message", "권한 카테고리 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/assign-user")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> assignRolesToUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            @SuppressWarnings("unchecked")
            Set<String> roleNames = (Set<String>) request.get("roleNames");
            
            roleService.updateUserRoles(userId, roleNames);
            return ResponseEntity.ok(Map.of("message", "사용자 역할이 성공적으로 할당되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ROLE_ASSIGN_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "역할 할당 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{roleName}/users")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> getUsersByRole(@PathVariable String roleName) {
        try {
            List<User> users = roleService.getUsersByRole(roleName);
            return ResponseEntity.ok(Map.of(
                    "users", users,
                    "count", users.size()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ROLE_USERS_FETCH_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "역할별 사용자 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/statistics")
    @RequirePermission("ROLE_MANAGEMENT")
    public ResponseEntity<?> getRoleStatistics() {
        try {
            Map<String, Object> statistics = roleService.getRoleStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "STATISTICS_FETCH_FAILED", "message", "역할 통계 조회 중 오류가 발생했습니다."));
        }
    }
}