FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# wget 설치 (healthcheck용)
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# JAR 파일 복사
COPY build/libs/baro-app.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
