package com.enterprise.webtemplate.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @Size(max = 20, message = "전화번호는 20자 이하로 입력해주세요.")
    private String phone;

    @Size(max = 100, message = "부서명은 100자 이하로 입력해주세요.")
    private String department;

    @Size(max = 100, message = "직책은 100자 이하로 입력해주세요.")
    private String position;

    public UserProfileUpdateRequest() {}

    public UserProfileUpdateRequest(String name, String phone, String department, String position) {
        this.name = name;
        this.phone = phone;
        this.department = department;
        this.position = position;
    }

    // Getters and Setters
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
}