# api-messaging-svc

문자 메시지 전송, 수신자 기록 관리, 상태 콜백 처리(Twilio)를 담당하는 핵심 서비스입니다. 일부 지표는 `api-analytics-svc`와 연동됩니다.

---

## 기술 스택 및 실행 포트
- Spring Boot 3, Java 21, Gradle
- JPA + PostgreSQL
- Twilio SDK
- SpringDoc OpenAPI UI (Swagger)
- 기본 컨테이너 포트: `8080` ([ADR-005](../msa-project-hub/docs/adr/ADR-005-service-port-convention.md)) 

---

## 환경 변수
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`
- `MESSAGE_URL` (외부/내부 메시지 전송 API URL 필요 시)
- `MESSAGE_DASHBOARD_URL` (Analytics 연동 URL 필요 시)

application.properties 발췌:
```properties
spring.jpa.properties.hibernate.default_schema=api_messaging_svc
twilio.account-sid=${TWILIO_ACCOUNT_SID}
twilio.auth-token=${TWILIO_AUTH_TOKEN}
```

---

## 빌드 및 실행
```bash
# 로컬 실행
./gradlew bootRun

# 빌드(JAR)
./gradlew bootJar

# Docker 이미지 빌드
docker build -t api-messaging-svc:local .

# Docker 컨테이너 실행 (예시)
docker run --rm -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/messaging?currentSchema=api_messaging_svc" \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  -e TWILIO_ACCOUNT_SID=ACxxxxxxxx \
  -e TWILIO_AUTH_TOKEN=xxxxxxxx \
  api-messaging-svc:local
```

---

## Swagger UI
`/swagger-ui/index.html` 경로에서 API 문서를 확인할 수 있습니다.

---

## API 엔드포인트
베이스 경로: `/api/v1/messages`

- 메시지 전송: `POST /api/v1/messages`
  - Body 예시
  ```json
  {
    "userEmail": "user@example.com",
    "body": "안녕하세요",
    "recipients": ["+821012345678", "+821011111111"]
  }
  ```
  - 응답: 메시지 엔티티 또는 DTO

- 상태 콜백: `POST /api/v1/messages/status`
  - Content-Type: `application/x-www-form-urlencoded` (Twilio 기본)
  - Form 필드 예시: `MessageSid`, `MessageStatus`, `ErrorCode`, `ErrorMessage`
  - 응답: 200 OK (상태 업데이트)

- 사용자별 메시지 조회: `GET /api/v1/messages?userEmail=user@example.com`
  - 응답: 사용자 메시지 목록

---

## 참고
- 포트 정책: [ADR-005](../msa-project-hub/docs/adr/ADR-005-service-port-convention.md)
- Analytics 연동: [ADR-008](../msa-project-hub/docs/adr/ADR-008-messaging-ai-history.md) (Messaging AI History)
- API 버저닝: [ADR-003](../msa-project-hub/docs/adr/ADR-003-api-versioning-and-base-path.md)
- SMS 공급자 선정: [ADR-007](../msa-project-hub/docs/adr/ADR-007-sms-provider-selection.md)
