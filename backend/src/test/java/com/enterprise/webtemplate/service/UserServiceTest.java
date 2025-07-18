package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.UserRegistrationRequest;
import com.enterprise.webtemplate.entity.Role;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.RoleRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(testRole));

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("newuser@example.com");
        registrationRequest.setName("새로운 사용자");
        registrationRequest.setPassword("Password123!");
        registrationRequest.setPhoneNumber("010-1234-5678");
        registrationRequest.setDepartment("개발팀");
        registrationRequest.setPosition("개발자");
    }

    @Test
    void testRegisterUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("테스트 사용자", result.getName());
        assertTrue(result.getIsActive());
        
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindByEmail_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("테스트 사용자", result.get().getName());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testFindByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail("notfound@example.com");

        // Then
        assertFalse(result.isPresent());

        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void testActivateUser_Success() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.activateUser(1L);

        // Then
        assertTrue(testUser.getIsActive());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void testActivateUser_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.activateUser(1L);
        });

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeactivateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(1L);

        // Then
        assertFalse(testUser.getIsActive());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserProfile_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUserProfile(1L, "새로운 이름", "010-9876-5432", "마케팅팀", "매니저");

        // Then
        assertNotNull(result);
        assertEquals("새로운 이름", result.getName());
        assertEquals("010-9876-5432", result.getPhoneNumber());
        assertEquals("마케팅팀", result.getDepartment());
        assertEquals("매니저", result.getPosition());

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangePassword_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.changePassword(1L, "oldPassword", "newPassword123!");

        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword123!");
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.changePassword(1L, "wrongPassword", "newPassword123!");
        });

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testIsEmailTaken_True() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        boolean result = userService.isEmailTaken("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void testIsEmailTaken_False() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // When
        boolean result = userService.isEmailTaken("newuser@example.com");

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail("newuser@example.com");
    }
}