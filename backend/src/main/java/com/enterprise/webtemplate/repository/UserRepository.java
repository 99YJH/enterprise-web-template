package com.enterprise.webtemplate.repository;

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
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.approvalStatus = :status")
    Page<User> findByApprovalStatus(@Param("status") User.ApprovalStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword% OR u.department LIKE %:keyword%")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date")
    Page<User> findInactiveUsers(@Param("date") LocalDateTime date, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > CURRENT_TIMESTAMP")
    Page<User> findLockedUsers(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewUsersFrom(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.approvalStatus = :status")
    long countByApprovalStatus(@Param("status") User.ApprovalStatus status);
    
    // 관리자용 사용자 검색 쿼리
    @Query("SELECT u FROM User u " +
           "WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:department IS NULL OR LOWER(u.department) LIKE LOWER(CONCAT('%', :department, '%'))) " +
           "AND (:position IS NULL OR LOWER(u.position) LIKE LOWER(CONCAT('%', :position, '%'))) " +
           "AND (:isActive IS NULL OR u.isActive = :isActive) " +
           "AND (:isEmailVerified IS NULL OR u.isEmailVerified = :isEmailVerified) " +
           "AND (:approvalStatus IS NULL OR u.approvalStatus = :approvalStatus)")
    Page<User> findUsersWithFilters(
            @Param("email") String email,
            @Param("name") String name,
            @Param("department") String department,
            @Param("position") String position,
            @Param("isActive") Boolean isActive,
            @Param("isEmailVerified") Boolean isEmailVerified,
            @Param("approvalStatus") User.ApprovalStatus approvalStatus,
            Pageable pageable
    );
    
    // 역할별 사용자 검색
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    // 승인 대기 사용자 목록
    @Query("SELECT u FROM User u WHERE u.approvalStatus = 'PENDING' ORDER BY u.createdAt ASC")
    List<User> findPendingUsers();
    
    // 비활성 사용자 목록
    @Query("SELECT u FROM User u WHERE u.isActive = false ORDER BY u.updatedAt DESC")
    List<User> findInactiveUsers();
    
    // 최근 로그인 사용자 목록
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NOT NULL ORDER BY u.lastLoginAt DESC")
    Page<User> findRecentlyLoggedInUsers(Pageable pageable);
    
    // 부서별 사용자 수
    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.department IS NOT NULL GROUP BY u.department")
    List<Object[]> countUsersByDepartment();
    
    // 역할별 사용자 수
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();

    // 대시보드 통계용 메서드들
    long countByIsActive(boolean isActive);
    
    long countByApprovalStatus(String approvalStatus);
    
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<User> findTop10ByOrderByCreatedAtDesc();
    
    List<User> findTop10ByApprovalStatusOrderByUpdatedAtDesc(String approvalStatus);
    
    // 알림 서비스용 메서드들
    List<User> findByIsActive(boolean isActive);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRoleNames(@Param("roleNames") List<String> roleNames);
}