#############################################
## Auto Build (Gradle 빌드 포함)
#############################################
#
## 1단계: 빌드용 Gradle 이미지
#FROM gradle:8.5-jdk17 AS builder
#WORKDIR /app
#
## 소스 복사
#COPY . .
#
## 빌드 실행
#RUN ./gradlew clean build --no-daemon
#
## 2단계: 실행용 JRE 이미지
#FROM openjdk:17-jdk-slim
#WORKDIR /app
#
## 빌드된 JAR 복사 (통일된 경로로)
#COPY --from=builder /app/build/libs/*.jar app.jar
#
## 실행
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]


###########################################
# Manual Build (외부 빌드 후 이미지 생성)
###########################################
FROM openjdk:17-jdk-slim
WORKDIR /app

# 수동 빌드한 JAR 경로 지정
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
