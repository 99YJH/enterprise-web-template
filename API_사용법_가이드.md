# API 사용법 가이드

## 개요
이 문서는 엔터프라이즈 웹 애플리케이션 템플릿의 API 엔드포인트 사용법을 자세히 설명합니다.

## 기본 정보

### Base URL
```
http://localhost:8080/api
```

### 인증 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

---

## 인증 API

### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123",
    "rememberMe": false
}
```

**응답:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
        "id": 1,
        "email": "user@example.com",
        "name": "사용자 이름",
        "roles": ["USER"]
    }
}
```

### 회원가입
```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "newuser@example.com",
    "name": "새로운 사용자",
    "password": "Password123!",
    "phoneNumber": "010-1234-5678",
    "department": "개발팀",
    "position": "개발자"
}
```

### 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
    "refreshToken": "refresh_token_here"
}
```

### 로그아웃
```http
POST /api/auth/logout
Authorization: Bearer {JWT_TOKEN}
```

---

## 사용자 관리 API

### 사용자 목록 조회
```http
GET /api/users?page=0&size=10&search=검색어&status=ACTIVE
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
    "content": [
        {
            "id": 1,
            "email": "user@example.com",
            "name": "사용자 이름",
            "isActive": true,
            "approvalStatus": "APPROVED",
            "roles": ["USER"],
            "createdAt": "2024-01-01T00:00:00",
            "lastLoginAt": "2024-01-15T10:30:00"
        }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0
}
```

### 사용자 상세 조회
```http
GET /api/users/{userId}
Authorization: Bearer {JWT_TOKEN}
```

### 사용자 활성화
```http
PUT /api/users/{userId}/activate
Authorization: Bearer {JWT_TOKEN}
```

### 사용자 비활성화
```http
PUT /api/users/{userId}/deactivate
Authorization: Bearer {JWT_TOKEN}
```

### 프로필 업데이트
```http
PUT /api/users/{userId}/profile
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "name": "수정된 이름",
    "phoneNumber": "010-9876-5432",
    "department": "마케팅팀",
    "position": "매니저"
}
```

### 비밀번호 변경
```http
PUT /api/users/{userId}/password
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "currentPassword": "oldPassword",
    "newPassword": "newPassword123!"
}
```

---

## 파일 관리 API

### 파일 업로드
```http
POST /api/files/upload
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data

file: (선택한 파일)
isPublic: false
description: 파일 설명
```

**응답:**
```json
{
    "id": 1,
    "originalFilename": "document.pdf",
    "storedFilename": "20240101_123456_document.pdf",
    "fileSize": 2048576,
    "contentType": "application/pdf",
    "fileType": "DOCUMENT",
    "isPublic": false,
    "description": "파일 설명",
    "uploadedBy": {
        "id": 1,
        "name": "업로더 이름"
    },
    "createdAt": "2024-01-01T12:34:56"
}
```

### 내 파일 목록 조회
```http
GET /api/files/my-files?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

### 공개 파일 목록 조회
```http
GET /api/files/public?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

### 파일 다운로드
```http
GET /api/files/download/{fileId}
Authorization: Bearer {JWT_TOKEN}
```

### 파일 삭제
```http
DELETE /api/files/{fileId}
Authorization: Bearer {JWT_TOKEN}
```

### 프로필 이미지 업로드
```http
POST /api/files/profile-image
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data

file: (이미지 파일)
```

---

## 알림 API

### 내 알림 목록 조회
```http
GET /api/notifications?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
    "content": [
        {
            "id": 1,
            "title": "새로운 알림",
            "message": "알림 메시지 내용",
            "type": "INFO",
            "isRead": false,
            "createdAt": "2024-01-01T12:00:00"
        }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "size": 10,
    "number": 0
}
```

### 읽지 않은 알림 수
```http
GET /api/notifications/unread-count
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
    "count": 5
}
```

### 알림 읽음 처리
```http
PUT /api/notifications/{notificationId}/read
Authorization: Bearer {JWT_TOKEN}
```

### 모든 알림 읽음 처리
```http
PUT /api/notifications/mark-all-read
Authorization: Bearer {JWT_TOKEN}
```

### 알림 삭제
```http
DELETE /api/notifications/{notificationId}
Authorization: Bearer {JWT_TOKEN}
```

---

## 대시보드 API

### 대시보드 통계
```http
GET /api/dashboard/stats
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
    "totalUsers": 100,
    "activeUsers": 80,
    "inactiveUsers": 20,
    "pendingUsers": 10,
    "approvedUsers": 85,
    "rejectedUsers": 5,
    "totalRoles": 5,
    "totalPermissions": 20,
    "totalFiles": 500,
    "todayRegistrations": 5,
    "weeklyRegistrations": 25
}
```

### 사용자 통계
```http
GET /api/dashboard/user-stats
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
    "usersByRole": {
        "ADMIN": 5,
        "USER": 95
    },
    "monthlyRegistrations": [
        {
            "month": "2024-01",
            "monthName": "2024년 01월",
            "count": 10
        }
    ],
    "usersByApprovalStatus": {
        "PENDING": 10,
        "APPROVED": 85,
        "REJECTED": 5
    }
}
```

### 최근 활동
```http
GET /api/dashboard/recent-activities
Authorization: Bearer {JWT_TOKEN}
```

### 시스템 상태
```http
GET /api/dashboard/system-health
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
    "systemStatus": "HEALTHY",
    "timestamp": "2024-01-01T12:00:00",
    "memory": {
        "totalMemory": 1073741824,
        "freeMemory": 536870912,
        "usedMemory": 536870912,
        "maxMemory": 2147483648,
        "usagePercentage": 25.0
    },
    "database": {
        "status": "HEALTHY",
        "userCount": 100,
        "connectionTest": "SUCCESS"
    },
    "uptime": 86400000
}
```

---

## 역할 및 권한 관리 API

### 모든 역할 조회
```http
GET /api/roles
Authorization: Bearer {JWT_TOKEN}
```

### 역할 상세 조회
```http
GET /api/roles/{roleId}
Authorization: Bearer {JWT_TOKEN}
```

### 새 역할 생성
```http
POST /api/roles
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "name": "MANAGER",
    "description": "매니저 역할",
    "permissionNames": ["USER_READ", "USER_UPDATE"]
}
```

### 역할 수정
```http
PUT /api/roles/{roleId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "name": "MANAGER",
    "description": "수정된 매니저 역할",
    "permissionNames": ["USER_READ", "USER_UPDATE", "FILE_READ"]
}
```

### 역할 삭제
```http
DELETE /api/roles/{roleId}
Authorization: Bearer {JWT_TOKEN}
```

### 모든 권한 조회
```http
GET /api/roles/permissions
Authorization: Bearer {JWT_TOKEN}
```

### 카테고리별 권한 조회
```http
GET /api/roles/permissions/by-category
Authorization: Bearer {JWT_TOKEN}
```

### 사용자 역할 업데이트
```http
PUT /api/roles/users/{userId}/roles
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "roleNames": ["USER", "MANAGER"]
}
```

---

## 관리자 API

### 사용자 승인
```http
PUT /api/admin/users/{userId}/approve
Authorization: Bearer {JWT_TOKEN}
```

### 사용자 거부
```http
PUT /api/admin/users/{userId}/reject
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "reason": "거부 사유"
}
```

### 시스템 설정 조회
```http
GET /api/admin/settings
Authorization: Bearer {JWT_TOKEN}
```

### 시스템 설정 업데이트
```http
PUT /api/admin/settings
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
    "key": "value",
    "anotherKey": "anotherValue"
}
```

---

## 오류 응답 형식

### 일반 오류
```json
{
    "error": "ERROR_CODE",
    "message": "사용자 친화적인 오류 메시지",
    "timestamp": "2024-01-01T12:00:00",
    "path": "/api/endpoint"
}
```

### 검증 오류
```json
{
    "error": "VALIDATION_ERROR",
    "message": "입력 데이터 검증 실패",
    "details": {
        "email": "유효하지 않은 이메일 형식입니다",
        "password": "비밀번호는 8자 이상이어야 합니다"
    }
}
```

### 권한 오류
```json
{
    "error": "ACCESS_DENIED",
    "message": "이 작업을 수행할 권한이 없습니다",
    "requiredPermission": "USER_CREATE"
}
```

---

## 페이지네이션 파라미터

모든 목록 API에서 공통으로 사용되는 파라미터:

- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 기준 (예: `name,asc` 또는 `createdAt,desc`)
- `search`: 검색 키워드

---

## 파일 업로드 제한

### 지원되는 파일 형식
- 이미지: `image/jpeg`, `image/png`, `image/gif`
- 문서: `application/pdf`, `application/msword`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- 기타: 설정에 따라 추가 가능

### 파일 크기 제한
- 기본 최대 크기: 10MB
- 프로필 이미지: 5MB

---

## WebSocket 연결

### 연결 엔드포인트
```
ws://localhost:8080/ws
```

### 구독 경로
- 개인 알림: `/user/queue/notifications`
- 브로드캐스트 알림: `/topic/announcements`
- 시스템 알림: `/topic/system`

### 메시지 전송
```javascript
stompClient.send("/app/message", {}, JSON.stringify({
    content: "메시지 내용",
    type: "CHAT"
}));
```

---

## 사용 예시

### JavaScript/TypeScript
```typescript
// API 클라이언트 설정
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000,
});

// 요청 인터셉터 - 토큰 자동 추가
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 응답 인터셉터 - 오류 처리
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // 토큰 만료 또는 인증 실패
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// 사용 예시
const fetchUsers = async () => {
    try {
        const response = await api.get('/users?page=0&size=10');
        return response.data;
    } catch (error) {
        console.error('사용자 목록 조회 실패:', error);
        throw error;
    }
};
```

### cURL 예시
```bash
# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "rememberMe": false
  }'

# 사용자 목록 조회
curl -X GET "http://localhost:8080/api/users?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 파일 업로드
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/file.pdf" \
  -F "isPublic=false" \
  -F "description=업로드된 문서"
```

---

## 추가 참고사항

### 보안 고려사항
1. 모든 API 호출에는 유효한 JWT 토큰이 필요합니다
2. 토큰은 안전한 곳에 저장해야 합니다 (예: httpOnly 쿠키)
3. HTTPS를 사용하여 통신을 암호화해야 합니다

### 성능 최적화
1. 페이지네이션을 활용하여 대량 데이터 처리
2. 필요한 데이터만 요청하도록 필터링 사용
3. 캐싱을 적절히 활용

### 오류 처리
1. HTTP 상태 코드를 확인하여 적절한 처리
2. 사용자 친화적인 오류 메시지 표시
3. 재시도 로직 구현 (네트워크 오류 등)

이 가이드를 통해 API를 효과적으로 활용하여 애플리케이션을 개발할 수 있습니다.