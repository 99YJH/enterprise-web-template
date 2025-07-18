package com.enterprise.webtemplate.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class UserRoleUpdateRequest {

    @NotNull(message = "사용자 ID를 입력해주세요.")
    private Long userId;

    @NotEmpty(message = "역할을 선택해주세요.")
    private Set<String> roleNames;

    public UserRoleUpdateRequest() {}

    public UserRoleUpdateRequest(Long userId, Set<String> roleNames) {
        this.userId = userId;
        this.roleNames = roleNames;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }
}