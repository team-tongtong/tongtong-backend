# TONGTONG-SERVER

---
**통통**은 <!-- TODO: 서비스 한 줄 소개 (예: OO한 사람들을 위한 OO 플랫폼입니다.) -->
<br/><br/>
본 저장소는 통통 서비스의 백엔드 서버로, <!-- TODO: 관리하는 데이터와 제공하는 API 소개 -->
<br/>

## **✨ Contributors**

| <a href="https://github.com/seunghee0321"><img width="120" alt="강승희 프로필" src="https://github.com/user-attachments/assets/228c2a2e-2918-489e-9648-8df0552dc36e" /></a> | <a href="https://github.com/swo0nyy"><img width="120" alt="오소원 프로필" src="https://github.com/user-attachments/assets/8c64e0ab-564a-4d7f-b201-eb7260e4007d" /></a> |
|:-------------------------------------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|                         **강승희** <br> [@seunghee0321](https://github.com/seunghee0321)                         |                                                       **오소원** <br> [@swo0nyy](https://github.com/swo0nyy)                                                        |

<br/>

## **🔗 Links**

| 구분 | 주소 |
| :--- | :--- |
| 배포 서버 | https://tongtong-api.p-e.kr |
| Swagger | https://tongtong-api.p-e.kr/swagger-ui/index.html |
| ERD | <!-- TODO: ERD Cloud 링크 --> |

<br/>

## **⚒️ Tech Stack**

| 항목 | 기술 스택 |
| :--- | :--- |
| Language / Runtime | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle |
| Database | MySQL 8 |
| ORM | Spring Data JPA |
| API Docs | springdoc-openapi (Swagger UI) |
| Infra / CI-CD | AWS EC2, Docker, Nginx, GitHub Actions |

<br/>

## **✨ 주요 기능**

| 도메인 | 기능 |
| :--- | :--- |
| `user` | <!-- TODO --> |
| `global` | 공통 응답 · 에러 코드, 전역 예외 처리, CORS · Swagger · JPA 설정 |

<br/>

## **🚀 배포 & 운영**

- **CI**: `main` · `develop` 대상 PR 생성 시 GitHub Actions로 빌드 · 테스트 자동 검증
- **CD**: `develop` 머지 시 Docker 이미지 빌드 → GHCR 푸시 → EC2 배포 자동화
- **HTTPS**: Nginx 리버스 프록시 + 인증서 자동 갱신

<br/>

## **🌿 Branch Strategy**

| 브랜치 | 역할 |
| :--- | :--- |
| `main` | 배포 안정 버전 |
| `develop` | 개발 통합 (기본 브랜치) |
| `feat/#{이슈번호}/{기능명}` | 기능 개발 → `develop` 으로 PR |
| `hotfix/#{이슈번호}/{버그명}` | 긴급 수정 |

커밋 · PR 컨벤션은 [CLAUDE.md](CLAUDE.md) 참고

<br/>

## **🗂️ Project Structure**

```text
🗃️ tongtong-server
└── 📂 src/main/java/com/likelion/tongtong
    ├── 📂 domain
    │   └── 📁 user            # 회원
    │
    └── 📂 global
        ├── 📁 common          # BaseTimeEntity, 페이지 응답
        ├── 📁 config          # CORS, JPA, Swagger 설정
        ├── 📁 response        # 공통 응답(ApiResponse), 성공·에러 코드
        └── 📁 exception       # 비즈니스 예외, 전역 예외 처리
```