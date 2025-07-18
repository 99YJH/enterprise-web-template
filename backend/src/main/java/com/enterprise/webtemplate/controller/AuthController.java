package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.dto.LoginRequest;
import com.enterprise.webtemplate.dto.LoginResponse;
import com.enterprise.webtemplate.dto.RegisterRequest;
import com.enterprise.webtemplate.dto.RegisterResponse;
import com.enterprise.webtemplate.service.AuthService;
import com.enterprise.webtemplate.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_CREDENTIALS", "message", e.getMessage()));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ACCOUNT_DISABLED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "LOGIN_FAILED", "message", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "MISSING_REFRESH_TOKEN", "message", "리프레시 토큰이 필요합니다."));
            }

            LoginResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_REFRESH_TOKEN", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "REFRESH_FAILED", "message", "토큰 갱신 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logout(token);
            }
            return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "LOGOUT_FAILED", "message", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            // 현재 인증된 사용자 정보 반환
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "MISSING_TOKEN", "message", "인증 토큰이 필요합니다."));
            }

            // JWT 필터에서 이미 인증이 처리되므로 여기서는 성공 응답만 반환
            return ResponseEntity.ok(Map.of("message", "인증된 사용자입니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AUTH_CHECK_FAILED", "message", "사용자 인증 확인 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuthStatus() {
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "message", "인증 상태가 유효합니다."
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse response = userService.registerUser(registerRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "REGISTRATION_FAILED", "message", "회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        try {
            boolean isAvailable = userService.isEmailAvailable(email);
            return ResponseEntity.ok(Map.of(
                    "available", isAvailable,
                    "message", isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "EMAIL_CHECK_FAILED", "message", "이메일 확인 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> validateEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "MISSING_EMAIL", "message", "이메일을 입력해주세요."));
            }

            userService.checkEmailAvailability(email);
            return ResponseEntity.ok(Map.of("message", "사용 가능한 이메일입니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "EMAIL_NOT_AVAILABLE", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "EMAIL_VALIDATION_FAILED", "message", "이메일 검증 중 오류가 발생했습니다."));
        }
    }
}