# MODI-backend
MODI는 사용자의 일상 기록을 사진/감정/키워드 중심으로 저장하고, AI 기능을 통해 요약/문체 변환/자동 문장 생성을 지원하는 백엔드 서비스입니다.

## Skills
- Spring Boot
- MySQL
- Docker
- AWS (EC2, RDS, S3)

## Project Structure
```text
src
├── main
│   ├── java/kuit/modi
│   │   ├── config        # 보안, AWS, OpenAPI 설정
│   │   ├── controller    # API 엔드포인트
│   │   ├── domain        # JPA 엔티티
│   │   ├── dto           # 요청/응답 DTO
│   │   ├── exception     # 커스텀 예외 및 상태 코드
│   │   ├── filter        # JWT 인증 필터
│   │   ├── openai        # OpenAI 연동 클라이언트
│   │   ├── repository    # JPA Repository
│   │   └── service       # 비즈니스 로직
│   └── resources         # application.yml 등 설정 파일
└── test
    └── java/kuit/modi    # JUnit/Mockito 테스트
```

## Getting Started
### 1) Prerequisites
- JDK 17
- Gradle Wrapper 사용 (`./gradlew`)
- MySQL

### 2) Environment Variables
`src/main/resources/application.yml`에서 참조하는 값들이 필요합니다.
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `AWS_ACCESS_KEY`
- `AWS_SECRET_KEY`
- `AWS_REGION`
- `AWS_S3_BUCKET`
- `OPENAI_API_KEY`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`

### 3) Run
```bash
./gradlew bootRun
```

### 4) Test
```bash
./gradlew test
```

## API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Docs(JSON): `http://localhost:8080/api-docs`

## CI/CD
- CI: GitHub Actions에서 `main` 대상 `push`, `pull_request` 시 테스트 실행
- CD: CI 성공 시 배포 워크플로우 실행
- 배포 시크릿이 없는 경우, CD 워크플로우는 skip 메시지를 남기고 종료

## References

### ERD
https://www.erdcloud.com/d/Hhmnhir4ys9LYGRoE

### API 명세서
https://hickory-numeric-060.notion.site/API-2262dfee0b72815e99dcdb9224594a2f?source=copy_link

### 아키텍처 다이어그램
<img width="699" height="469" alt="Frame 1 (3)" src="https://github.com/user-attachments/assets/1431a00f-2f45-446c-850c-64241aee6fbf" />
