# 테스트 계정 생성 가이드

## 🚀 빠른 시작 (추천 방법)

### 🎨 웹 페이지에서 로그인 (가장 쉬움!)

**테스트 로그인 페이지 접속:**
```
http://localhost:8080/test-login.html
```

**또는 API Gateway 거쳐서:**
```
http://localhost:8080/api/test/login-page
```

이 페이지에서:
1. ✅ 테스트 계정 5개를 자동 생성
2. ✅ 원하는 계정 선택
3. ✅ "로그인" 버튼 클릭
4. ✅ Access Token 획득
5. ✅ 토큰 자동 저장 및 복사

---

## 🔧 API로 수동 로그인

### 1️⃣ 테스트 계정 5개 생성

```bash
GET http://localhost:8080/api/test/create-test-users
```

**응답 예시:**
```json
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
      {
        "id": 2,
        "socialId": "testuser2",
        "nickname": "Test User 2",
        "provider": "KAKAO",
        "email": "testuser2@test.com",
        "status": "created"
      },
      ...
    ]
  },
  "message": "5개의 테스트 계정이 생성되었습니다."
}
```

---

### 2️⃣ 테스트 계정으로 토큰 발급

각 사용자별로 토큰을 받을 수 있습니다.

```bash
POST http://localhost:8080/api/test/get-token?userId=1
```

**응답 예시:**
```json
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

### 3️⃣ 발급받은 토큰으로 API 호출

**Authorization 헤더에 accessToken 포함:**

```bash
GET http://localhost:8080/api/user/profile
Authorization: Bearer {accessToken}
```

---

## 📝 테스트 계정 정보

| ID | Social ID | Nickname | Email |
|----|-----------|----------|-------|
| 1 | testuser1 | Test User 1 | testuser1@test.com |
| 2 | testuser2 | Test User 2 | testuser2@test.com |
| 3 | testuser3 | Test User 3 | testuser3@test.com |
| 4 | testuser4 | Test User 4 | testuser4@test.com |
| 5 | testuser5 | Test User 5 | testuser5@test.com |

---

## 🧪 테스트 시나리오

### 시나리오 1: 커버 작성 후 다른 계정으로 좋아요

```bash
# 1. testuser1로 로그인
POST /api/test/get-token?userId=1
→ accessToken1 받기

# 2. testuser1이 커버 생성
POST /api/cover/create
Authorization: Bearer {accessToken1}
Body: { "coverName": "...", ... }

# 3. testuser2로 로그인
POST /api/test/get-token?userId=2
→ accessToken2 받기

# 4. testuser2가 해당 커버에 좋아요
POST /api/cover/{coverId}/like
Authorization: Bearer {accessToken2}

# 5. testuser1이 자신의 커버 좋아요 목록 조회
GET /api/cover/me/likes
Authorization: Bearer {accessToken1}
```

### 시나리오 2: 댓글 작성 테스트

```bash
# 1. testuser1로 커버 생성
# 2. testuser2~5로 댓글 작성
POST /api/cover/{coverId}/comment
Authorization: Bearer {accessToken}
Body: { "content": "좋은 커버네요!" }

# 3. testuser1이 댓글 조회
GET /api/cover/{coverId}/comments
```

### 시나리오 3: 프로필 업데이트

```bash
# 닉네임 변경
POST /api/user/profile
Authorization: Bearer {accessToken}
Body: { "nickname": "Updated Nickname", "profileImage": "..." }
```

---

## 💡 추가 기능

### 1️⃣ 같은 계정 다시 생성하면?
- **이미 존재하는 경우**: 기존 계정 반환, `status: "already_exists"`
- 테스트를 위해 삭제 후 다시 생성하려면 DB에서 직접 삭제

### 2️⃣ 토큰 유효기간
- **accessToken**: 1시간
- **refreshToken**: 7일

### 3️⃣ 테스트 데이터 초기화 (필요시)

```sql
-- MySQL에서 실행
DELETE FROM user WHERE social_id LIKE 'testuser%';
```

---

## 🔧 운영 환경에서는?

테스트 엔드포인트는 **개발 환경**에서만 활성화되도록 설정할 수 있습니다:

### application.yml 조건부 설정

```yaml
# 개발 환경
spring:
  profiles:
    active: dev

---
spring:
  config:
    activate:
      on-profile: dev
  # 테스트 엔드포인트 활성화
app:
  test-api-enabled: true

---
spring:
  config:
    activate:
      on-profile: prod
  # 테스트 엔드포인트 비활성화
app:
  test-api-enabled: false
```

### Controller에서 조건 확인

```kotlin
@GetMapping("/create-test-users")
fun createTestUsers(
    @Value("\${app.test-api-enabled:false}") testApiEnabled: Boolean
): ResponseEntity<ApiResponse<Map<String, Any>>> {
    if (!testApiEnabled) {
        return ResponseEntity.status(403).body(
            ApiResponse(success = false, message = "Test API is disabled")
        )
    }
    // ... 기존 로직
}
```

---

## 📌 주요 API 엔드포인트

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/test/create-test-users` | 테스트 계정 5개 생성 |
| POST | `/api/test/get-token?userId={id}` | 테스트 토큰 발급 |
| POST | `/api/cover/create` | 커버 생성 |
| POST | `/api/cover/{coverId}/like` | 커버 좋아요 |
| GET | `/api/cover/me/likes` | 내 좋아요 목록 |
| POST | `/api/cover/{coverId}/comment` | 댓글 작성 |
| GET | `/api/cover/{coverId}/comments` | 댓글 조회 |

---

## 🎯 추천 테스트 플로우

1. ✅ `/api/test/create-test-users` → 5개 계정 생성
2. ✅ `/api/test/get-token?userId=1` → accessToken1 획득
3. ✅ `/api/test/get-token?userId=2` → accessToken2 획득
4. ✅ `/api/test/get-token?userId=3` → accessToken3 획득
5. ✅ 각 accessToken으로 다양한 기능 테스트

---

## ❓ 자주 묻는 질문

**Q1. 테스트 계정을 삭제하려면?**
```sql
DELETE FROM user WHERE social_id LIKE 'testuser%';
```

**Q2. 토큰이 만료되면?**
- POST `/api/auth/refresh` 엔드포인트로 새 토큰 발급

**Q3. 프로필 이미지를 설정하려면?**
- 기본: `https://via.placeholder.com/150?text={nickname}`
- 커스텀: POST `/api/user/profile`에서 `profileImage` 필드 사용


