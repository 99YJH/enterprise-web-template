package com.enterprise.webtemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class RoleCreateRequest {

    @NotBlank(message = "역할명을 입력해주세요.")
    @Size(min = 2, max = 50, message = "역할명은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @Size(max = 200, message = "설명은 200자 이하로 입력해주세요.")
    private String description;

    private Set<String> permissionNames;

    public RoleCreateRequest() {}

    public RoleCreateRequest(String name, String description, Set<String> permissionNames) {
        this.name = name;
        this.description = description;
        this.permissionNames = permissionNames;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getPermissionNames() {
        return permissionNames;
    }

    public void setPermissionNames(Set<String> permissionNames) {
        this.permissionNames = permissionNames;
    }
}