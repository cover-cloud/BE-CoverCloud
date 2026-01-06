#!/bin/bash

# CoverCloud 백엔드 서비스 시작 스크립트

echo "========================================="
echo "CoverCloud 백엔드 서비스 시작"
echo "========================================="

# JAR 파일 경로 설정
USER_SERVICE="user-service/build/libs/user-service-0.0.1.jar"
MUSIC_SERVICE="music-service/build/libs/music-service-0.0.1.jar"
COVER_SERVICE="cover-service/build/libs/cover-service-0.0.1.jar"
GATEWAY="api-gateway/build/libs/api-gateway-0.0.1.jar"

# 로그 디렉토리 생성
mkdir -p logs

# 1. User Service 시작 (8081)
echo "1. User Service 시작 중... (포트: 8081)"
nohup java -jar $USER_SERVICE > logs/user-service.log 2>&1 &
USER_PID=$!
echo "   PID: $USER_PID"
sleep 5

# 2. Music Service 시작 (8083)
echo "2. Music Service 시작 중... (포트: 8083)"
nohup java -jar $MUSIC_SERVICE > logs/music-service.log 2>&1 &
MUSIC_PID=$!
echo "   PID: $MUSIC_PID"
sleep 5

# 3. Cover Service 시작 (8082)
echo "3. Cover Service 시작 중... (포트: 8082)"
nohup java -jar $COVER_SERVICE > logs/cover-service.log 2>&1 &
COVER_PID=$!
echo "   PID: $COVER_PID"
sleep 5

# 4. API Gateway 시작 (8080)
echo "4. API Gateway 시작 중... (포트: 8080)"
nohup java -jar $GATEWAY > logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo "   PID: $GATEWAY_PID"
sleep 5

echo ""
echo "========================================="
echo "모든 서비스가 시작되었습니다!"
echo "========================================="
echo "User Service:    http://localhost:8081 (PID: $USER_PID)"
echo "Music Service:   http://localhost:8083 (PID: $MUSIC_PID)"
echo "Cover Service:   http://localhost:8082 (PID: $COVER_PID)"
echo "API Gateway:     http://localhost:8080 (PID: $GATEWAY_PID)"
echo ""
echo "프론트엔드는 http://localhost:8080 을 사용하세요"
echo ""
echo "로그 파일 위치: logs/"
echo "서비스 중지: ./stop-all.sh"
echo "========================================="

# PID 저장
echo $USER_PID > logs/user-service.pid
echo $MUSIC_PID > logs/music-service.pid
echo $COVER_PID > logs/cover-service.pid
echo $GATEWAY_PID > logs/api-gateway.pid
