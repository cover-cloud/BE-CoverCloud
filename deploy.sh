#!/bin/bash

# 1. 기존 실행 중인 Java 프로세스 종료
echo ">>> Stopping existing services..."
fuser -k 8080/tcp || true
fuser -k 8081/tcp || true # 다른 서비스 포트가 있다면 추가
# ... 필요한 만큼 fuser 추가

# 2. JAR 파일들이 위치한 경로로 이동
cd /home/ubuntu/deploy

# 3. 서비스 실행 (로그는 .log 파일로 남김)
echo ">>> Starting services..."
nohup java -jar -Dspring.profiles.active=prod api-gateway.jar > gateway.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod user-service.jar > user.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod music-service.jar > music.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod cover-service.jar > cover.log 2>&1 &

echo ">>> Deployment script finished!"
