# BE-CoverCloud

[<img src="https://img.shields.io/badge/-readme.md-important?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/-tech blog-blue?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/release-v0.0.0-yellow?style=flat&logo=google-chrome&logoColor=white" />]()
<br/> [<img src="https://img.shields.io/badge/프로젝트 기간-2025.11.13~2026.03.31-green?style=flat&logo=&logoColor=white" />]()


## 📝 소개
BE-CoverCloud는 MSA(Microservices Architecture) 기반의 백엔드 시스템입니다. 각 서비스는 독립적으로 배포 및 확장 가능하며, 공통 라이브러리를 통해 코드 재사용성을 높였습니다.

다음과 같은 내용을 작성할 수 있습니다.
- 프로젝트 소개
- 프로젝트 화면 구성 또는 프로토 타입
- 프로젝트 API 설계
- 사용한 기술 스택
- 프로젝트 아키텍쳐
- 기술적 이슈와 해결 과정

필요한 기술 스택에 대한 logo는 [skills 폴더](/skills/)에서 다운로드 받을 수 있습니다.

<br />

// ### 화면 구성
// |Screen #1|Screen #2|
// |:---:|:---:|

[//]: # (|<img src="https://user-images.githubusercontent.com/80824750/208456048-acbf44a8-cd71-4132-b35a-500047adbe1c.gif" width="400"/>|<img src="https://user-images.githubusercontent.com/80824750/208456234-fb5fe434-aa65-4d7a-b955-89098d5bbe0b.gif" width="400"/>|)

// ### 프로토타입

[//]: # (<img src="https://user-images.githubusercontent.com/80824750/208454673-0449e49c-57c6-4a6b-86cf-66c5b1e623dc.png">)

<br />

## 🗂️ APIs
작성한 API는 아래에서 확인할 수 있습니다.

👉🏻 [API 바로보기](https://fanatical-maple-fe1.notion.site/API-Docs-2b79f9489f1580268353cc78b1873732?pvs=73)


<br />

## ⚙ 기술 스택


### Back-end (상세)
<div>


<svg xmlns="http://www.w3.org/2000/svg" width="80" height="101" viewBox="0 0 90 105">
  <rect x="5" y="5" width="80" height="101" rx="7" fill="#ffffff" stroke="#e5e5e5"/>
  <image href="https://noticon-static.tammolo.com/dgggcrkxq/image/upload/v1635384696/noticon/t2v5vtq6gp7d0bgspmyh.png" x="24"
y="22"
width="45"
height="45"/>
<text
x="45"
y="90"
font-size="11"
font-weight="500"
font-family="Montserrat, sans-serif"
text-anchor="middle"
fill="#333">
Kotlin</text>
</svg>
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/SpringBoot.png?raw=true" alt="Spring Boot" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/SpringSecurity.png?raw=true" alt="Spring Security" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/SpringDataJPA.png?raw=true" alt="Spring Data JPA" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Mysql.png?raw=true" alt="MySQL" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/JWT.png?raw=true" alt="JWT" width="80">

</div>


### Infra
<div>
<svg xmlns="http://www.w3.org/2000/svg" width="80" height="101" viewBox="0 0 90 105">
  <rect x="5" y="5" width="80" height="101" rx="8" fill="#ffffff" stroke="#e5e5e5"/>
  <image href="https://noticon-static.tammolo.com/dgggcrkxq/image/upload/v1593062577/noticon/uts4vbntu8ejsaxdtj1l.png
" x="20"
y="22"
width="50"
height="50"/>
<text
x="45"
y="90"
font-size="10.8"
font-family="Arial, sans-serif"
text-anchor="middle"
fill="#333">
GCP</text>
</svg>

[//]: # (<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/AWSEC2.png?raw=true" alt="AWS EC2" width="80">)
</div>

### Tools
<div>
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Github.png?raw=true" alt="GitHub" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Notion.png?raw=true" alt="Notion" width="80">
</div>


<br />

## 🛠️ 프로젝트 아키텍쳐
![no-image](https://user-images.githubusercontent.com/80824750/208294567-738dd273-e137-4bbf-8307-aff64258fe03.png)



<br />

## 🤔 기술적 이슈와 해결 과정
아래는 프로젝트에서 실제로 마주한 주요 문제들과 간단한 해결 요약입니다. 핵심 포인트만 빠르게 확인할 수 있도록 정리했습니다.

- Redis 도입
  - 사용 이유: 읽기 요청이 많은 데이터(예: 인기 커버 목록, 좋아요 카운트)의 응답 속도를 개선하고, 실시간 상태 관리(좋아요 집계, 즉시 로그아웃 처리 등)를 위해 메모리 기반인 Redis를 도입했습니다.
  - 기대 효과: 응답속도 개선, DB 트래픽 감소, 실시간성 향상으로 서비스 확장성 및 사용자 경험 개선

- 카카오/네이버 소셜 로그인
  - 문제: 소셜에서 내려오는 정보가 불완전하거나 provider 필드 때문에 DB 저장 오류가 날 수 있었습니다.
  - 해결: 프로바이더별 필드 추출을 안정화하고, Provider는 문자열(enum->STRING)로 저장하도록 수정했습니다.
  - 효과: 소셜 로그인 안정화 및 사용자 생성 오류 제거.

- 파일 업로드 및 CDN
	- 문제: 서버가 직접 파일을 처리하면 트래픽과 비용이 증가하고 확장성에 제약이 생깁니다.
	- 해결(Presigned URL): 서버는 S3 업로드용 임시 URL(프리사인드 URL)만 발급하고, 클라이언트가 해당 URL로 직접 업로드합니다. 업로드 완료 후 서버는 메타데이터(경로, 소유자 등)만 저장합니다.
	- 효과: 서버 트래픽·비용 절감, 대용량 업로드 확장성 확보, 만료되는 URL로 보안 유지.
	- 간단 팁: presign 만료 5~15분 권장, 업로드 전 파일타입/권한 검증, 브라우저 CORS 설정 확인.


- MSA(게이트웨이·공통 라이브러리)
  - 문제: 서비스가 여러 개라 라우팅과 공통 코드 관리가 번거로웠습니다.
  - 해결: API Gateway로 진입점을 통합하고 공통 코드를 `shared-library`로 분리했습니다.
  - 효과: 개발/배포가 쉬워지고 인증 흐름이 단순해졌습니다.

- JWT 발급 및 토큰 관리
  - 문제: 토큰 만료/무효화(로그아웃) 관리가 필요했습니다.
  - 해결: 액세스/리프레시 토큰 정책을 도입하고 Redis로 블랙리스트/리프레시 상태를 관리했습니다.
  - 효과: 보안성 향상 및 즉시 로그아웃 처리 가능.

---

