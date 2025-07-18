package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.NotificationDTO;
import com.enterprise.webtemplate.entity.Notification;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.NotificationRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사용자");
        testUser.setIsActive(true);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTitle("테스트 알림");
        testNotification.setMessage("테스트 메시지");
        testNotification.setType(Notification.NotificationType.INFO);
        testNotification.setUser(testUser);
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());

        // SecurityContext 설정
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void testCreateNotification_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createNotification("테스트 제목", "테스트 메시지", "INFO", 1L);

        // Then
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }

    @Test
    void testCreateNotification_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.createNotification("테스트 제목", "테스트 메시지", "INFO", 1L);
        });

        verify(userRepository).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void testCreateBroadcastNotification_Success() {
        // Given
        List<User> activeUsers = List.of(testUser);
        when(userRepository.findByIsActive(true)).thenReturn(activeUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createBroadcastNotification("공지사항", "전체 공지", "ANNOUNCEMENT");

        // Then
        verify(userRepository).findByIsActive(true);
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }

    @Test
    void testCreateAdminNotification_Success() {
        // Given
        List<User> adminUsers = List.of(testUser);
        when(userRepository.findByRoleNames(List.of("ADMIN", "MASTER"))).thenReturn(adminUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createAdminNotification("관리자 알림", "중요한 알림", "SECURITY");

        // Then
        verify(userRepository).findByRoleNames(List.of("ADMIN", "MASTER"));
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }

    @Test
    void testGetNotifications_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Page<Notification> notificationPage = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUser(eq(testUser), any(Pageable.class))).thenReturn(notificationPage);

        // When
        Page<NotificationDTO> result = notificationService.getNotifications(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("테스트 알림", result.getContent().get(0).getTitle());

        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).findByUser(eq(testUser), any(Pageable.class));
    }

    @Test
    void testGetUnreadCount_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByUserAndIsRead(testUser, false)).thenReturn(5L);

        // When
        long result = notificationService.getUnreadCount();

        // Then
        assertEquals(5L, result);

        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).countByUserAndIsRead(testUser, false);
    }

    @Test
    void testMarkAsRead_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead(1L);

        // Then
        assertTrue(testNotification.getIsRead());

        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).findByIdAndUser(1L, testUser);
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void testMarkAsRead_NotificationNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(1L);
        });

        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).findByIdAndUser(1L, testUser);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testMarkAllAsRead_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        List<Notification> unreadNotifications = List.of(testNotification);
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(anyList())).thenReturn(unreadNotifications);

        // When
        notificationService.markAllAsRead();

        // Then
        assertTrue(testNotification.getIsRead());

        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).findByUserAndIsRead(testUser, false);
        verify(notificationRepository).saveAll(unreadNotifications);
    }

    @Test
    void testDeleteNotification_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testNotification));

        // When
        notificationService.deleteNotification(1L);

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).findByIdAndUser(1L, testUser);
        verify(notificationRepository).delete(testNotification);
    }

    @Test
    void testDeleteAllNotifications_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        List<Notification> userNotifications = List.of(testNotification);
        when(notificationRepository.findByUser(testUser)).thenReturn(userNotifications);

        // When
        notificationService.deleteAllNotifications();

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(notificationRepository).findByUser(testUser);
        verify(notificationRepository).deleteAll(userNotifications);
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.getUnreadCount();
        });
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.getUnreadCount();
        });

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testNotifyUserRegistration_Success() {
        // Given
        List<User> adminUsers = List.of(testUser);
        when(userRepository.findByRoleNames(List.of("ADMIN", "MASTER"))).thenReturn(adminUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.notifyUserRegistration("새로운 사용자", "newuser@example.com");

        // Then
        verify(userRepository).findByRoleNames(List.of("ADMIN", "MASTER"));
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }

    @Test
    void testNotifyUserApproval_Success() {
        // Given
        List<User> adminUsers = List.of(testUser);
        when(userRepository.findByRoleNames(List.of("ADMIN", "MASTER"))).thenReturn(adminUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.notifyUserApproval("승인된 사용자", "approved@example.com");

        // Then
        verify(userRepository).findByRoleNames(List.of("ADMIN", "MASTER"));
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }

    @Test
    void testNotifySystemUpdate_Success() {
        // Given
        List<User> activeUsers = List.of(testUser);
        when(userRepository.findByIsActive(true)).thenReturn(activeUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.notifySystemUpdate("시스템이 업데이트되었습니다.");

        // Then
        verify(userRepository).findByIsActive(true);
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }

    @Test
    void testNotifySecurityAlert_Success() {
        // Given
        List<User> adminUsers = List.of(testUser);
        when(userRepository.findByRoleNames(List.of("ADMIN", "MASTER"))).thenReturn(adminUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.notifySecurityAlert("보안 경고가 발생했습니다.");

        // Then
        verify(userRepository).findByRoleNames(List.of("ADMIN", "MASTER"));
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("test@example.com"),
                eq("/queue/notifications"),
                any(NotificationDTO.class)
        );
    }
}