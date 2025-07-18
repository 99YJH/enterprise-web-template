package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.RoleResponse;
import com.enterprise.webtemplate.dto.RoleCreateRequest;
import com.enterprise.webtemplate.dto.RoleUpdateRequest;
import com.enterprise.webtemplate.dto.PermissionResponse;
import com.enterprise.webtemplate.dto.UserRoleUpdateRequest;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.Permission;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.PermissionRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    // 모든 역할 조회
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        validateAdminAccess();
        return roleRepository.findAll().stream()
                .map(RoleResponse::new)
                .collect(Collectors.toList());
    }

    // 역할 상세 조회
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        validateAdminAccess();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다."));
        return new RoleResponse(role);
    }

    // 역할 생성
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        validateAdminAccess();
        
        // 역할명 중복 검사
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 역할명입니다.");
        }
        
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        
        // 권한 설정
        if (request.getPermissionNames() != null && !request.getPermissionNames().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : request.getPermissionNames()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("권한을 찾을 수 없습니다: " + permissionName));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }
        
        Role savedRole = roleRepository.save(role);
        return new RoleResponse(savedRole);
    }

    // 역할 수정
    @Transactional
    public RoleResponse updateRole(Long roleId, RoleUpdateRequest request) {
        validateAdminAccess();
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다."));
        
        // 시스템 기본 역할 수정 제한
        if (isSystemRole(role.getName())) {
            throw new IllegalArgumentException("시스템 기본 역할은 수정할 수 없습니다.");
        }
        
        // 역할명 중복 검사 (기존 이름과 다른 경우에만)
        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("이미 존재하는 역할명입니다.");
            }
            role.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        
        // 권한 업데이트
        if (request.getPermissionNames() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : request.getPermissionNames()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("권한을 찾을 수 없습니다: " + permissionName));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }
        
        Role updatedRole = roleRepository.save(role);
        return new RoleResponse(updatedRole);
    }

    // 역할 삭제
    @Transactional
    public void deleteRole(Long roleId) {
        validateAdminAccess();
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다."));
        
        // 시스템 기본 역할 삭제 제한
        if (isSystemRole(role.getName())) {
            throw new IllegalArgumentException("시스템 기본 역할은 삭제할 수 없습니다.");
        }
        
        // 사용자가 할당된 역할인지 확인
        if (!role.getUsers().isEmpty()) {
            throw new IllegalArgumentException("사용자가 할당된 역할은 삭제할 수 없습니다.");
        }
        
        roleRepository.delete(role);
    }

    // 모든 권한 조회
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        validateAdminAccess();
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 권한 조회
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsByCategory() {
        validateAdminAccess();
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::new)
                .collect(Collectors.groupingBy(PermissionResponse::getCategory));
    }

    // 사용자 역할 업데이트
    @Transactional
    public void updateUserRoles(Long userId, Set<String> roleNames) {
        validateAdminAccess();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 마스터 사용자의 역할 변경 제한
        boolean isMasterUser = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("MASTER"));
        if (isMasterUser) {
            throw new IllegalArgumentException("마스터 사용자의 역할은 변경할 수 없습니다.");
        }
        
        // 새 역할 설정
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + roleName));
            newRoles.add(role);
        }
        
        user.setRoles(newRoles);
        userRepository.save(user);
    }

    // 역할별 사용자 조회
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleName) {
        validateAdminAccess();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다."));
        return role.getUsers().stream().collect(Collectors.toList());
    }

    // 권한 통계 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getRoleStatistics() {
        validateAdminAccess();
        
        Map<String, Object> statistics = Map.of(
            "totalRoles", roleRepository.count(),
            "totalPermissions", permissionRepository.count(),
            "roleUserCounts", roleRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            Role::getName,
                            role -> role.getUsers().size()
                    ))
        );
        
        return statistics;
    }

    private void validateAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증된 사용자를 찾을 수 없습니다.");
        }
        
        // 현재 사용자의 역할 확인
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        boolean hasAdminAccess = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MASTER"));
        
        if (!hasAdminAccess) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }

    private boolean isSystemRole(String roleName) {
        return Set.of("MASTER", "ADMIN", "USER").contains(roleName);
    }
}