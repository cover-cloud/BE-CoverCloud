#!/bin/bash

# CoverCloud 백엔드 서비스 중지 스크립트

echo "========================================="
echo "CoverCloud 백엔드 서비스 중지"
echo "========================================="

# PID 파일에서 프로세스 종료
if [ -f logs/api-gateway.pid ]; then
    echo "API Gateway 중지 중..."
    kill $(cat logs/api-gateway.pid) 2>/dev/null
    rm logs/api-gateway.pid
fi

if [ -f logs/cover-service.pid ]; then
    echo "Cover Service 중지 중..."
    kill $(cat logs/cover-service.pid) 2>/dev/null
    rm logs/cover-service.pid
fi

if [ -f logs/music-service.pid ]; then
    echo "Music Service 중지 중..."
    kill $(cat logs/music-service.pid) 2>/dev/null
    rm logs/music-service.pid
fi

if [ -f logs/user-service.pid ]; then
    echo "User Service 중지 중..."
    kill $(cat logs/user-service.pid) 2>/dev/null
    rm logs/user-service.pid
fi

sleep 2

echo "========================================="
echo "모든 서비스가 중지되었습니다."
echo "========================================="
