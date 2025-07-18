package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.dto.NotificationDTO;
import com.enterprise.webtemplate.entity.Notification;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.NotificationRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(String title, String message, String type, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(Notification.NotificationType.valueOf(type.toUpperCase()));
        notification.setUser(user);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);

        // WebSocket을 통해 실시간 알림 전송
        NotificationDTO notificationDTO = new NotificationDTO(savedNotification);
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                notificationDTO
        );
    }

    @Transactional
    public void createBroadcastNotification(String title, String message, String type) {
        List<User> activeUsers = userRepository.findByIsActive(true);
        
        for (User user : activeUsers) {
            createNotification(title, message, type, user.getId());
        }
    }

    @Transactional
    public void createAdminNotification(String title, String message, String type) {
        List<User> adminUsers = userRepository.findByRoleNames(List.of("ADMIN", "MASTER"));
        
        for (User user : adminUsers) {
            createNotification(title, message, type, user.getId());
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Notification> notifications = notificationRepository.findByUser(currentUser, pageable);
        return notifications.map(NotificationDTO::new);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByUserAndIsRead(currentUser, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUser(notificationId, currentUser)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsRead(currentUser, false);
        
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUser(notificationId, currentUser)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
        
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications() {
        User currentUser = getCurrentUser();
        List<Notification> userNotifications = notificationRepository.findByUser(currentUser);
        notificationRepository.deleteAll(userNotifications);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증된 사용자를 찾을 수 없습니다.");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 시스템 이벤트 알림 메서드들
    public void notifyUserRegistration(String userName, String userEmail) {
        createAdminNotification(
                "새로운 사용자 가입",
                String.format("%s (%s)님이 가입을 요청했습니다.", userName, userEmail),
                "USER_REGISTRATION"
        );
    }

    public void notifyUserApproval(String userName, String userEmail) {
        createAdminNotification(
                "사용자 승인 완료",
                String.format("%s (%s)님이 승인되었습니다.", userName, userEmail),
                "USER_APPROVAL"
        );
    }

    public void notifySystemUpdate(String updateInfo) {
        createBroadcastNotification(
                "시스템 업데이트",
                updateInfo,
                "SYSTEM"
        );
    }

    public void notifySecurityAlert(String alertMessage) {
        createAdminNotification(
                "보안 알림",
                alertMessage,
                "SECURITY"
        );
    }
}