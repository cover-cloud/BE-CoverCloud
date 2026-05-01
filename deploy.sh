#!/bin/bash

DEPLOY_DIR="/home/ubuntu/deploy"

# .env 파일에서 환경변수 로드
if [ -f "$DEPLOY_DIR/.env" ]; then
    set -a
    source "$DEPLOY_DIR/.env"
    set +a
    echo ">>> .env 파일 로드 완료"
else
    echo ">>> 경고: .env 파일이 없습니다!"
fi

# 1. 기존 실행 중인 Java 프로세스 종료
echo ">>> Stopping existing services..."
fuser -k 8080/tcp || true
fuser -k 8081/tcp || true
fuser -k 8082/tcp || true
fuser -k 8083/tcp || true

sleep 2

# 2. JAR 파일들이 위치한 경로로 이동
cd "$DEPLOY_DIR"

# 3. 서비스 실행 (로그는 .log 파일로 남김)
echo ">>> Starting services..."
nohup java -jar -Dspring.profiles.active=prod api-gateway-0.0.1.jar > gateway.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod user-service-0.0.1.jar > user.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod music-service-0.0.1.jar > music.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod cover-service-0.0.1.jar > cover.log 2>&1 &

echo ">>> Deployment script finished!"
