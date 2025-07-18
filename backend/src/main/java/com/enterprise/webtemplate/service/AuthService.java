package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.LoginRequest;
import com.enterprise.webtemplate.dto.LoginResponse;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordService passwordService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().toLowerCase().trim();
        
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 계정 상태 검증
        validateUserAccount(user, loginRequest.getPassword());

        try {
            // 인증 수행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
            );

            // 로그인 성공 처리
            handleSuccessfulLogin(user);

            // JWT 토큰 생성
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    jwtService.getExpirationTime(),
                    user
            );

        } catch (BadCredentialsException e) {
            // 로그인 실패 처리
            handleFailedLogin(user);
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    private void validateUserAccount(User user, String password) {
        // 활성화 상태 확인
        if (!user.getIsActive()) {
            throw new DisabledException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        // 승인 상태 확인
        if (user.getApprovalStatus() != User.ApprovalStatus.APPROVED) {
            String message = switch (user.getApprovalStatus()) {
                case PENDING -> "계정 승인 대기 중입니다. 관리자의 승인을 기다려주세요.";
                case REJECTED -> "계정이 거부되었습니다. 관리자에게 문의하세요.";
                default -> "계정 상태를 확인할 수 없습니다.";
            };
            throw new DisabledException(message);
        }

        // 계정 잠금 상태 확인
        if (!user.isAccountNonLocked()) {
            throw new DisabledException(
                String.format("계정이 잠겨있습니다. %s 이후에 다시 시도해주세요.", 
                    user.getAccountLockedUntil())
            );
        }

        // 비밀번호 정책 검증 (선택적)
        if (isPasswordExpired(user)) {
            throw new BadCredentialsException("비밀번호가 만료되었습니다. 비밀번호를 변경해주세요.");
        }
    }

    private boolean isPasswordExpired(User user) {
        if (user.getPasswordChangedAt() == null) {
            return true; // 비밀번호 변경 이력이 없으면 만료로 간주
        }
        // 90일 정책 (필요에 따라 조정)
        return user.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(90));
    }

    @Transactional
    private void handleSuccessfulLogin(User user) {
        // 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        
        // 실패 카운트 초기화
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userRepository.save(user);
    }

    @Transactional
    private void handleFailedLogin(User user) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // 계정 잠금
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }

        userRepository.save(user);
    }

    public LoginResponse refreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                String newAccessToken = jwtService.generateToken(userDetails);
                String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

                return new LoginResponse(
                        newAccessToken,
                        newRefreshToken,
                        jwtService.getExpirationTime(),
                        user
                );
            } else {
                throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
            }
        } catch (Exception e) {
            throw new BadCredentialsException("토큰 갱신에 실패했습니다.");
        }
    }

    public void logout(String token) {
        // 토큰 블랙리스트 처리 (필요시 Redis 등을 사용하여 구현)
        // 현재는 클라이언트에서 토큰을 삭제하는 것으로 처리
    }
}