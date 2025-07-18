package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.dto.UserProfileResponse;
import com.enterprise.webtemplate.dto.UserProfileUpdateRequest;
import com.enterprise.webtemplate.dto.PasswordChangeRequest;
import com.enterprise.webtemplate.service.UserService;
import com.enterprise.webtemplate.service.FileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            UserProfileResponse profile = userService.getCurrentUserProfile();
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PROFILE_FETCH_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "프로필 조회 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        try {
            UserProfileResponse updatedProfile = userService.updateUserProfile(request);
            return ResponseEntity.ok(Map.of(
                    "message", "프로필이 성공적으로 업데이트되었습니다.",
                    "profile", updatedProfile
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PROFILE_UPDATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "프로필 업데이트 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PASSWORD_CHANGE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "비밀번호 변경 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/me/profile-summary")
    public ResponseEntity<?> getProfileSummary() {
        try {
            UserProfileResponse profile = userService.getCurrentUserProfile();
            
            // 요약 정보만 반환
            Map<String, Object> summary = Map.of(
                    "name", profile.getName(),
                    "email", profile.getEmail(),
                    "department", profile.getDepartment() != null ? profile.getDepartment() : "",
                    "position", profile.getPosition() != null ? profile.getPosition() : "",
                    "isActive", profile.getIsActive(),
                    "approvalStatus", profile.getApprovalStatus(),
                    "roleNames", profile.getRoleNames(),
                    "lastLoginAt", profile.getLastLoginAt()
            );
            
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PROFILE_SUMMARY_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "프로필 요약 조회 중 오류가 발생했습니다."));
        }
    }
}