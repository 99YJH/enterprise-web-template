package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.annotation.RequirePermission;
import com.enterprise.webtemplate.dto.NotificationDTO;
import com.enterprise.webtemplate.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @RequirePermission("NOTIFICATION_READ")
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<NotificationDTO> notifications = notificationService.getNotifications(page, size);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "NOTIFICATIONS_FETCH_FAILED", "message", "알림 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/unread-count")
    @RequirePermission("NOTIFICATION_READ")
    public ResponseEntity<?> getUnreadCount() {
        try {
            long unreadCount = notificationService.getUnreadCount();
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "UNREAD_COUNT_FETCH_FAILED", "message", "읽지 않은 알림 수 조회 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{notificationId}/read")
    @RequirePermission("NOTIFICATION_READ")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("message", "알림을 읽음으로 표시했습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "NOTIFICATION_NOT_FOUND", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "NOTIFICATION_UPDATE_FAILED", "message", "알림 상태 업데이트 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/read-all")
    @RequirePermission("NOTIFICATION_READ")
    public ResponseEntity<?> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(Map.of("message", "모든 알림을 읽음으로 표시했습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "NOTIFICATIONS_UPDATE_FAILED", "message", "알림 상태 업데이트 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{notificationId}")
    @RequirePermission("NOTIFICATION_READ")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "NOTIFICATION_NOT_FOUND", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "NOTIFICATION_DELETE_FAILED", "message", "알림 삭제 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/all")
    @RequirePermission("NOTIFICATION_READ")
    public ResponseEntity<?> deleteAllNotifications() {
        try {
            notificationService.deleteAllNotifications();
            return ResponseEntity.ok(Map.of("message", "모든 알림이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "NOTIFICATIONS_DELETE_FAILED", "message", "알림 삭제 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/admin/send")
    @RequirePermission("NOTIFICATION_SEND")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String type = (String) request.get("type");
            String target = (String) request.get("target"); // "all", "admin", "user"
            Long userId = request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null;

            if (title == null || message == null || type == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "INVALID_REQUEST", "message", "제목, 메시지, 타입은 필수입니다."));
            }

            switch (target) {
                case "all":
                    notificationService.createBroadcastNotification(title, message, type);
                    break;
                case "admin":
                    notificationService.createAdminNotification(title, message, type);
                    break;
                case "user":
                    if (userId == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "INVALID_REQUEST", "message", "사용자 ID가 필요합니다."));
                    }
                    notificationService.createNotification(title, message, type, userId);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "INVALID_TARGET", "message", "올바른 대상을 선택해주세요."));
            }

            return ResponseEntity.ok(Map.of("message", "알림이 성공적으로 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "NOTIFICATION_SEND_FAILED", "message", "알림 전송 중 오류가 발생했습니다."));
        }
    }
}