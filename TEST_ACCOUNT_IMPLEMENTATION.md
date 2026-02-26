# 테스트 계정 기능 추가 - 변경 사항 요약

## 📋 변경된 파일 목록

### 1. UserController.kt (수정)
**파일**: `user-service/src/main/kotlin/com/covercloud/user/controller/UserController.kt`

**추가 내용**:
- 새로운 `TestUserController` 클래스 추가
- 엔드포인트 1: `GET /api/test/create-test-users` - 테스트 계정 5개 생성
- 엔드포인트 2: `POST /api/test/get-token?userId={id}` - 테스트 토큰 발급

### 2. UserService.kt (수정)
**파일**: `user-service/src/main/kotlin/com/covercloud/user/service/UserService.kt`

**추가 메서드**:
- `createTestUsers(testUsersList)` - 테스트 계정 생성
- `getTestTokens(userId)` - 테스트 계정용 토큰 발급

---

## 🚀 사용 방법

### Step 1: 서버 실행
```bash
# user-service 시작
cd user-service
./gradlew bootRun
```

### Step 2: 테스트 계정 생성
```bash
curl http://localhost:8080/api/test/create-test-users
```

### Step 3: 토큰 발급받기
```bash
curl -X POST http://localhost:8080/api/test/get-token?userId=1
```

### Step 4: API 테스트
```bash
curl -H "Authorization: Bearer {accessToken}" http://localhost:8080/api/user/profile
```

---

## 📊 생성되는 테스트 계정

```
ID  | Social ID  | Nickname     | Email              | Provider
----|------------|--------------|-------------------|----------
1   | testuser1  | Test User 1  | testuser1@test.com | KAKAO
2   | testuser2  | Test User 2  | testuser2@test.com | KAKAO
3   | testuser3  | Test User 3  | testuser3@test.com | KAKAO
4   | testuser4  | Test User 4  | testuser4@test.com | KAKAO
5   | testuser5  | Test User 5  | testuser5@test.com | KAKAO
```

---

## 🔗 라우팅 설정

**API Gateway (api-gateway/src/main/resources/application.yml)**

이미 설정되어 있는 라우팅:
```yaml
routes:
  - id: user-service
    uri: http://localhost:8081
    predicates:
      - Path=/api/auth/**, /api/user/**, /api/test/**, /oauth2/**, /login/oauth2/**
```

✅ `/api/test/**` 경로가 이미 포함되어 있으므로 별도 설정 불필요!

---

## 💻 API 명세

### 1. 테스트 계정 생성
```
GET /api/test/create-test-users

응답:
{
  "success": true,
  "data": {
    "created": 5,
    "users": [
      {
        "id": 1,
        "socialId": "testuser1",
        "nickname": "Test User 1",
        "provider": "KAKAO",
        "email": "testuser1@test.com",
        "status": "created"
      },
      ...
    ]
  },
  "message": "5개의 테스트 계정이 생성되었습니다."
}
```

### 2. 테스트 토큰 발급
```
POST /api/test/get-token?userId=1

응답:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "테스트 토큰 발급 완료"
}
```

---

## 📌 주의사항

### 운영 환경에서 테스트 엔드포인트 비활성화 (권장)

테스트 기능을 개발 환경에서만 활성화하려면:

**application.yml (user-service)**
```yaml
app:
  test-api-enabled: ${TEST_API_ENABLED:true}  # 기본값: true (개발)
```

**application-prod.yml**
```yaml
app:
  test-api-enabled: false
```

**Controller 수정**
```kotlin
@GetMapping("/create-test-users")
fun createTestUsers(
    @Value("\${app.test-api-enabled:false}") testApiEnabled: Boolean
): ResponseEntity<...> {
    if (!testApiEnabled) {
        return ResponseEntity.status(403).body(
            ApiResponse(success = false, message = "Test API is disabled")
        )
    }
    // ...
}
```

---

## 🧪 테스트 예제

### 예제 1: 기본 사용법
```bash
# 1. 계정 생성
curl http://localhost:8080/api/test/create-test-users

# 2. User 1 토큰 받기
curl -X POST http://localhost:8080/api/test/get-token?userId=1

# 3. 프로필 조회
curl -H "Authorization: Bearer {accessToken}" \
  http://localhost:8080/api/user/profile
```

### 예제 2: 여러 계정으로 상호작용 테스트
```bash
# User 1 토큰
TOKEN1=$(curl -s -X POST http://localhost:8080/api/test/get-token?userId=1 \
  | jq -r '.data.accessToken')

# User 2 토큰
TOKEN2=$(curl -s -X POST http://localhost:8080/api/test/get-token?userId=2 \
  | jq -r '.data.accessToken')

# User 1이 커버 생성
COVER_ID=$(curl -s -X POST http://localhost:8080/api/cover/create \
  -H "Authorization: Bearer $TOKEN1" \
  -H "Content-Type: application/json" \
  -d '{...cover data...}' | jq -r '.data.id')

# User 2가 해당 커버에 좋아요
curl -X POST http://localhost:8080/api/cover/$COVER_ID/like \
  -H "Authorization: Bearer $TOKEN2"
```

---

## 📚 관련 문서

- `TEST_ACCOUNT_GUIDE.md` - 상세 테스트 가이드
- `TestAccounts.postman_collection.json` - Postman 컬렉션

---

## ✅ 체크리스트

- [x] TestUserController 구현
- [x] createTestUsers 메서드 구현
- [x] getTestTokens 메서드 구현
- [x] API Gateway 라우팅 확인
- [x] 가이드 문서 작성
- [x] Postman 컬렉션 작성
- [ ] 운영 환경 설정 (필요시)

---

## 🔍 테스트 계정 조회 및 삭제 (DB)

### 테스트 계정 조회
```sql
SELECT * FROM user WHERE social_id LIKE 'testuser%';
```

### 테스트 계정 삭제
```sql
DELETE FROM user WHERE social_id LIKE 'testuser%';
```

### 관련 데이터도 함께 삭제 (옵션)
```sql
-- 테스트 계정의 좋아요 삭제
DELETE FROM cover_like WHERE user_id IN 
  (SELECT id FROM user WHERE social_id LIKE 'testuser%');

-- 테스트 계정의 댓글 삭제
DELETE FROM comment WHERE user_id IN 
  (SELECT id FROM user WHERE social_id LIKE 'testuser%');

-- 테스트 계정의 커버 삭제
DELETE FROM cover WHERE artist_id IN 
  (SELECT id FROM user WHERE social_id LIKE 'testuser%');

-- 테스트 계정 삭제
DELETE FROM user WHERE social_id LIKE 'testuser%';
```

---

## 💡 추가 기능 아이디어

만약 더 필요하다면 다음 기능들을 추가할 수 있습니다:

1. **더미 커버 데이터 생성**: `/api/test/create-dummy-covers`
2. **더미 댓글 생성**: `/api/test/create-dummy-comments`
3. **테스트 데이터 초기화**: `/api/test/reset-all`
4. **특정 사용자 데이터 확인**: `GET /api/test/user/{userId}/details`


