# Auto build
# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

# 2단계: 빌드된 JAR만 복사
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


# Manual build
## 1. Java 17 이미지 기반
#FROM openjdk:17-jdk-slim
#
## 2. 빌드된 jar 복사 (build/libs/*.jar 중 하나 복사)
#ARG JAR_FILE=build/libs/*.jar
#COPY ${JAR_FILE} app.jar
#
## 3. 컨테이너 실행 시 jar 실행
#ENTRYPOINT ["java", "-jar", "/app.jar"]