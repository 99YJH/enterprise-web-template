-- 전체 데이터베이스 설정 스크립트
-- 이 파일은 schema.sql과 data.sql을 순차적으로 실행합니다.

-- 스키마 생성
SOURCE schema.sql;

-- 초기 데이터 삽입
SOURCE data.sql;

-- 설정 완료 메시지
SELECT 'Database setup completed successfully!' as message;