# BE-CoverCloud

## 프로젝트 소개
BE-CoverCloud는 MSA(Microservices Architecture) 기반의 백엔드 시스템입니다. 각 서비스는 독립적으로 배포 및 확장 가능하며, 공통 라이브러리를 통해 코드 재사용성을 높였습니다.

## 주요 기능

- **api-gateway**
	- 외부 요청을 각 마이크로서비스로 라우팅
	- 인증/인가 처리
	- 공통 로깅 및 에러 핸들링

- **user-service**
	- 회원가입, 로그인, 사용자 정보 관리
	- 세션 및 인증 토큰 관리 (Redis 활용)
	- 사용자 정보 수정/조회/삭제

- **cover-service**
	- 커버 이미지 등록, 수정, 삭제, 조회
	- 커버 관련 데이터 캐싱 (Redis 활용)

- **music-service**
	- 음악 데이터 등록, 수정, 삭제, 조회
	- 인기 음악, 최근 재생 기록 캐싱 (Redis 활용)

- **shared-library**
	- 공통 응답 포맷, 예외 처리, DTO, 유틸리티 함수 제공

## 서비스 구조 (MSA)
- **api-gateway**: 외부 요청을 각 마이크로서비스로 라우팅하고, 인증/인가, 로깅 등 공통 처리를 담당합니다.
- **user-service**: 사용자 정보 관리, 회원가입/로그인 등 사용자 관련 비즈니스 로직을 처리합니다.
- **cover-service**: 커버 이미지 및 관련 데이터 관리, 커버 생성/수정/조회 기능을 제공합니다.
- **music-service**: 음악 데이터 관리, 음악 등록/조회 등 음악 관련 기능을 담당합니다.
- **shared-library**: 각 서비스에서 공통으로 사용하는 유틸리티, 예외 처리, DTO, 공통 설정 등을 제공합니다.

## 사용 기술 및 적용 위치

- **Kotlin, Spring Boot**  
	모든 마이크로서비스의 주요 개발 언어 및 프레임워크로 사용. REST API, DI, Spring Data 등 핵심 기능 구현에 활용.

- **Gradle (Kotlin DSL)**  
	전체 프로젝트 및 각 모듈의 빌드, 의존성 관리에 사용.

- **Redis**  
	- **user-service**: 세션 관리, 인증 토큰 저장, 임시 데이터 캐싱 등에 사용.
	- **cover-service**: 커버 이미지 메타데이터 캐싱, 자주 조회되는 데이터의 성능 향상에 사용.
	- **music-service**: 인기 음악 목록, 최근 재생 기록 등 캐시 데이터 저장에 사용.

- **공통 라이브러리(shared-library)**  
	각 서비스에서 중복되는 코드(예: 공통 응답 포맷, 예외 처리, 유틸리티 함수 등)를 관리하여 코드 일관성과 재사용성 향상.

- **Git**  
	소스 버전 관리 및 협업에 사용.

- **(선택) Docker**  
	서비스 컨테이너화 및 배포 자동화에 사용 가능(필요시).

## 설치 및 실행 방법

### 1. 환경 준비
- JDK 17 이상
- Gradle 7.x 이상 (Wrapper 포함)
- Redis 서버 (localhost:6379 등 기본 포트 사용)

### 2. 빌드 및 실행
```bash
# 전체 서비스 빌드
./gradlew build

# 각 서비스 실행 예시
cd api-gateway && ./gradlew bootRun
cd user-service && ./gradlew bootRun
cd cover-service && ./gradlew bootRun
cd music-service && ./gradlew bootRun

# 모든 서비스 일괄 실행
./start-all.sh
```

## 환경설정
- 각 서비스별 `src/main/resources/application.yml`에서 포트, DB, Redis 등 환경 변수 설정
- Redis 연결 정보는 각 서비스의 `application.yml`에서 `spring.redis.host`, `spring.redis.port` 등으로 지정
- 공통 설정은 `shared-library` 또는 각 서비스의 `application.yml` 참고

## 기타 안내
- 모든 서비스는 독립적으로 실행 및 배포 가능
- 로그는 logs/ 디렉터리에 저장
- 서비스 간 통신은 REST API 기반
# BE-CoverCloud