# 멀티 스테이지 빌드를 사용하여 이미지 크기 최적화
FROM gradle:8.5-jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 파일들을 먼저 복사 (의존성 캐싱 최적화)
COPY build.gradle settings.gradle ./
COPY gradle gradle

# 의존성 다운로드 (이 레이어는 소스 코드가 변경되지 않으면 캐시됨)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN gradle bootJar --no-daemon -x test

# 런타임 이미지
FROM eclipse-temurin:21-jre

# 애플리케이션 실행을 위한 사용자 생성 (보안상 권장)
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
