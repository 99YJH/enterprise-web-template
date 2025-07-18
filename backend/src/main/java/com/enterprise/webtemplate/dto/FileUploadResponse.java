package com.enterprise.webtemplate.dto;

import com.enterprise.webtemplate.entity.FileEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class FileUploadResponse {
    private Long id;
    private String originalName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String downloadUrl;
    private Boolean isPublic;
    private String uploadedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public FileUploadResponse() {}

    public FileUploadResponse(FileEntity fileEntity) {
        this.id = fileEntity.getId();
        this.originalName = fileEntity.getOriginalFilename();
        this.fileName = fileEntity.getStoredFilename();
        this.fileType = fileEntity.getFileType().name();
        this.fileSize = fileEntity.getFileSize();
        this.filePath = fileEntity.getFilePath();
        this.downloadUrl = "/api/files/" + fileEntity.getId() + "/download";
        this.isPublic = fileEntity.getIsPublic();
        this.uploadedBy = fileEntity.getUploadedBy().getName();
        this.createdAt = fileEntity.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}