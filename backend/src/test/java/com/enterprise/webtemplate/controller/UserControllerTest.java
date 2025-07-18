package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.dto.UserRegistrationRequest;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");
        testRole.setDescription("기본 사용자");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사용자");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(testRole));
        testUser.setPhoneNumber("010-1234-5678");
        testUser.setDepartment("개발팀");
        testUser.setPosition("개발자");

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("newuser@example.com");
        registrationRequest.setName("새로운 사용자");
        registrationRequest.setPassword("Password123!");
        registrationRequest.setPhoneNumber("010-1234-5678");
        registrationRequest.setDepartment("개발팀");
        registrationRequest.setPosition("개발자");
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testGetUsers_Success() throws Exception {
        // Given
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userService.getUsers(any(PageRequest.class), anyString(), anyString())).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.content[0].name").value("테스트 사용자"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).getUsers(any(PageRequest.class), eq("test"), isNull());
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testGetUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpected(jsonPath("$.name").value("테스트 사용자"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testGetUserById_NotFound() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));

        verify(userService).getUserById(1L);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("테스트 사용자"));

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("이미 존재하는 이메일입니다."));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void testRegisterUser_InvalidInput() throws Exception {
        // Given
        registrationRequest.setEmail("invalid-email");
        registrationRequest.setPassword("weak");

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testActivateUser_Success() throws Exception {
        // Given
        doNothing().when(userService).activateUser(1L);

        // When & Then
        mockMvc.perform(put("/api/users/1/activate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자가 활성화되었습니다."));

        verify(userService).activateUser(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testActivateUser_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("사용자를 찾을 수 없습니다.")).when(userService).activateUser(1L);

        // When & Then
        mockMvc.perform(put("/api/users/1/activate")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));

        verify(userService).activateUser(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testDeactivateUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser(1L);

        // When & Then
        mockMvc.perform(put("/api/users/1/deactivate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자가 비활성화되었습니다."));

        verify(userService).deactivateUser(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testDeleteUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자가 삭제되었습니다."));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_MANAGEMENT")
    void testDeleteUser_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("사용자를 찾을 수 없습니다.")).when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateProfile_Success() throws Exception {
        // Given
        when(userService.updateUserProfile(anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/1/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"수정된 이름\",\"phoneNumber\":\"010-9876-5432\",\"department\":\"마케팅팀\",\"position\":\"매니저\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 업데이트되었습니다."))
                .andExpect(jsonPath("$.user.name").value("테스트 사용자"));

        verify(userService).updateUserProfile(1L, "수정된 이름", "010-9876-5432", "마케팅팀", "매니저");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testChangePassword_Success() throws Exception {
        // Given
        doNothing().when(userService).changePassword(anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(put("/api/users/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"oldPassword\",\"newPassword\":\"newPassword123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));

        verify(userService).changePassword(1L, "oldPassword", "newPassword123!");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testChangePassword_WrongCurrentPassword() throws Exception {
        // Given
        doThrow(new RuntimeException("현재 비밀번호가 일치하지 않습니다."))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(put("/api/users/1/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"wrongPassword\",\"newPassword\":\"newPassword123!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));

        verify(userService).changePassword(1L, "wrongPassword", "newPassword123!");
    }

    @Test
    void testCheckEmail_Available() throws Exception {
        // Given
        when(userService.isEmailTaken("newuser@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/users/check-email")
                .param("email", "newuser@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(userService).isEmailTaken("newuser@example.com");
    }

    @Test
    void testCheckEmail_NotAvailable() throws Exception {
        // Given
        when(userService.isEmailTaken("test@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/users/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(userService).isEmailTaken("test@example.com");
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUsers(any(), anyString(), anyString());
    }

    @Test
    @WithMockUser(authorities = "READ_ONLY")
    void testInsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isForbidden());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }
}