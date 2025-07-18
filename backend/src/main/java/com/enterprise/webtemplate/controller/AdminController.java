package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.annotation.RequirePermission;
import com.enterprise.webtemplate.dto.UserListResponse;
import com.enterprise.webtemplate.dto.UserProfileResponse;
import com.enterprise.webtemplate.dto.UserProfileUpdateRequest;
import com.enterprise.webtemplate.dto.UserSearchRequest;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isEmailVerified,
            @RequestParam(required = false) User.ApprovalStatus approvalStatus,
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        try {
            UserSearchRequest request = new UserSearchRequest();
            request.setEmail(email);
            request.setName(name);
            request.setDepartment(department);
            request.setPosition(position);
            request.setIsActive(isActive);
            request.setIsEmailVerified(isEmailVerified);
            request.setApprovalStatus(approvalStatus);
            request.setRoleName(roleName);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);
            request.setPage(page);
            request.setSize(size);
            
            UserListResponse.PageResponse response = userService.searchUsers(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "USER_SEARCH_FAILED", "message", "사용자 검색 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{userId}")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserProfileResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "USER_NOT_FOUND", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "USER_FETCH_FAILED", "message", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{userId}")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UserProfileUpdateRequest request) {
        try {
            UserProfileResponse updatedUser = userService.updateUserByAdmin(userId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "사용자 정보가 성공적으로 업데이트되었습니다.",
                    "user", updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "USER_UPDATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "사용자 정보 업데이트 중 오류가 발생했습니다."));
        }
    }

    @PatchMapping("/{userId}/activation")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> toggleUserActivation(@PathVariable Long userId) {
        try {
            User user = userService.toggleUserActivationByAdmin(userId);
            return ResponseEntity.ok(Map.of(
                    "message", user.getIsActive() ? "사용자가 활성화되었습니다." : "사용자가 비활성화되었습니다.",
                    "isActive", user.getIsActive()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ACTIVATION_TOGGLE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "사용자 활성화 상태 변경 중 오류가 발생했습니다."));
        }
    }

    @PatchMapping("/{userId}/approval")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> updateApprovalStatus(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "MISSING_STATUS", "message", "승인 상태를 입력해주세요."));
            }
            
            User.ApprovalStatus status = User.ApprovalStatus.valueOf(statusStr.toUpperCase());
            User user = userService.updateUserApprovalStatus(userId, status);
            
            String message = switch (status) {
                case APPROVED -> "사용자가 승인되었습니다.";
                case REJECTED -> "사용자가 거부되었습니다.";
                case PENDING -> "사용자가 대기 상태로 변경되었습니다.";
            };
            
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "approvalStatus", user.getApprovalStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_STATUS", "message", "올바른 승인 상태를 입력해주세요."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "APPROVAL_UPDATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "승인 상태 업데이트 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{userId}")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUserByAdmin(userId);
            return ResponseEntity.ok(Map.of("message", "사용자가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "DELETE_FORBIDDEN", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "USER_DELETE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "사용자 삭제 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/pending")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> getPendingUsers() {
        try {
            List<User> pendingUsers = userService.getPendingUsers();
            return ResponseEntity.ok(Map.of(
                    "users", pendingUsers,
                    "count", pendingUsers.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "PENDING_USERS_FETCH_FAILED", "message", "승인 대기 사용자 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/inactive")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> getInactiveUsers() {
        try {
            List<User> inactiveUsers = userService.getInactiveUsers();
            return ResponseEntity.ok(Map.of(
                    "users", inactiveUsers,
                    "count", inactiveUsers.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INACTIVE_USERS_FETCH_FAILED", "message", "비활성 사용자 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/statistics")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> getUserStatistics() {
        try {
            Map<String, Object> statistics = userService.getUserStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "STATISTICS_FETCH_FAILED", "message", "사용자 통계 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/{userId}/reset-password")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "MISSING_PASSWORD", "message", "새 비밀번호를 입력해주세요."));
            }
            
            userService.resetUserPassword(userId, newPassword);
            return ResponseEntity.ok(Map.of("message", "사용자 비밀번호가 성공적으로 재설정되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PASSWORD_RESET_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/{userId}/unlock")
    @RequirePermission("USER_MANAGEMENT")
    public ResponseEntity<?> unlockUserAccount(@PathVariable Long userId) {
        try {
            userService.unlockUserAccount(userId);
            return ResponseEntity.ok(Map.of("message", "사용자 계정이 성공적으로 잠금 해제되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ACCOUNT_UNLOCK_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "계정 잠금 해제 중 오류가 발생했습니다."));
        }
    }
}