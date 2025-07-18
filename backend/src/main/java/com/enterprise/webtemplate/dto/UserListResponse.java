package com.enterprise.webtemplate.dto;

import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserListResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String department;
    private String position;
    private String profileImageUrl;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private User.ApprovalStatus approvalStatus;
    private Set<String> roleNames;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer failedLoginAttempts;

    public UserListResponse() {}

    public UserListResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.department = user.getDepartment();
        this.position = user.getPosition();
        this.profileImageUrl = user.getProfileImageUrl();
        this.isActive = user.getIsActive();
        this.isEmailVerified = user.getIsEmailVerified();
        this.approvalStatus = user.getApprovalStatus();
        this.roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        this.lastLoginAt = user.getLastLoginAt();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.failedLoginAttempts = user.getFailedLoginAttempts();
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public User.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(User.ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    // 페이지네이션을 위한 정적 메서드
    public static class PageResponse {
        private List<UserListResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;

        public PageResponse() {}

        public PageResponse(Page<User> userPage) {
            this.content = userPage.getContent().stream()
                    .map(UserListResponse::new)
                    .collect(Collectors.toList());
            this.page = userPage.getNumber();
            this.size = userPage.getSize();
            this.totalElements = userPage.getTotalElements();
            this.totalPages = userPage.getTotalPages();
            this.first = userPage.isFirst();
            this.last = userPage.isLast();
            this.hasNext = userPage.hasNext();
            this.hasPrevious = userPage.hasPrevious();
        }

        // Getters and Setters
        public List<UserListResponse> getContent() {
            return content;
        }

        public void setContent(List<UserListResponse> content) {
            this.content = content;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
}