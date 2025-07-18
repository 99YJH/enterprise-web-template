package com.enterprise.webtemplate.controller;

import com.enterprise.webtemplate.annotation.RequirePermission;
import com.enterprise.webtemplate.dto.FileUploadResponse;
import com.enterprise.webtemplate.entity.FileEntity;
import com.enterprise.webtemplate.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            FileEntity savedFile = fileService.uploadProfileImage(file);
            
            return ResponseEntity.ok(Map.of(
                    "message", "프로필 이미지가 성공적으로 업로드되었습니다.",
                    "fileId", savedFile.getId(),
                    "fileName", savedFile.getOriginalName(),
                    "fileUrl", "/api/files/" + savedFile.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "FILE_UPLOAD_FAILED", "message", "파일 업로드 중 오류가 발생했습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "시스템 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<?> deleteProfileImage() {
        try {
            fileService.deleteProfileImage();
            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "FILE_DELETE_FAILED", "message", "파일 삭제 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFile(@PathVariable Long fileId) {
        try {
            FileEntity fileEntity = fileService.getFileById(fileId);
            byte[] fileContent = fileService.getFileContent(fileId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileEntity.getContentType()));
            headers.setContentLength(fileContent.length);
            
            // 이미지 파일인 경우 인라인으로 표시, 그 외에는 다운로드
            if (fileEntity.getContentType().startsWith("image/")) {
                headers.setContentDispositionFormData("inline", fileEntity.getOriginalName());
            } else {
                headers.setContentDispositionFormData("attachment", fileEntity.getOriginalName());
            }
            
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "FILE_ACCESS_FAILED", "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "FILE_READ_FAILED", "message", "파일 읽기 중 오류가 발생했습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "시스템 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{fileId}/info")
    public ResponseEntity<?> getFileInfo(@PathVariable Long fileId) {
        try {
            FileEntity fileEntity = fileService.getFileById(fileId);
            
            return ResponseEntity.ok(Map.of(
                    "id", fileEntity.getId(),
                    "originalName", fileEntity.getOriginalName(),
                    "contentType", fileEntity.getContentType(),
                    "fileSize", fileEntity.getFileSize(),
                    "uploadedAt", fileEntity.getCreatedAt(),
                    "fileType", fileEntity.getFileType(),
                    "isPublic", fileEntity.getIsPublic()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "FILE_INFO_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "시스템 오류가 발생했습니다."));
        }
    }

    @PostMapping("/upload")
    @RequirePermission("FILE_UPLOAD")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "description", required = false) String description) {
        try {
            FileEntity fileEntity = fileService.uploadFile(file, isPublic, description);
            FileUploadResponse response = new FileUploadResponse(fileEntity);
            return ResponseEntity.ok(Map.of(
                "message", "파일이 성공적으로 업로드되었습니다.",
                "file", response
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "FILE_UPLOAD_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "파일 업로드 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/list")
    @RequirePermission("FILE_DOWNLOAD")
    public ResponseEntity<?> getFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isPublic) {
        try {
            Page<FileUploadResponse> files = fileService.getFiles(page, size, fileType, search, isPublic);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "FILES_FETCH_FAILED", "message", "파일 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{fileId}/download")
    @RequirePermission("FILE_DOWNLOAD")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            Resource resource = fileService.downloadFile(fileId);
            String fileName = resource.getFilename();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    @RequirePermission("FILE_DELETE")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
            return ResponseEntity.ok(Map.of("message", "파일이 삭제되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "FILE_NOT_FOUND", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "FILE_DELETE_FAILED", "message", "파일 삭제 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/my-files")
    @RequirePermission("FILE_DOWNLOAD")
    public ResponseEntity<?> getMyFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fileType) {
        try {
            Page<FileUploadResponse> files = fileService.getMyFiles(page, size, fileType);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "MY_FILES_FETCH_FAILED", "message", "내 파일 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/statistics")
    @RequirePermission("FILE_MANAGEMENT")
    public ResponseEntity<?> getFileStatistics() {
        try {
            Map<String, Object> statistics = fileService.getFileStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "FILE_STATISTICS_FAILED", "message", "파일 통계 조회 중 오류가 발생했습니다."));
        }
    }
}