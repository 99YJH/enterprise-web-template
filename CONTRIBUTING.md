# 기여 가이드

이 프로젝트에 기여해주셔서 감사합니다! 다음 가이드라인을 따라 기여해주시기 바랍니다.

## 기여 방법

### 1. 이슈 생성
- 버그 리포트나 기능 요청 시 이슈를 먼저 생성해주세요
- 이슈 템플릿을 사용하여 자세한 정보를 제공해주세요

### 2. 개발 환경 설정
```bash
# 프로젝트 클론
git clone https://github.com/your-username/enterprise-web-template.git
cd enterprise-web-template

# 개발 브랜치 생성
git checkout -b feature/your-feature-name

# 의존성 설치
cd backend && ./gradlew build
cd ../frontend && npm install
```

### 3. 코드 작성
- 기존 코드 스타일을 따라주세요
- 테스트 코드를 작성해주세요
- 커밋 메시지는 다음 형식을 따라주세요:
  ```
  feat: 새로운 기능 추가
  fix: 버그 수정
  docs: 문서 수정
  style: 코드 스타일 변경
  refactor: 코드 리팩토링
  test: 테스트 추가/수정
  chore: 빌드 프로세스 수정
  ```

### 4. 테스트 실행
```bash
# 백엔드 테스트
cd backend
./gradlew test

# 프론트엔드 테스트
cd frontend
npm test
```

### 5. 풀 리퀘스트 생성
- 변경사항을 명확하게 설명해주세요
- 관련 이슈를 연결해주세요
- 테스트가 통과하는지 확인해주세요

## 코드 스타일

### Java (Backend)
- Google Java Style Guide 준수
- 변수명: camelCase
- 상수명: UPPER_SNAKE_CASE
- 클래스명: PascalCase

### TypeScript (Frontend)
- ESLint + Prettier 설정 준수
- 변수명: camelCase
- 컴포넌트명: PascalCase
- 인터페이스명: PascalCase (I 접두사 사용하지 않음)

## 리뷰 프로세스

1. 자동 테스트가 통과해야 합니다
2. 코드 리뷰어가 승인해야 합니다
3. 충돌이 없어야 합니다
4. 커밋 메시지가 규칙에 맞아야 합니다

## 질문이나 도움이 필요한 경우

- GitHub Issues에 질문을 남겨주세요
- 토론이 필요한 경우 GitHub Discussions를 이용해주세요

다시 한 번, 기여해주셔서 감사합니다! 🙏