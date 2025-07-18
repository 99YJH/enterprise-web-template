# 데이터베이스 설정 가이드

## 개요
이 디렉토리는 엔터프라이즈 웹 템플릿의 MySQL 데이터베이스 초기화를 위한 SQL 스크립트를 포함합니다.

## 파일 구조
- `schema.sql` - 데이터베이스 스키마 및 테이블 생성
- `data.sql` - 초기 데이터 삽입 (역할, 권한, 마스터 사용자)
- `setup.sql` - 전체 설정을 위한 통합 스크립트

## 사용 방법

### 1. MySQL 접속
```bash
mysql -u root -p
```

### 2. 데이터베이스 설정 실행
```sql
-- 방법 1: 통합 스크립트 사용
SOURCE /path/to/template/database/setup.sql;

-- 방법 2: 개별 파일 실행
SOURCE /path/to/template/database/schema.sql;
SOURCE /path/to/template/database/data.sql;
```

### 3. 데이터베이스 사용
```sql
USE enterprise_web_template;

-- 생성된 테이블 확인
SHOW TABLES;

-- 관리자 사용자 확인
SELECT * FROM users WHERE email = 'admin@admin.com';
```

## 기본 설정 정보

### 데이터베이스
- **이름**: `enterprise_web_template`
- **문자셋**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`

### 관리자 사용자
- **이메일**: `admin@admin.com`
- **비밀번호**: `Admin123!`
- **역할**: MASTER (모든 권한 보유)

### 기본 역할
- **MASTER**: 최고 관리자 (모든 권한)
- **ADMIN**: 관리자 (관리자 권한)
- **USER**: 일반 사용자 (기본 권한)

### 권한 카테고리
- **USER_MANAGEMENT**: 사용자 관리 권한
- **ROLE_MANAGEMENT**: 역할 관리 권한
- **FILE_MANAGEMENT**: 파일 관리 권한
- **NOTIFICATION_MANAGEMENT**: 알림 관리 권한
- **DASHBOARD**: 대시보드 권한
- **SYSTEM**: 시스템 관리 권한

## 테이블 구조

### users
사용자 정보를 저장하는 테이블
- 이메일, 비밀번호, 이름, 전화번호
- 프로필 이미지, 활성 상태, 승인 상태

### roles
역할 정보를 저장하는 테이블
- 역할명, 설명, 시스템 역할 여부

### permissions
권한 정보를 저장하는 테이블
- 권한명, 설명, 카테고리

### user_roles
사용자-역할 연결 테이블

### role_permissions
역할-권한 연결 테이블

### files
파일 정보를 저장하는 테이블
- 원본명, 저장명, 경로, 크기, 타입
- 업로드자, 업로드일, 공개 여부

### notifications
알림 정보를 저장하는 테이블
- 제목, 메시지, 타입, 대상 사용자
- 읽음 여부, 브로드캐스트 여부

## 문제 해결

### 1. 데이터베이스 생성 실패
```sql
-- 권한 확인
SHOW GRANTS FOR 'root'@'localhost';

-- 데이터베이스 수동 생성
CREATE DATABASE enterprise_web_template CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 외래키 제약 조건 오류
```sql
-- 외래키 체크 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 스크립트 실행 후 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;
```

### 3. 중복 데이터 오류
스크립트는 `ON DUPLICATE KEY UPDATE` 구문을 사용하여 중복 실행을 방지합니다.

## 추가 명령어

### 데이터베이스 초기화
```sql
-- 모든 테이블 삭제 후 재생성
DROP DATABASE IF EXISTS enterprise_web_template;
SOURCE setup.sql;
```

### 데이터 확인
```sql
-- 사용자 수 확인
SELECT COUNT(*) FROM users;

-- 역할별 사용자 수 확인
SELECT r.name, COUNT(ur.user_id) as user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
GROUP BY r.id, r.name;

-- 권한별 역할 수 확인
SELECT p.name, COUNT(rp.role_id) as role_count
FROM permissions p
LEFT JOIN role_permissions rp ON p.id = rp.permission_id
GROUP BY p.id, p.name;
```