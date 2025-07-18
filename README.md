# 🏢 엔터프라이즈 웹 애플리케이션 템플릿

> 🚀 **Spring Boot 3.2 + Next.js 14**로 구축된 현대적인 엔터프라이즈급 웹 애플리케이션 템플릿

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

## 📋 목차
- [✨ 주요 기능](#-주요-기능)
- [🏗️ 아키텍처](#️-아키텍처)
- [🚀 빠른 시작](#-빠른-시작)
- [📁 프로젝트 구조](#-프로젝트-구조)
- [🔧 설정 가이드](#-설정-가이드)
- [📚 사용 가이드](#-사용-가이드)
- [🧪 테스트](#-테스트)
- [🔐 보안](#-보안)
- [📈 성능](#-성능)
- [🤝 기여하기](#-기여하기)
- [📄 라이센스](#-라이센스)

## ✨ 주요 기능

### 🔐 인증 및 권한 관리
- **JWT 기반 인증** - 토큰 기반 안전한 인증 시스템
- **역할 기반 접근 제어 (RBAC)** - 세밀한 권한 관리
- **마스터 계정 자동 생성** - 초기 관리자 계정 자동 설정
- **비밀번호 정책** - 강력한 비밀번호 정책 적용
- **세션 관리** - 토큰 만료 및 갱신 처리

### 👥 사용자 관리
- **회원가입 및 승인 시스템** - 관리자 승인 기반 회원가입
- **프로필 관리** - 개인정보 수정 및 프로필 이미지 업로드
- **사용자 목록 관리** - 페이지네이션 및 검색 기능
- **계정 활성화/비활성화** - 계정 상태 관리
- **역할 할당** - 사용자별 역할 및 권한 할당

### 📊 대시보드 및 통계
- **실시간 통계** - 사용자, 파일, 시스템 통계
- **시각화 차트** - 다양한 차트를 통한 데이터 시각화
- **시스템 상태 모니터링** - 메모리, 데이터베이스 상태 확인
- **최근 활동 추적** - 사용자 활동 로그 및 추적

### 📁 파일 관리
- **파일 업로드/다운로드** - 다양한 파일 형식 지원
- **권한 기반 접근 제어** - 파일별 접근 권한 관리
- **파일 타입 검증** - 업로드 파일 타입 및 크기 제한
- **프로필 이미지 관리** - 사용자 프로필 이미지 업로드

### 🔔 실시간 알림
- **WebSocket 기반 실시간 알림** - 즉시 알림 전송
- **알림 카테고리** - 다양한 알림 타입 분류
- **알림 상태 관리** - 읽음/안읽음 상태 관리
- **브로드캐스트 알림** - 전체 사용자 대상 공지

### 🎨 UI/UX
- **Material-UI 기반 모던 UI** - 깔끔하고 직관적인 인터페이스
- **다크/라이트 모드** - 사용자 선호도에 따른 테마 전환
- **반응형 디자인** - 모바일, 태블릿, 데스크톱 지원
- **접근성 지원** - WAI-ARIA 준수 및 키보드 탐색 지원

### 🛡️ 보안 및 성능
- **보안 헤더 설정** - CSRF, XSS, CSP 등 보안 헤더 적용
- **입력 데이터 검증** - 서버/클라이언트 양방향 검증
- **성능 모니터링** - 응답 시간 및 리소스 사용량 추적
- **캐싱 전략** - 효율적인 데이터 캐싱

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (Next.js 14)  │◄──►│ (Spring Boot 3) │◄──►│   (MySQL 8.0)   │
│                 │    │                 │    │                 │
│ • React 18      │    │ • Spring Security│    │ • JPA/Hibernate │
│ • TypeScript    │    │ • JWT Auth      │    │ • Connection Pool│
│ • Material-UI   │    │ • WebSocket     │    │ • Transactions  │
│ • Zustand       │    │ • REST API      │    │ • Indexes       │
│ • Axios         │    │ • AOP           │    │ • Backup        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 기술 스택

#### Backend (Spring Boot 3.2)
- **Java 17** - 최신 Java LTS 버전
- **Spring Security** - 인증 및 권한 관리
- **Spring Data JPA** - 데이터베이스 ORM
- **Spring WebSocket** - 실시간 통신
- **JWT (JSON Web Token)** - 토큰 기반 인증
- **MySQL Connector** - 데이터베이스 연결
- **Gradle** - 빌드 도구

#### Frontend (Next.js 14)
- **React 18** - 최신 React 버전
- **TypeScript** - 정적 타입 시스템
- **Material-UI (MUI)** - UI 컴포넌트 라이브러리
- **Zustand** - 상태 관리 라이브러리
- **React Hook Form** - 폼 관리
- **Zod** - 스키마 검증
- **Axios** - HTTP 클라이언트

#### Database
- **MySQL 8.0** - 메인 데이터베이스
- **H2 Database** - 개발/테스트용 인메모리 DB

## 🚀 빠른 시작

### 전제 조건
- **Java 17** 이상
- **Node.js 18** 이상
- **MySQL 8.0** (선택사항, H2 사용 가능)
- **Git**

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-username/enterprise-web-template.git
cd enterprise-web-template
```

### 2. 백엔드 설정 및 실행
```bash
# 백엔드 디렉토리로 이동
cd backend

# 애플리케이션 실행 (H2 데이터베이스 사용)
./gradlew bootRun

# 또는 MySQL 사용시
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

### 3. 프론트엔드 설정 및 실행
```bash
# 새 터미널에서 프론트엔드 디렉토리로 이동
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

### 4. 애플리케이션 접속
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/api/h2-console (H2 사용시)

### 5. 초기 로그인
- **이메일**: `master@enterprise.com`
- **비밀번호**: `MasterPassword123!`

## 📁 프로젝트 구조

```
enterprise-web-template/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/
│   │   └── com/enterprise/webtemplate/
│   │       ├── config/         # 설정 클래스
│   │       ├── controller/     # REST 컨트롤러
│   │       ├── dto/           # 데이터 전송 객체
│   │       ├── entity/        # JPA 엔티티
│   │       ├── repository/    # 데이터 접근 계층
│   │       ├── service/       # 비즈니스 로직
│   │       └── security/      # 보안 설정
│   ├── src/main/resources/
│   │   ├── application.yml    # 애플리케이션 설정
│   │   └── data.sql          # 초기 데이터
│   └── src/test/             # 테스트 코드
├── frontend/                  # Next.js 프론트엔드
│   ├── src/
│   │   ├── app/              # Next.js 13+ App Router
│   │   ├── components/       # React 컴포넌트
│   │   ├── hooks/           # 커스텀 훅
│   │   ├── lib/             # 유틸리티 함수
│   │   ├── stores/          # 상태 관리
│   │   └── types/           # TypeScript 타입
│   ├── public/              # 정적 파일
│   └── package.json         # 의존성 관리
├── docs/                    # 문서
│   ├── 기능_사용_가이드.md
│   ├── API_사용법_가이드.md
│   ├── 컴포넌트_사용법_가이드.md
│   └── 데이터베이스_설정_가이드.md
└── README.md               # 프로젝트 설명서
```

## 🔧 설정 가이드

### 환경 변수 설정

#### Backend (.env 또는 환경 변수)
```bash
# 데이터베이스 설정
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_URL=jdbc:mysql://localhost:3306/enterprise_web_template

# JWT 설정
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# 파일 업로드 설정
FILE_UPLOAD_DIR=./uploads
FILE_MAX_SIZE=10485760
```

#### Frontend (.env.local)
```bash
# API 엔드포인트
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws

# 파일 업로드 설정
NEXT_PUBLIC_MAX_FILE_SIZE=10485760
```

### 데이터베이스 설정

#### H2 Database (개발용)
```yaml
spring:
  profiles:
    active: local
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
```

#### MySQL Database (운영용)
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/enterprise_web_template
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

자세한 설정 방법은 [데이터베이스 설정 가이드](./데이터베이스_설정_가이드.md)를 참조하세요.

## 📚 사용 가이드

### 📖 상세 문서
- [🔧 기능 사용 가이드](./기능_사용_가이드.md) - 각 기능의 사용법 및 확장 방법
- [🌐 API 사용법 가이드](./API_사용법_가이드.md) - REST API 엔드포인트 상세 설명
- [⚛️ 컴포넌트 사용법 가이드](./컴포넌트_사용법_가이드.md) - React 컴포넌트 사용법
- [🗃️ 데이터베이스 설정 가이드](./데이터베이스_설정_가이드.md) - 데이터베이스 설정 및 마이그레이션

### 주요 기능 사용법

#### 1. 새 사용자 생성
```bash
# API 호출 예시
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "name": "사용자 이름",
    "password": "Password123!",
    "phoneNumber": "010-1234-5678"
  }'
```

#### 2. 알림 전송
```java
@Autowired
private NotificationService notificationService;

// 특정 사용자에게 알림 전송
notificationService.createNotification(
    "제목", "메시지", "INFO", userId
);

// 전체 사용자에게 브로드캐스트
notificationService.createBroadcastNotification(
    "공지사항", "전체 공지 메시지", "ANNOUNCEMENT"
);
```

#### 3. 파일 업로드
```typescript
const handleFileUpload = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  
  return response.data;
};
```

## 🧪 테스트

### 테스트 실행

#### Backend 테스트
```bash
cd backend

# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.enterprise.webtemplate.service.UserServiceTest"

# 테스트 커버리지 리포트 생성
./gradlew test jacocoTestReport
```

#### Frontend 테스트
```bash
cd frontend

# 모든 테스트 실행
npm test

# 테스트 커버리지
npm run test:coverage

# 특정 테스트 파일 실행
npm test -- --testNamePattern="AccessibleButton"
```

### 테스트 커버리지
- **Unit Tests**: 80% 이상
- **Integration Tests**: 60% 이상
- **Critical Path Coverage**: 100%

자세한 테스트 정보는 [테스트 구현 요약](./테스트_구현_요약.md)을 참조하세요.

## 🔐 보안

### 보안 기능
- **JWT 토큰 인증** - 안전한 토큰 기반 인증
- **RBAC 권한 관리** - 역할 기반 접근 제어
- **CSRF 보호** - Cross-Site Request Forgery 방지
- **XSS 보호** - Cross-Site Scripting 방지
- **SQL Injection 방지** - 파라미터화된 쿼리 사용
- **비밀번호 암호화** - BCrypt 해시 알고리즘
- **보안 헤더 설정** - HSTS, CSP 등 보안 헤더 적용

### 보안 설정
```yaml
# 보안 헤더 설정
security:
  headers:
    content-security-policy: "default-src 'self'"
    x-frame-options: "DENY"
    x-content-type-options: "nosniff"
```

## 📈 성능

### 성능 최적화
- **데이터베이스 인덱싱** - 효율적인 쿼리 성능
- **연결 풀 관리** - HikariCP 커넥션 풀
- **캐싱 전략** - 메모리 캐싱 및 HTTP 캐싱
- **압축 설정** - Gzip 압축 활성화
- **정적 파일 최적화** - CDN 및 캐싱 헤더 설정

### 성능 모니터링
```java
@Component
public class PerformanceMonitor {
    
    @Around("@annotation(Monitored)")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 성능 로그 기록
        logger.info("Method {} executed in {} ms", 
                   joinPoint.getSignature().getName(), executionTime);
        
        return result;
    }
}
```

## 🤝 기여하기

### 개발 환경 설정
1. 프로젝트 포크 및 클론
2. 개발 브랜치 생성
3. 변경사항 구현 및 테스트
4. 코미트 및 풀 리퀘스트 생성

### 커밋 메시지 규칙
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드 프로세스 수정
```

### 코드 스타일
- **Java**: Google Java Style Guide
- **TypeScript**: ESLint + Prettier
- **변수명**: camelCase
- **상수명**: UPPER_SNAKE_CASE
- **클래스명**: PascalCase

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🙏 감사의 말

이 프로젝트는 현대적인 웹 개발 기술과 모범 사례를 결합하여 만들어졌습니다. 

### 주요 기여자
- **Backend Architecture**: Spring Boot 3.2 + Spring Security
- **Frontend Architecture**: Next.js 14 + TypeScript
- **Database Design**: MySQL 8.0 + JPA/Hibernate
- **Security Implementation**: JWT + RBAC
- **Testing Strategy**: JUnit 5 + React Testing Library

### 사용된 오픈소스 라이브러리
- Spring Boot, Spring Security, Spring Data JPA
- Next.js, React, TypeScript, Material-UI
- MySQL, H2 Database, HikariCP
- JWT, BCrypt, Zod, Axios

---

**🚀 지금 시작하세요!** 이 템플릿을 사용하여 엔터프라이즈급 웹 애플리케이션을 빠르게 개발할 수 있습니다.

문의사항이나 개선 제안이 있으시면 [Issues](https://github.com/your-username/enterprise-web-template/issues)에 남겨주세요.