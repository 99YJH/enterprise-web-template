package com.enterprise.webtemplate.dto;

import com.enterprise.webtemplate.entity.Permission;

public class PermissionResponse {

    private Long id;
    private String name;
    private String description;
    private String category;

    public PermissionResponse() {}

    public PermissionResponse(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.description = permission.getDescription();
        this.category = permission.getCategory();
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}