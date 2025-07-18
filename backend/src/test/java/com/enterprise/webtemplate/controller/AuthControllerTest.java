package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.dto.LoginRequest;
import com.enterprise.webtemplate.dto.LoginResponse;
import com.enterprise.webtemplate.dto.RegisterRequest;
import com.enterprise.webtemplate.dto.RegisterResponse;
import com.enterprise.webtemplate.service.AuthService;
import com.enterprise.webtemplate.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private RegisterRequest registerRequest;
    private RegisterResponse registerResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setRememberMe(false);

        loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token");
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(3600L);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setName("새로운 사용자");
        registerRequest.setPassword("Password123!");
        registerRequest.setPhoneNumber("010-1234-5678");
        registerRequest.setDepartment("개발팀");
        registerRequest.setPosition("개발자");

        registerResponse = new RegisterResponse();
        registerResponse.setMessage("회원가입이 성공적으로 완료되었습니다.");
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_AccountDisabled() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new DisabledException("Account is disabled"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ACCOUNT_DISABLED"))
                .andExpect(jsonPath("$.message").value("Account is disabled"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_InternalError() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("로그인 처리 중 오류가 발생했습니다."));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        // Given
        when(authService.refreshToken(anyString())).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"valid-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService).refreshToken("valid-refresh-token");
    }

    @Test
    void testRefreshToken_MissingToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 필요합니다."));

        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        // Given
        when(authService.refreshToken(anyString()))
                .thenThrow(new BadCredentialsException("Invalid refresh token"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"invalid-refresh-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        verify(authService).refreshToken("invalid-refresh-token");
    }

    @Test
    void testLogout_Success() throws Exception {
        // Given
        doNothing().when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));

        verify(authService).logout("jwt-token");
    }

    @Test
    void testLogout_NoAuthHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));

        verify(authService, never()).logout(anyString());
    }

    @Test
    void testLogout_Error() throws Exception {
        // Given
        doThrow(new RuntimeException("Logout failed")).when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("LOGOUT_FAILED"))
                .andExpect(jsonPath("$.message").value("로그아웃 처리 중 오류가 발생했습니다."));

        verify(authService).logout("jwt-token");
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증된 사용자입니다."));
    }

    @Test
    void testGetCurrentUser_MissingToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증 토큰이 필요합니다."));
    }

    @Test
    void testGetCurrentUser_InvalidTokenFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Invalid token format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증 토큰이 필요합니다."));
    }

    @Test
    @WithMockUser
    void testCheckAuthStatus_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.message").value("인증 상태가 유효합니다."));
    }

    @Test
    void testRegister_Success() throws Exception {
        // Given
        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(registerResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));

        verify(userService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testRegister_ValidationError() throws Exception {
        // Given
        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("이미 존재하는 이메일입니다."));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));

        verify(userService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testRegister_InternalError() throws Exception {
        // Given
        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("REGISTRATION_FAILED"))
                .andExpect(jsonPath("$.message").value("회원가입 처리 중 오류가 발생했습니다."));

        verify(userService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testCheckEmailAvailability_Available() throws Exception {
        // Given
        when(userService.isEmailAvailable("newuser@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "newuser@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."));

        verify(userService).isEmailAvailable("newuser@example.com");
    }

    @Test
    void testCheckEmailAvailability_NotAvailable() throws Exception {
        // Given
        when(userService.isEmailAvailable("test@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));

        verify(userService).isEmailAvailable("test@example.com");
    }

    @Test
    void testCheckEmailAvailability_Error() throws Exception {
        // Given
        when(userService.isEmailAvailable(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("EMAIL_CHECK_FAILED"))
                .andExpect(jsonPath("$.message").value("이메일 확인 중 오류가 발생했습니다."));

        verify(userService).isEmailAvailable("test@example.com");
    }

    @Test
    void testValidateEmail_Success() throws Exception {
        // Given
        doNothing().when(userService).checkEmailAvailability("newuser@example.com");

        // When & Then
        mockMvc.perform(post("/api/auth/check-email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"newuser@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."));

        verify(userService).checkEmailAvailability("newuser@example.com");
    }

    @Test
    void testValidateEmail_MissingEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/check-email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_EMAIL"))
                .andExpect(jsonPath("$.message").value("이메일을 입력해주세요."));

        verify(userService, never()).checkEmailAvailability(anyString());
    }

    @Test
    void testValidateEmail_NotAvailable() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."))
                .when(userService).checkEmailAvailability("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/auth/check-email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_NOT_AVAILABLE"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));

        verify(userService).checkEmailAvailability("test@example.com");
    }

    @Test
    void testValidateEmail_Error() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(userService).checkEmailAvailability(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/check-email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("EMAIL_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("이메일 검증 중 오류가 발생했습니다."));

        verify(userService).checkEmailAvailability("test@example.com");
    }
}