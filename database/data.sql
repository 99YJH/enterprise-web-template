-- 초기 데이터 삽입

-- 기본 역할 생성
INSERT INTO roles (name, description, is_system_role) VALUES 
('MASTER', '최고 관리자', true),
('ADMIN', '관리자', true),
('USER', '일반 사용자', true)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 기본 권한 생성
INSERT INTO permissions (name, description, category) VALUES 
-- 사용자 관리 권한
('USER_READ', '사용자 조회', 'USER_MANAGEMENT'),
('USER_CREATE', '사용자 생성', 'USER_MANAGEMENT'),
('USER_UPDATE', '사용자 수정', 'USER_MANAGEMENT'),
('USER_DELETE', '사용자 삭제', 'USER_MANAGEMENT'),

-- 역할 관리 권한
('ROLE_READ', '역할 조회', 'ROLE_MANAGEMENT'),
('ROLE_CREATE', '역할 생성', 'ROLE_MANAGEMENT'),
('ROLE_UPDATE', '역할 수정', 'ROLE_MANAGEMENT'),
('ROLE_DELETE', '역할 삭제', 'ROLE_MANAGEMENT'),

-- 파일 관리 권한
('FILE_READ', '파일 조회', 'FILE_MANAGEMENT'),
('FILE_CREATE', '파일 업로드', 'FILE_MANAGEMENT'),
('FILE_UPDATE', '파일 수정', 'FILE_MANAGEMENT'),
('FILE_DELETE', '파일 삭제', 'FILE_MANAGEMENT'),

-- 알림 관리 권한
('NOTIFICATION_READ', '알림 조회', 'NOTIFICATION_MANAGEMENT'),
('NOTIFICATION_CREATE', '알림 생성', 'NOTIFICATION_MANAGEMENT'),
('NOTIFICATION_UPDATE', '알림 수정', 'NOTIFICATION_MANAGEMENT'),
('NOTIFICATION_DELETE', '알림 삭제', 'NOTIFICATION_MANAGEMENT'),

-- 대시보드 권한
('DASHBOARD_VIEW', '대시보드 조회', 'DASHBOARD'),
('DASHBOARD_ADMIN', '대시보드 관리', 'DASHBOARD'),

-- 시스템 관리 권한
('SYSTEM_ADMIN', '시스템 관리', 'SYSTEM'),
('SYSTEM_CONFIG', '시스템 설정', 'SYSTEM')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 관리자 사용자 생성 (비밀번호: Admin123!)
INSERT INTO users (email, password, name, phone_number, is_active, approval_status) VALUES 
('admin@admin.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.k5uSihS6', 'Admin User', '010-0000-0000', true, 'APPROVED')
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    name = VALUES(name),
    phone_number = VALUES(phone_number),
    is_active = VALUES(is_active),
    approval_status = VALUES(approval_status);

-- 역할별 권한 할당
-- MASTER 역할에 모든 권한 부여
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MASTER'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ADMIN 역할에 관리자 권한 부여
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN (
    'USER_READ', 'USER_CREATE', 'USER_UPDATE',
    'ROLE_READ', 'ROLE_CREATE', 'ROLE_UPDATE',
    'FILE_READ', 'FILE_CREATE', 'FILE_UPDATE', 'FILE_DELETE',
    'NOTIFICATION_READ', 'NOTIFICATION_CREATE', 'NOTIFICATION_UPDATE', 'NOTIFICATION_DELETE',
    'DASHBOARD_VIEW', 'DASHBOARD_ADMIN'
  )
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- USER 역할에 기본 사용자 권한 부여
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.name IN (
    'USER_READ',
    'FILE_READ', 'FILE_CREATE',
    'NOTIFICATION_READ',
    'DASHBOARD_VIEW'
  )
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 관리자 사용자에게 MASTER 역할 부여
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'admin@admin.com'
  AND r.name = 'MASTER'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- 샘플 알림 데이터 생성
INSERT INTO notifications (title, message, type, is_broadcast, created_at) VALUES 
('시스템 알림', '엔터프라이즈 웹 템플릿에 오신 것을 환영합니다!', 'SYSTEM', true, NOW()),
('환영 메시지', '새로운 기능들을 탐험해보세요.', 'INFO', true, NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);