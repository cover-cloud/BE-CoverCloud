# 테스트 계정 로그인 API

## 🚀 빠른 시작

프론트에서 이 API를 호출하면 **테스트 계정으로 바로 로그인**됩니다.

---

## 📝 API 명세

### 엔드포인트
```
POST /api/auth/test-login
```

### 요청 예시

```javascript
// JavaScript/React
const response = await fetch('http://localhost:8080/api/auth/test-login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    socialId: 'testuser1',
    nickname: 'Test User 1'
  })
});

const data = await response.json();
console.log(data);
```

### 요청 본문 (Body)
```json
{
  "socialId": "testuser1",
  "nickname": "Test User 1"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| socialId | string | ✅ | 테스트 계정 ID (예: testuser1, testuser2 등) |
| nickname | string | ❌ | 닉네임 (생략하면 socialId를 닉네임으로 사용) |

---

## ✅ 응답 예시

### 성공 응답 (200 OK)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "socialId": "testuser1",
    "nickname": "Test User 1",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "provider": "TEST"
  },
  "message": "테스트 로그인 성공"
}
```

### 실패 응답 (400 Bad Request)
```json
{
  "success": false,
  "message": "테스트 로그인 실패"
}
```

---

## 🎯 사용 예시

### 1️⃣ 기본 사용법 (curl)

```bash
curl -X POST http://localhost:8080/api/auth/test-login \
  -H "Content-Type: application/json" \
  -d '{
    "socialId": "testuser1",
    "nickname": "Test User 1"
  }'
```

### 2️⃣ React 예시

```javascript
import { useState } from 'react';

export default function TestLogin() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleTestLogin = async (socialId, nickname) => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('/api/auth/test-login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          socialId,
          nickname
        })
      });

      const data = await response.json();

      if (data.success) {
        // 토큰 저장
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
        localStorage.setItem('userId', data.data.userId);

        // 메인 페이지로 이동
        window.location.href = '/main';
      } else {
        setError(data.message);
      }
    } catch (err) {
      setError('로그인 중 오류가 발생했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>테스트 계정 로그인</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      
      <button onClick={() => handleTestLogin('testuser1', 'Test User 1')} disabled={loading}>
        {loading ? '로그인 중...' : 'User 1로 로그인'}
      </button>
      <button onClick={() => handleTestLogin('testuser2', 'Test User 2')} disabled={loading}>
        {loading ? '로그인 중...' : 'User 2로 로그인'}
      </button>
      <button onClick={() => handleTestLogin('testuser3', 'Test User 3')} disabled={loading}>
        {loading ? '로그인 중...' : 'User 3로 로그인'}
      </button>
      <button onClick={() => handleTestLogin('testuser4', 'Test User 4')} disabled={loading}>
        {loading ? '로그인 중...' : 'User 4로 로그인'}
      </button>
      <button onClick={() => handleTestLogin('testuser5', 'Test User 5')} disabled={loading}>
        {loading ? '로그인 중...' : 'User 5로 로그인'}
      </button>
    </div>
  );
}
```

### 3️⃣ Vue 예시

```javascript
// TestLogin.vue
<template>
  <div>
    <h2>테스트 계정 로그인</h2>
    <p v-if="error" style="color: red">{{ error }}</p>
    
    <button 
      v-for="user in testUsers" 
      :key="user.socialId"
      @click="handleTestLogin(user.socialId, user.nickname)"
      :disabled="loading"
    >
      {{ loading ? '로그인 중...' : `${user.nickname}로 로그인` }}
    </button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      error: null,
      testUsers: [
        { socialId: 'testuser1', nickname: 'Test User 1' },
        { socialId: 'testuser2', nickname: 'Test User 2' },
        { socialId: 'testuser3', nickname: 'Test User 3' },
        { socialId: 'testuser4', nickname: 'Test User 4' },
        { socialId: 'testuser5', nickname: 'Test User 5' }
      ]
    };
  },
  methods: {
    async handleTestLogin(socialId, nickname) {
      this.loading = true;
      this.error = null;

      try {
        const response = await fetch('/api/auth/test-login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ socialId, nickname })
        });

        const data = await response.json();

        if (data.success) {
          localStorage.setItem('accessToken', data.data.accessToken);
          localStorage.setItem('refreshToken', data.data.refreshToken);
          localStorage.setItem('userId', data.data.userId);

          this.$router.push('/main');
        } else {
          this.error = data.message;
        }
      } catch (err) {
        this.error = '로그인 중 오류가 발생했습니다';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
```

### 4️⃣ 유틸리티 함수

```javascript
// utils/testLogin.js
export async function testLogin(socialId, nickname) {
  const response = await fetch('/api/auth/test-login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      socialId,
      nickname: nickname || socialId
    })
  });

  const data = await response.json();

  if (!data.success) {
    throw new Error(data.message);
  }

  // 토큰 저장
  localStorage.setItem('accessToken', data.data.accessToken);
  localStorage.setItem('refreshToken', data.data.refreshToken);
  localStorage.setItem('userId', data.data.userId);

  return data.data;
}

// 사용법
// testLogin('testuser1', 'Test User 1')
//   .then(user => console.log('로그인 성공:', user))
//   .catch(err => console.error('로그인 실패:', err));
```

---

## 🧪 테스트 계정 목록

| Social ID | Nickname | Email |
|-----------|----------|-------|
| testuser1 | Test User 1 | testuser1@test.com |
| testuser2 | Test User 2 | testuser2@test.com |
| testuser3 | Test User 3 | testuser3@test.com |
| testuser4 | Test User 4 | testuser4@test.com |
| testuser5 | Test User 5 | testuser5@test.com |

---

## 💡 특징

✅ **자동 계정 생성**: 존재하지 않는 계정이면 자동으로 생성  
✅ **즉시 로그인**: 토큰 바로 발급  
✅ **간단한 요청**: socialId만 있으면 OK  
✅ **프론트 독립적**: API Gateway를 통해 호출 가능

---

## 🔌 로컬 개발 환경 설정

### 1. API Gateway 실행
```bash
cd api-gateway
./gradlew bootRun
```

### 2. User Service 실행
```bash
cd user-service
./gradlew bootRun
```

### 3. 프론트에서 API 호출
```javascript
// API_URL이 http://localhost:8080이면 그대로 호출
const response = await fetch('http://localhost:8080/api/auth/test-login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ socialId: 'testuser1' })
});
```

---

## ⚠️ 주의사항

### 운영 환경에서 테스트 로그인 비활성화 (권장)

```yaml
# application.yml (user-service)
app:
  test-login-enabled: ${TEST_LOGIN_ENABLED:true}  # 기본: true (개발 환경)
```

```yaml
# application-prod.yml
app:
  test-login-enabled: false
```

```kotlin
// Controller에서 확인
@PostMapping("/test-login")
fun testLogin(
    @Value("\${app.test-login-enabled:false}") testLoginEnabled: Boolean,
    @RequestBody request: TestLoginRequest
): ResponseEntity<ApiResponse<Map<String, Any>>> {
    if (!testLoginEnabled) {
        return ResponseEntity.status(403).body(
            ApiResponse(success = false, message = "Test login is disabled")
        )
    }
    // ... 기존 로직
}
```

---

## 🔍 토큰 검증

발급받은 토큰으로 API를 호출할 때 Authorization 헤더 포함:

```javascript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('/api/user/profile?userId=1', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
```

---

## 📞 문제 해결

### Q. 계정이 생성되지 않음
**A.** socialId가 올바른지 확인하세요. 소문자만 사용하세요.

### Q. 토큰이 만료됨
**A.** refreshToken으로 새 accessToken을 발급받으세요. (`/api/auth/refresh`)

### Q. CORS 오류 발생
**A.** API Gateway의 CORS 설정을 확인하세요. `allowedOrigins`에 프론트 URL이 포함되어 있는지 확인하세요.


