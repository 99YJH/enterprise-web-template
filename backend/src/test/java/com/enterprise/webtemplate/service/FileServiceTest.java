package com.enterprise.webtemplate.service;

import com.enterprise.webtemplate.entity.FileEntity;
import com.enterprise.webtemplate.entity.User;
import com.enterprise.webtemplate.repository.FileRepository;
import com.enterprise.webtemplate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FileService fileService;

    private User testUser;
    private FileEntity testFile;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사용자");

        testFile = new FileEntity();
        testFile.setId(1L);
        testFile.setOriginalFilename("test.jpg");
        testFile.setStoredFilename("stored_test.jpg");
        testFile.setFileSize(1024L);
        testFile.setContentType("image/jpeg");
        testFile.setFilePath("uploads/test.jpg");
        testFile.setUploadedBy(testUser);
        testFile.setFileType(FileEntity.FileType.IMAGE);
        testFile.setIsPublic(false);
        testFile.setScanStatus(FileEntity.ScanStatus.CLEAN);
        testFile.setCreatedAt(LocalDateTime.now());

        mockFile = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        // SecurityContext 설정
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);

        // 필드 값 설정
        ReflectionTestUtils.setField(fileService, "uploadDir", "uploads");
        ReflectionTestUtils.setField(fileService, "maxFileSize", 10485760L);
        ReflectionTestUtils.setField(fileService, "allowedTypes", "image/jpeg,image/png,image/gif");
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(testFile);

        // When
        FileEntity result = fileService.uploadFile(mockFile, false, "테스트 파일");

        // Then
        assertNotNull(result);
        assertEquals("test.jpg", result.getOriginalFilename());
        assertEquals("stored_test.jpg", result.getStoredFilename());
        assertEquals(1024L, result.getFileSize());
        assertEquals("image/jpeg", result.getContentType());
        assertEquals(testUser, result.getUploadedBy());
        assertEquals(FileEntity.FileType.IMAGE, result.getFileType());
        assertFalse(result.getIsPublic());

        verify(userRepository).findByEmail("test@example.com");
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void testUploadFile_FileTooLarge() {
        // Given
        MockMultipartFile largeFile = new MockMultipartFile(
            "file", 
            "large.jpg", 
            "image/jpeg", 
            new byte[20 * 1024 * 1024] // 20MB
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(largeFile, false, "큰 파일");
        });

        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testUploadFile_InvalidFileType() {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(invalidFile, false, "텍스트 파일");
        });

        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testUploadFile_EmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.jpg", 
            "image/jpeg", 
            new byte[0]
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(emptyFile, false, "빈 파일");
        });

        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testGetFileById_Success() {
        // Given
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));

        // When
        FileEntity result = fileService.getFileById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test.jpg", result.getOriginalFilename());

        verify(fileRepository).findById(1L);
    }

    @Test
    void testGetFileById_NotFound() {
        // Given
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            fileService.getFileById(1L);
        });

        verify(fileRepository).findById(1L);
    }

    @Test
    void testDeleteFile_Success() {
        // Given
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(testFile);

        // When
        fileService.deleteFile(1L);

        // Then
        assertNotNull(testFile.getDeletedAt());
        verify(fileRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(fileRepository).save(testFile);
    }

    @Test
    void testDeleteFile_NotFound() {
        // Given
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            fileService.deleteFile(1L);
        });

        verify(fileRepository).findById(1L);
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testDeleteFile_NoPermission() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@example.com");
        
        testFile.setUploadedBy(anotherUser);
        
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            fileService.deleteFile(1L);
        });

        verify(fileRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testUploadProfileImage_Success() throws IOException {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(testFile);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        FileEntity result = fileService.uploadProfileImage(mockFile);

        // Then
        assertNotNull(result);
        assertEquals(FileEntity.FileType.PROFILE_IMAGE, result.getFileType());
        assertFalse(result.getIsPublic());

        verify(userRepository).findByEmail("test@example.com");
        verify(fileRepository).save(any(FileEntity.class));
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteProfileImage_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(fileRepository.findByUploadedByAndFileTypeAndDeletedAtIsNull(testUser, FileEntity.FileType.PROFILE_IMAGE))
            .thenReturn(java.util.List.of(testFile));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(testFile);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        fileService.deleteProfileImage();

        // Then
        assertNotNull(testFile.getDeletedAt());
        assertNull(testUser.getProfileImageUrl());

        verify(userRepository).findByEmail("test@example.com");
        verify(fileRepository).findByUploadedByAndFileTypeAndDeletedAtIsNull(testUser, FileEntity.FileType.PROFILE_IMAGE);
        verify(fileRepository).save(testFile);
        verify(userRepository).save(testUser);
    }

    @Test
    void testValidateFileAccess_PublicFile() {
        // Given
        testFile.setIsPublic(true);

        // When & Then
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(fileService, "validateFileAccess", testFile);
        });
    }

    @Test
    void testValidateFileAccess_OwnerFile() {
        // Given
        testFile.setIsPublic(false);
        testFile.setUploadedBy(testUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(fileService, "validateFileAccess", testFile);
        });
    }

    @Test
    void testValidateFileAccess_NoPermission() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@example.com");
        
        testFile.setIsPublic(false);
        testFile.setUploadedBy(anotherUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            ReflectionTestUtils.invokeMethod(fileService, "validateFileAccess", testFile);
        });
    }

    @Test
    void testGetFileExtension() {
        // When
        String result = ReflectionTestUtils.invokeMethod(fileService, "getFileExtension", "test.jpg");

        // Then
        assertEquals("jpg", result);
    }

    @Test
    void testGetFileExtension_NoExtension() {
        // When
        String result = ReflectionTestUtils.invokeMethod(fileService, "getFileExtension", "test");

        // Then
        assertEquals("", result);
    }

    @Test
    void testGetFileExtension_NullFilename() {
        // When
        String result = ReflectionTestUtils.invokeMethod(fileService, "getFileExtension", (String) null);

        // Then
        assertEquals("", result);
    }
}