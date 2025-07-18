package com.enterprise.webtemplate.repository;

import com.enterprise.webtemplate.entity.Notification;
import com.enterprise.webtemplate.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser(User user, Pageable pageable);

    List<Notification> findByUser(User user);

    List<Notification> findByUserAndIsRead(User user, Boolean isRead);

    Optional<Notification> findByIdAndUser(Long id, User user);

    long countByUserAndIsRead(User user, Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotifications(@Param("user") User user);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndType(@Param("user") User user, @Param("type") Notification.NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.createdAt >= :since")
    long countRecentNotifications(@Param("user") User user, @Param("since") LocalDateTime since);

    void deleteByUserAndCreatedAtBefore(User user, LocalDateTime date);
}