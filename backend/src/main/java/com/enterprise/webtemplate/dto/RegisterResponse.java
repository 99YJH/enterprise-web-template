package com.enterprise.webtemplate.dto;

import com.enterprise.webtemplate.entity.User;

import java.time.LocalDateTime;

public class RegisterResponse {

    private Long id;
    private String email;
    private String name;
    private String department;
    private String position;
    private User.ApprovalStatus approvalStatus;
    private boolean isActive;
    private LocalDateTime createdAt;
    private String message;

    public RegisterResponse() {}

    public RegisterResponse(User user, String message) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.department = user.getDepartment();
        this.position = user.getPosition();
        this.approvalStatus = user.getApprovalStatus();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
        this.message = message;
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

    public User.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(User.ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}