# api-login-svc

사용자 회원가입 및 로그인 검증을 담당하는 인증 기초 서비스입니다. 비밀번호는 해시로 저장되며, OAuth2/JWT는 추후 고도화 예정입니다(참고: [ADR-006](../msa-project-hub/docs/adr/ADR-006-authentication-strategy.md)).

---

## 기술 스택 및 실행 포트
- Spring Boot 3, Java 21, Gradle
- JPA + PostgreSQL, PasswordEncoder(BCrypt)
- 기본 컨테이너 포트: `8080` ([ADR-005](../msa-project-hub/docs/adr/ADR-005-service-port-convention.md))

---

## 환경 변수
- `DATABASE_URL` (예: `jdbc:postgresql://localhost:5432/login`)
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

application.properties 발췌:
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
```

---

## 빌드 및 실행
```bash
# 로컬 실행
./gradlew bootRun

# 빌드(JAR)
./gradlew bootJar

# Docker 이미지 빌드
docker build -t api-login-svc:local .

# Docker 컨테이너 실행 (예시)
docker run --rm -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/login" \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  api-login-svc:local
```

---

## CORS
`WebConfig`에서 `http://localhost:3000`과 배포된 프론트엔드 주소를 기본 허용합니다.

---

## API 엔드포인트
베이스 경로: `/api/v1/users`

- 회원가입: `POST /api/v1/users/signup`
  - Body
  ```json
  { "email": "user@example.com", "password": "plain-text" }
  ```
  - 응답
  ```json
  { "id": 1, "email": "user@example.com" }
  ```

- 로그인: `POST /api/v1/users/login`
  - Body
  ```json
  { "email": "user@example.com", "password": "plain-text" }
  ```
  - 응답
  ```json
  { "valid": true }
  ```

---

## 보안 메모
- 비밀번호는 `PasswordEncoder`로 해시 저장됩니다.
- 토큰 인증(OAuth2/JWT)은 [ADR-006](../msa-project-hub/docs/adr/ADR-006-authentication-strategy.md)에 따라 추후 도입 예정입니다.
- 시크릿/환경설정 관리: [ADR-012](../msa-project-hub/docs/adr/ADR-012-secret-and-config-management.md)
- CORS 기준 정책: [ADR-013](../msa-project-hub/docs/adr/ADR-013-cors-baseline-policy.md)


