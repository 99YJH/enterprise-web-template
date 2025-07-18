package com.enterprise.webtemplate.repository;

import com.enterprise.webtemplate.entity.FileEntity;
import com.enterprise.webtemplate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long>, JpaSpecificationExecutor<FileEntity> {

    List<FileEntity> findByUploadedByAndFileTypeAndDeletedAtIsNull(User uploadedBy, FileEntity.FileType fileType);

    List<FileEntity> findByUploadedByAndDeletedAtIsNull(User uploadedBy);

    List<FileEntity> findByFileTypeAndDeletedAtIsNull(FileEntity.FileType fileType);

    Optional<FileEntity> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT f FROM FileEntity f WHERE f.uploadedBy = :user AND f.fileType = :fileType AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<FileEntity> findLatestFilesByUserAndType(@Param("user") User user, @Param("fileType") FileEntity.FileType fileType);

    @Query("SELECT f FROM FileEntity f WHERE f.isPublic = true AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<FileEntity> findPublicFiles();

    @Query("SELECT COUNT(f) FROM FileEntity f WHERE f.uploadedBy = :user AND f.deletedAt IS NULL")
    long countByUploadedByAndDeletedAtIsNull(@Param("user") User user);

    @Query("SELECT SUM(f.fileSize) FROM FileEntity f WHERE f.uploadedBy = :user AND f.deletedAt IS NULL")
    Long sumFileSizeByUploadedByAndDeletedAtIsNull(@Param("user") User user);
    
    // 새로 추가된 메소드들
    long countByDeletedAtIsNull();
    
    long countByIsPublicAndDeletedAtIsNull(boolean isPublic);
    
    long countByFileTypeAndDeletedAtIsNull(FileEntity.FileType fileType);
    
    @Query("SELECT SUM(f.fileSize) FROM FileEntity f WHERE f.deletedAt IS NULL")
    Long sumFileSizeByDeletedAtIsNull();
    
    long countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime createdAt);
}