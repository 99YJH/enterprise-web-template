package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.RegisterRequest;
import com.enterprise.webtemplate.dto.RegisterResponse;
import com.enterprise.webtemplate.dto.UserProfileResponse;
import com.enterprise.webtemplate.dto.UserProfileUpdateRequest;
import com.enterprise.webtemplate.dto.PasswordChangeRequest;
import com.enterprise.webtemplate.dto.UserListResponse;
import com.enterprise.webtemplate.dto.UserSearchRequest;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        // 입력값 검증
        validateRegistrationRequest(request);

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 정책 검증
        PasswordService.PasswordValidationResult passwordValidation = 
                passwordService.validatePassword(request.getPassword());
        if (!passwordValidation.isValid()) {
            throw new IllegalArgumentException(passwordValidation.getErrorMessage());
        }

        // 사용자 생성
        User user = createUser(request);

        // 기본 역할 할당 (USER)
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("기본 사용자 역할을 찾을 수 없습니다."));
        user.addRole(userRole);

        // 저장
        User savedUser = userRepository.save(user);

        String message = "회원가입이 완료되었습니다. 관리자의 승인을 기다려주세요.";
        return new RegisterResponse(savedUser, message);
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 형식 추가 검증
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        // 비즈니스 규칙에 따른 추가 검증
        email = email.toLowerCase().trim();
        if (email.length() > 100) {
            throw new IllegalArgumentException("이메일은 100자를 초과할 수 없습니다.");
        }

        // 이름 검증
        String name = request.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("이름은 2자 이상 입력해주세요.");
        }
    }

    private User createUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordService.encodePassword(request.getPassword()));
        user.setName(request.getName().trim());
        
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }
        
        if (request.getDepartment() != null && !request.getDepartment().trim().isEmpty()) {
            user.setDepartment(request.getDepartment().trim());
        }
        
        if (request.getPosition() != null && !request.getPosition().trim().isEmpty()) {
            user.setPosition(request.getPosition().trim());
        }

        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setApprovalStatus(User.ApprovalStatus.PENDING);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        return user;
    }

    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByEmail(email.toLowerCase().trim());
    }

    public void checkEmailAvailability(String email) {
        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public User updateApprovalStatus(Long userId, User.ApprovalStatus status) {
        User user = findById(userId);
        user.setApprovalStatus(status);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserActivation(Long userId) {
        User user = findById(userId);
        user.setIsActive(!user.getIsActive());
        return userRepository.save(user);
    }

    // 개인정보 관리 관련 메서드
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = getCurrentAuthenticatedUser();
        return new UserProfileResponse(currentUser);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UserProfileUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        
        // 입력값 검증 및 업데이트
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (request.getName().trim().length() < 2) {
                throw new IllegalArgumentException("이름은 2자 이상 입력해주세요.");
            }
            currentUser.setName(request.getName().trim());
        }
        
        if (request.getPhone() != null) {
            currentUser.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }
        
        if (request.getDepartment() != null) {
            currentUser.setDepartment(request.getDepartment().trim().isEmpty() ? null : request.getDepartment().trim());
        }
        
        if (request.getPosition() != null) {
            currentUser.setPosition(request.getPosition().trim().isEmpty() ? null : request.getPosition().trim());
        }

        User updatedUser = userRepository.save(currentUser);
        return new UserProfileResponse(updatedUser);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        
        // 입력값 검증
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
        
        if (!request.isDifferentFromCurrent()) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        
        // 새 비밀번호 정책 검증
        PasswordService.PasswordValidationResult passwordValidation = 
                passwordService.validatePassword(request.getNewPassword());
        if (!passwordValidation.isValid()) {
            throw new IllegalArgumentException(passwordValidation.getErrorMessage());
        }
        
        // 비밀번호 변경
        currentUser.setPassword(passwordService.encodePassword(request.getNewPassword()));
        currentUser.setPasswordChangedAt(LocalDateTime.now());
        
        userRepository.save(currentUser);
    }

    @Transactional
    public void updateProfileImage(String profileImageUrl) {
        User currentUser = getCurrentAuthenticatedUser();
        currentUser.setProfileImageUrl(profileImageUrl);
        userRepository.save(currentUser);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증된 사용자를 찾을 수 없습니다.");
        }
        
        String email = authentication.getName();
        return findByEmail(email);
    }

    // 관리자용 사용자 관리 메서드
    @Transactional(readOnly = true)
    public UserListResponse.PageResponse searchUsers(UserSearchRequest request) {
        // 정렬 설정
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<User> userPage;
        
        // 역할별 검색이 있는 경우
        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            userPage = userRepository.findByRoleName(request.getRoleName(), pageable);
        } else {
            // 일반 필터 검색
            userPage = userRepository.findUsersWithFilters(
                    request.getEmail(),
                    request.getName(),
                    request.getDepartment(),
                    request.getPosition(),
                    request.getIsActive(),
                    request.getIsEmailVerified(),
                    request.getApprovalStatus(),
                    pageable
            );
        }
        
        return new UserListResponse.PageResponse(userPage);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long userId) {
        User user = findById(userId);
        return new UserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserByAdmin(Long userId, UserProfileUpdateRequest request) {
        User user = findById(userId);
        
        // 관리자 권한 확인
        validateAdminAccess();
        
        // 입력값 검증 및 업데이트
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (request.getName().trim().length() < 2) {
                throw new IllegalArgumentException("이름은 2자 이상 입력해주세요.");
            }
            user.setName(request.getName().trim());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }
        
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment().trim().isEmpty() ? null : request.getDepartment().trim());
        }
        
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition().trim().isEmpty() ? null : request.getPosition().trim());
        }

        User updatedUser = userRepository.save(user);
        return new UserProfileResponse(updatedUser);
    }

    @Transactional
    public User toggleUserActivationByAdmin(Long userId) {
        validateAdminAccess();
        return toggleUserActivation(userId);
    }

    @Transactional
    public User updateUserApprovalStatus(Long userId, User.ApprovalStatus status) {
        validateAdminAccess();
        return updateApprovalStatus(userId, status);
    }

    @Transactional
    public void deleteUserByAdmin(Long userId) {
        validateAdminAccess();
        User user = findById(userId);
        
        // 마스터 계정 삭제 방지
        boolean isMaster = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("MASTER"));
        if (isMaster) {
            throw new IllegalArgumentException("마스터 계정은 삭제할 수 없습니다.");
        }
        
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<User> getPendingUsers() {
        validateAdminAccess();
        return userRepository.findPendingUsers();
    }

    @Transactional(readOnly = true)
    public List<User> getInactiveUsers() {
        validateAdminAccess();
        return userRepository.findInactiveUsers();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        validateAdminAccess();
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 기본 통계
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByApprovalStatus(User.ApprovalStatus.APPROVED);
        long pendingUsers = userRepository.countByApprovalStatus(User.ApprovalStatus.PENDING);
        long rejectedUsers = userRepository.countByApprovalStatus(User.ApprovalStatus.REJECTED);
        
        statistics.put("totalUsers", totalUsers);
        statistics.put("activeUsers", activeUsers);
        statistics.put("pendingUsers", pendingUsers);
        statistics.put("rejectedUsers", rejectedUsers);
        
        // 부서별 통계
        List<Object[]> departmentStats = userRepository.countUsersByDepartment();
        Map<String, Long> departmentCounts = new HashMap<>();
        for (Object[] stat : departmentStats) {
            departmentCounts.put((String) stat[0], (Long) stat[1]);
        }
        statistics.put("departmentStats", departmentCounts);
        
        // 역할별 통계
        List<Object[]> roleStats = userRepository.countUsersByRole();
        Map<String, Long> roleCounts = new HashMap<>();
        for (Object[] stat : roleStats) {
            roleCounts.put((String) stat[0], (Long) stat[1]);
        }
        statistics.put("roleStats", roleCounts);
        
        return statistics;
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        validateAdminAccess();
        User user = findById(userId);
        
        // 비밀번호 정책 검증
        PasswordService.PasswordValidationResult passwordValidation = 
                passwordService.validatePassword(newPassword);
        if (!passwordValidation.isValid()) {
            throw new IllegalArgumentException(passwordValidation.getErrorMessage());
        }
        
        user.setPassword(passwordService.encodePassword(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userRepository.save(user);
    }

    @Transactional
    public void unlockUserAccount(Long userId) {
        validateAdminAccess();
        User user = findById(userId);
        
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userRepository.save(user);
    }

    private void validateAdminAccess() {
        User currentUser = getCurrentAuthenticatedUser();
        boolean hasAdminAccess = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MASTER"));
        
        if (!hasAdminAccess) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }
}