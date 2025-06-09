# 1. Java 17 이미지 기반
FROM openjdk:17-jdk-slim

# 2. 빌드된 jar 복사 (build/libs/*.jar 중 하나 복사)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 컨테이너 실행 시 jar 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
