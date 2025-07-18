package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.LoginRequest;
import com.enterprise.webtemplate.dto.LoginResponse;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.UserRepository;
import com.enterprise.webtemplate.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;
    private Authentication authentication;

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
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(testRole));

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setRememberMe(false);

        authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com",
            "password123",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void testLogin_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When
        LoginResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals("test@example.com", result.getUser().getEmail());
        assertEquals("테스트 사용자", result.getUser().getName());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_BadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testLogin_UserNotActive() {
        // Given
        testUser.setIsActive(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String token = "valid-jwt-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertTrue(result);
        verify(jwtTokenProvider).validateToken(token);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String token = "invalid-jwt-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertFalse(result);
        verify(jwtTokenProvider).validateToken(token);
    }

    @Test
    void testGetUserFromToken_ValidToken() {
        // Given
        String token = "valid-jwt-token";
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = authService.getUserFromToken(token);

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("테스트 사용자", result.get().getName());

        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testGetUserFromToken_UserNotFound() {
        // Given
        String token = "valid-jwt-token";
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = authService.getUserFromToken(token);

        // Then
        assertFalse(result.isPresent());

        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testRefreshToken_ValidToken() {
        // Given
        String oldToken = "valid-jwt-token";
        String newToken = "new-jwt-token";
        when(jwtTokenProvider.validateToken(oldToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(oldToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn(newToken);

        // When
        String result = authService.refreshToken(oldToken);

        // Then
        assertEquals(newToken, result);

        verify(jwtTokenProvider).validateToken(oldToken);
        verify(jwtTokenProvider).getUsernameFromToken(oldToken);
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Given
        String oldToken = "invalid-jwt-token";
        when(jwtTokenProvider.validateToken(oldToken)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(oldToken);
        });

        verify(jwtTokenProvider).validateToken(oldToken);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testLogin_WithRememberMe() {
        // Given
        loginRequest.setRememberMe(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When
        LoginResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertTrue(result.getExpiresIn() > 0); // Remember me should extend expiration

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void testLogout_Success() {
        // Given
        String token = "valid-jwt-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        // When
        authService.logout(token);

        // Then
        verify(jwtTokenProvider).validateToken(token);
        // 실제 구현에서는 토큰 블랙리스트 등의 로직이 있을 수 있음
    }
}