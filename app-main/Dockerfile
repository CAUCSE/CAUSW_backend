# 1. Zulu OpenJDK 17 기반 이미지 사용
FROM azul/zulu-openjdk-alpine:17

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. jar 파일 복사 (빌드 결과물에 맞게 경로 조정)
COPY app-main/build/libs/*.jar app.jar

# 4. 리소스 및 환경 파일 복사
COPY /app-main/src/main/resources/application-local.yml /app/src/main/resources/application.yml
COPY /app-main/src/main/resources/dongnae_firebase_key.json /app/src/main/resources/dongnae_firebase_key.json

# 5. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]


