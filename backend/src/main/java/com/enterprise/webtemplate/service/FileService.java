package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.entity.FileEntity;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.FileRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.enterprise.webtemplate.dto.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}") // 10MB
    private long maxFileSize;

    @Value("${app.file.allowed-types:image/jpeg,image/png,image/gif,image/webp,application/pdf,text/plain,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document}")
    private String allowedTypes;
    
    @Value("${app.file.general-upload-dir:general-files}")
    private String generalUploadDir;
    
    @Value("${app.file.max-general-size:52428800}") // 50MB
    private long maxGeneralFileSize;

    @Value("${app.file.profile-image-dir:profile-images}")
    private String profileImageDir;

    private static final List<String> ALLOWED_PROFILE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_GENERAL_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp", "pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    @Transactional
    public FileEntity uploadProfileImage(MultipartFile file) throws IOException {
        // 현재 사용자 확인
        User currentUser = getCurrentAuthenticatedUser();
        
        // 파일 검증
        validateFile(file);
        
        // 기존 프로필 이미지 삭제
        deleteExistingProfileImage(currentUser);
        
        // 파일 저장
        String savedFileName = saveFile(file, profileImageDir);
        
        // 데이터베이스에 저장
        FileEntity fileEntity = new FileEntity();
        fileEntity.setOriginalFilename(file.getOriginalFilename());
        fileEntity.setStoredFilename(savedFileName);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setFilePath(profileImageDir + "/" + savedFileName);
        fileEntity.setUploadedBy(currentUser);
        fileEntity.setFileType(FileEntity.FileType.PROFILE_IMAGE);
        fileEntity.setIsPublic(false);
        fileEntity.setScanStatus(FileEntity.ScanStatus.PENDING);
        
        FileEntity savedFile = fileRepository.save(fileEntity);
        
        // 사용자 프로필 이미지 URL 업데이트
        currentUser.setProfileImageUrl("/api/files/" + savedFile.getId());
        userRepository.save(currentUser);
        
        return savedFile;
    }

    @Transactional
    public void deleteProfileImage() {
        User currentUser = getCurrentAuthenticatedUser();
        deleteExistingProfileImage(currentUser);
        
        // 사용자 프로필 이미지 URL 제거
        currentUser.setProfileImageUrl(null);
        userRepository.save(currentUser);
    }

    @Transactional(readOnly = true)
    public FileEntity getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public byte[] getFileContent(Long fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        
        // 파일 접근 권한 확인
        validateFileAccess(fileEntity);
        
        Path filePath = Paths.get(uploadDir, fileEntity.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("파일이 존재하지 않습니다.");
        }
        
        return Files.readAllBytes(filePath);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일을 선택해주세요.");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes.split(",")).contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (JPEG, PNG, GIF, WebP만 가능)");
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_PROFILE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다.");
        }
    }

    private String saveFile(MultipartFile file, String subDir) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir, subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
        
        // 파일 저장
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return uniqueFilename;
    }

    private void deleteExistingProfileImage(User user) {
        // 기존 프로필 이미지 파일 엔티티 찾기
        List<FileEntity> existingFiles = fileRepository.findByUploadedByAndFileTypeAndDeletedAtIsNull(
                user, FileEntity.FileType.PROFILE_IMAGE);
        
        for (FileEntity existingFile : existingFiles) {
            // 물리적 파일 삭제
            try {
                Path filePath = Paths.get(uploadDir, existingFile.getUploadPath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // 로그 남기고 계속 진행
                System.err.println("파일 삭제 실패: " + existingFile.getFilePath());
            }
            
            // 소프트 삭제
            existingFile.setDeletedAt(LocalDateTime.now());
            fileRepository.save(existingFile);
        }
    }

    private void validateFileAccess(FileEntity fileEntity) {
        User currentUser = getCurrentAuthenticatedUser();
        
        // 공개 파일이거나 본인이 업로드한 파일인 경우 접근 허용
        if (!fileEntity.getIsPublic() && !fileEntity.getUploadedBy().getId().equals(currentUser.getId())) {
            // 관리자 권한 확인
            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MASTER"));
            
            if (!isAdmin) {
                throw new RuntimeException("파일에 접근할 권한이 없습니다.");
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    @Transactional
    public FileEntity uploadFile(MultipartFile file, boolean isPublic, String description) throws IOException {
        // 현재 사용자 확인
        User currentUser = getCurrentAuthenticatedUser();
        
        // 파일 검증
        validateGeneralFile(file);
        
        // 파일 저장
        String savedFileName = saveFile(file, generalUploadDir);
        
        // 데이터베이스에 저장
        FileEntity fileEntity = new FileEntity();
        fileEntity.setOriginalFilename(file.getOriginalFilename());
        fileEntity.setStoredFilename(savedFileName);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setFilePath(generalUploadDir + "/" + savedFileName);
        fileEntity.setUploadedBy(currentUser);
        fileEntity.setFileType(determineFileType(file.getContentType()));
        fileEntity.setIsPublic(isPublic);
        fileEntity.setDescription(description);
        fileEntity.setScanStatus(FileEntity.ScanStatus.PENDING);
        
        return fileRepository.save(fileEntity);
    }
    
    @Transactional(readOnly = true)
    public Page<FileUploadResponse> getFiles(int page, int size, String fileType, String search, Boolean isPublic) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Specification<FileEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 삭제되지 않은 파일만 조회
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            // 프로필 이미지 제외
            predicates.add(criteriaBuilder.notEqual(root.get("fileType"), FileEntity.FileType.PROFILE_IMAGE));
            
            // 파일 타입 필터
            if (StringUtils.hasText(fileType)) {
                predicates.add(criteriaBuilder.equal(root.get("fileType"), FileEntity.FileType.valueOf(fileType.toUpperCase())));
            }
            
            // 검색 조건
            if (StringUtils.hasText(search)) {
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("originalName")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                );
                predicates.add(searchPredicate);
            }
            
            // 공개/비공개 필터
            if (isPublic != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), isPublic));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<FileEntity> fileEntities = fileRepository.findAll(spec, pageable);
        return fileEntities.map(FileUploadResponse::new);
    }
    
    @Transactional(readOnly = true)
    public Page<FileUploadResponse> getMyFiles(int page, int size, String fileType) {
        User currentUser = getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Specification<FileEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 삭제되지 않은 파일만 조회
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            // 현재 사용자가 업로드한 파일만
            predicates.add(criteriaBuilder.equal(root.get("uploadedBy"), currentUser));
            
            // 파일 타입 필터
            if (StringUtils.hasText(fileType)) {
                predicates.add(criteriaBuilder.equal(root.get("fileType"), FileEntity.FileType.valueOf(fileType.toUpperCase())));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<FileEntity> fileEntities = fileRepository.findAll(spec, pageable);
        return fileEntities.map(FileUploadResponse::new);
    }
    
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        
        // 파일 접근 권한 확인
        validateFileAccess(fileEntity);
        
        Path filePath = Paths.get(uploadDir, fileEntity.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("파일이 존재하지 않습니다.");
        }
        
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("파일을 읽을 수 없습니다.");
        }
    }
    
    @Transactional
    public void deleteFile(Long fileId) {
        FileEntity fileEntity = getFileById(fileId);
        User currentUser = getCurrentAuthenticatedUser();
        
        // 파일 삭제 권한 확인
        boolean canDelete = fileEntity.getUploadedBy().getId().equals(currentUser.getId()) ||
                           currentUser.getRoles().stream()
                                   .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("MASTER"));
        
        if (!canDelete) {
            throw new RuntimeException("파일을 삭제할 권한이 없습니다.");
        }
        
        // 물리적 파일 삭제
        try {
            Path filePath = Paths.get(uploadDir, fileEntity.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + fileEntity.getFilePath());
        }
        
        // 소프트 삭제
        fileEntity.setDeletedAt(LocalDateTime.now());
        fileRepository.save(fileEntity);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 총 파일 수
        long totalFiles = fileRepository.countByDeletedAtIsNull();
        stats.put("totalFiles", totalFiles);
        
        // 공개/비공개 파일 수
        long publicFiles = fileRepository.countByIsPublicAndDeletedAtIsNull(true);
        long privateFiles = fileRepository.countByIsPublicAndDeletedAtIsNull(false);
        stats.put("publicFiles", publicFiles);
        stats.put("privateFiles", privateFiles);
        
        // 파일 타입별 통계
        Map<String, Long> fileTypeStats = new HashMap<>();
        for (FileEntity.FileType type : FileEntity.FileType.values()) {
            long count = fileRepository.countByFileTypeAndDeletedAtIsNull(type);
            fileTypeStats.put(type.name(), count);
        }
        stats.put("fileTypeStats", fileTypeStats);
        
        // 총 파일 크기
        Long totalSize = fileRepository.sumFileSizeByDeletedAtIsNull();
        stats.put("totalSize", totalSize != null ? totalSize : 0L);
        
        // 최근 업로드된 파일 수 (7일 이내)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentFiles = fileRepository.countByCreatedAtAfterAndDeletedAtIsNull(weekAgo);
        stats.put("recentFiles", recentFiles);
        
        return stats;
    }
    
    private void validateGeneralFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일을 선택해주세요.");
        }
        
        if (file.getSize() > maxGeneralFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes.split(",")).contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_GENERAL_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다.");
        }
    }
    
    private FileEntity.FileType determineFileType(String contentType) {
        if (contentType == null) {
            return FileEntity.FileType.DOCUMENT;
        }
        
        if (contentType.startsWith("image/")) {
            return FileEntity.FileType.IMAGE;
        } else if (contentType.equals("application/pdf")) {
            return FileEntity.FileType.DOCUMENT;
        } else if (contentType.startsWith("text/")) {
            return FileEntity.FileType.DOCUMENT;
        } else if (contentType.contains("word") || contentType.contains("excel") || contentType.contains("powerpoint")) {
            return FileEntity.FileType.DOCUMENT;
        } else {
            return FileEntity.FileType.DOCUMENT;
        }
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증된 사용자를 찾을 수 없습니다.");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}