package com.enterprise.webtemplate.config;

import com.enterprise.webtemplate.entity.Permission;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.PermissionRepository;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import com.enterprise.webtemplate.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializePermissions();
        initializeRoles();
        initializeMasterAccount();
    }

    private void initializePermissions() {
        List<Permission> systemPermissions = createSystemPermissions();
        
        for (Permission permission : systemPermissions) {
            if (!permissionRepository.existsByName(permission.getName())) {
                permissionRepository.save(permission);
            }
        }
    }

    private List<Permission> createSystemPermissions() {
        List<Permission> permissions = new ArrayList<>();
        
        // 시스템 관리 권한
        permissions.add(new Permission("SUPER_ADMIN", "최고 관리자 권한", "SYSTEM", "ALL", true));
        permissions.add(new Permission("ADMIN_ACCESS", "관리자 접근 권한", "ADMIN", "ACCESS", true));
        permissions.add(new Permission("SYSTEM_CONFIG", "시스템 설정 권한", "SYSTEM", "CONFIG", true));
        
        // 사용자 관리 권한
        permissions.add(new Permission("USER_MANAGEMENT", "사용자 관리 권한", "USER", "MANAGEMENT", true));
        permissions.add(new Permission("USER_CREATE", "사용자 생성 권한", "USER", "CREATE", true));
        permissions.add(new Permission("USER_READ", "사용자 조회 권한", "USER", "READ", true));
        permissions.add(new Permission("USER_UPDATE", "사용자 수정 권한", "USER", "UPDATE", true));
        permissions.add(new Permission("USER_DELETE", "사용자 삭제 권한", "USER", "DELETE", true));
        permissions.add(new Permission("USER_APPROVE", "사용자 승인 권한", "USER", "APPROVE", true));
        
        // 역할 및 권한 관리
        permissions.add(new Permission("ROLE_MANAGEMENT", "역할 관리 권한", "ROLE", "MANAGEMENT", true));
        permissions.add(new Permission("PERMISSION_MANAGEMENT", "권한 관리 권한", "PERMISSION", "MANAGEMENT", true));
        
        // 파일 관리 권한
        permissions.add(new Permission("FILE_UPLOAD", "파일 업로드 권한", "FILE", "UPLOAD", true));
        permissions.add(new Permission("FILE_DOWNLOAD", "파일 다운로드 권한", "FILE", "DOWNLOAD", true));
        permissions.add(new Permission("FILE_MANAGEMENT", "파일 관리 권한", "FILE", "MANAGEMENT", true));
        permissions.add(new Permission("FILE_DELETE", "파일 삭제 권한", "FILE", "DELETE", true));
        
        // 알림 관리 권한
        permissions.add(new Permission("NOTIFICATION_READ", "알림 조회 권한", "NOTIFICATION", "READ", true));
        permissions.add(new Permission("NOTIFICATION_SEND", "알림 발송 권한", "NOTIFICATION", "SEND", true));
        permissions.add(new Permission("NOTIFICATION_MANAGEMENT", "알림 관리 권한", "NOTIFICATION", "MANAGEMENT", true));
        
        // 기본 사용자 권한
        permissions.add(new Permission("PROFILE_READ", "프로필 조회 권한", "PROFILE", "READ", true));
        permissions.add(new Permission("PROFILE_UPDATE", "프로필 수정 권한", "PROFILE", "UPDATE", true));
        permissions.add(new Permission("PASSWORD_CHANGE", "비밀번호 변경 권한", "PASSWORD", "CHANGE", true));
        
        // 대시보드 권한
        permissions.add(new Permission("DASHBOARD_VIEW", "대시보드 조회 권한", "DASHBOARD", "VIEW", true));
        
        return permissions;
    }

    private void initializeRoles() {
        // 최고 관리자 역할
        if (!roleRepository.existsByName("SUPER_ADMIN")) {
            Role superAdminRole = new Role("SUPER_ADMIN", "최고 관리자", true);
            List<Permission> allPermissions = permissionRepository.findAll();
            for (Permission permission : allPermissions) {
                superAdminRole.addPermission(permission);
            }
            roleRepository.save(superAdminRole);
        }

        // 관리자 역할
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role("ADMIN", "관리자", true);
            List<String> adminPermissions = List.of(
                "ADMIN_ACCESS", "USER_MANAGEMENT", "USER_CREATE", "USER_READ", 
                "USER_UPDATE", "USER_APPROVE", "ROLE_MANAGEMENT", 
                "FILE_MANAGEMENT", "NOTIFICATION_MANAGEMENT", "NOTIFICATION_SEND",
                "PROFILE_READ", "PROFILE_UPDATE", "PASSWORD_CHANGE", "DASHBOARD_VIEW"
            );
            addPermissionsToRole(adminRole, adminPermissions);
            roleRepository.save(adminRole);
        }

        // 일반 사용자 역할
        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role("USER", "일반 사용자", true);
            List<String> userPermissions = List.of(
                "FILE_UPLOAD", "FILE_DOWNLOAD", "NOTIFICATION_READ",
                "PROFILE_READ", "PROFILE_UPDATE", "PASSWORD_CHANGE", "DASHBOARD_VIEW"
            );
            addPermissionsToRole(userRole, userPermissions);
            roleRepository.save(userRole);
        }
    }

    private void addPermissionsToRole(Role role, List<String> permissionNames) {
        for (String permissionName : permissionNames) {
            permissionRepository.findByName(permissionName)
                    .ifPresent(role::addPermission);
        }
    }

    private void initializeMasterAccount() {
        if (!userRepository.existsByEmail("master@enterprise.com")) {
            User masterUser = new User();
            masterUser.setEmail("master@enterprise.com");
            masterUser.setPassword(passwordService.encodePassword("Master@123"));
            masterUser.setName("시스템 관리자");
            masterUser.setDepartment("IT");
            masterUser.setPosition("시스템 관리자");
            masterUser.setIsActive(true);
            masterUser.setIsEmailVerified(true);
            masterUser.setApprovalStatus(User.ApprovalStatus.APPROVED);
            masterUser.setPasswordChangedAt(LocalDateTime.now());
            
            // 최고 관리자 역할 할당
            roleRepository.findByName("SUPER_ADMIN")
                    .ifPresent(masterUser::addRole);
            
            userRepository.save(masterUser);
        }
    }
}