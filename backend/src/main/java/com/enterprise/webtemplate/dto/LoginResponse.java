package com.enterprise.webtemplate.dto;

import com.enterprise.webtemplate.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserInfo user;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, long expiresIn, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = new UserInfo(user);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
        private String department;
        private String position;
        private String profileImageUrl;
        private boolean isActive;
        private User.ApprovalStatus approvalStatus;
        private LocalDateTime lastLoginAt;
        private Set<String> roles;
        private Set<String> permissions;

        public UserInfo() {}

        public UserInfo(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.name = user.getName();
            this.department = user.getDepartment();
            this.position = user.getPosition();
            this.profileImageUrl = user.getProfileImageUrl();
            this.isActive = user.getIsActive();
            this.approvalStatus = user.getApprovalStatus();
            this.lastLoginAt = user.getLastLoginAt();
            this.roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
            this.permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet());
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public User.ApprovalStatus getApprovalStatus() {
            return approvalStatus;
        }

        public void setApprovalStatus(User.ApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
        }

        public LocalDateTime getLastLoginAt() {
            return lastLoginAt;
        }

        public void setLastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }
    }
}