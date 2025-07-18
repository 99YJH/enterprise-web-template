package com.enterprise.webtemplate.dto;

import com.enterprise.webtemplate.entity.Permission;
import com.enterprise.webtemplate.entity.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Set<String> permissionNames;
    private int userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoleResponse() {}

    public RoleResponse(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        this.userCount = role.getUsers().size();
        this.createdAt = role.getCreatedAt();
        this.updatedAt = role.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
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
}