# 아키텍처

## 선정 배경

계층형 아키텍처 (Controller - Service - Repository)를 채택한다.

**근거**

1. 도메인 로직이 얇다
   포트·어댑터 같은 추가 추상화의 이득이 없다.

2. 팀의 공통 기반이다
   전원이 익숙한 구조라 학습 비용이 없다.

3. 일정 제약
   짧은 프로젝트 기간에 구조 학습 비용을 최소화한다.

패키지는 **도메인별로 묶는다.** 계층별로 묶으면 작업 분배 시
같은 폴더에 전원이 파일을 추가하게 되어 머지 충돌이 잦아진다.

## 패키지 구조

com.likelion.tongtong
├── TongtongApplication.java
├── global
│   ├── config
│   │   ├── SwaggerConfig.java
│   │   ├── JpaConfig.java
│   │   └── CorsConfig.java
│   ├── common
│   │   ├── BaseTimeEntity.java
│   │   └── PageResponse.java
│   ├── response
│   │   ├── ApiResponse.java
│   │   ├── SuccessCode.java
│   │   └── ErrorCode.java
│   └── exception
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
└── domain
    └── user
        ├── controller
        ├── service
        ├── repository
        ├── entity
        └── dto

각 도메인은 `controller / service / repository / entity / dto` 5계층을 가진다.

## 계층별 책임

| 계층 | 책임 | 금지 |
|---|---|---|
| Controller | 요청 수신, DTO 변환, 응답 반환 | 비즈니스 로직 |
| Service | 비즈니스 로직, 트랜잭션 경계 | HTTP 관련 처리 |
| Repository | 데이터 접근 | 비즈니스 로직 |

## 데이터 흐름

Request
  → Controller (Request DTO 수신)
  → Service (비즈니스 로직, Entity 조작)
  → Repository (DB 접근)
  → Service (Response DTO 변환)
  → Controller (ApiResponse 래핑)
Response

Entity는 Service 계층을 벗어나지 않는다.

## 공통 규약

### 응답 포맷

모든 응답은 `ApiResponse<T>` 로 감싼다.

성공

{
  "code": "SUCCESS",
  "message": "요청에 성공했습니다.",
  "data": { }
}

실패

{
  "code": "ROOM_NOT_FOUND",
  "message": "모집방을 찾을 수 없어요.",
  "data": null
}

- `code` 는 문자열이다. 성공은 `SuccessCode`, 실패는 `ErrorCode` 에 정의한다
- `message` 는 화면에 표시될 수 있는 문구다. 도메인 에러는 해요체로 쓴다
- `SuccessCode.CREATED` 는 body의 `code` 만 정한다. 생성 API는 Controller에서 HTTP 201을 함께 지정한다

### 예외 처리

- 비즈니스 예외는 `BusinessException` 을 던진다
- 에러 코드는 `ErrorCode` enum에 정의한다
- `GlobalExceptionHandler` 에서 일괄 처리한다
- 요청 DTO의 Bean Validation 어노테이션에는 한글 `message` 를 반드시 적는다

| 예외 | `code` |
|---|---|
| 본문 JSON 오류 (`HttpMessageNotReadableException`) | `INVALID_REQUEST_BODY` |
| 파라미터 타입 오류·누락 | `INVALID_PARAMETER` |
| `@Valid`, 메서드 파라미터 검증 실패 | `INVALID_INPUT` |
| 없는 주소 | `RESOURCE_NOT_FOUND` |
| 지원하지 않는 HTTP 메서드 | `METHOD_NOT_ALLOWED` |
| 지원하지 않는 Content-Type | `UNSUPPORTED_MEDIA_TYPE` |
| 그 외 스프링 4xx | `HTTP_{상태코드}` |
| 처리되지 않은 예외 | `INTERNAL_SERVER_ERROR` |

### 프로파일

| 프로파일 | 용도 | DB |
|---|---|---|
| `local` | 로컬 개발 | MySQL 8 (접속 정보는 환경변수로 주입) |
| `test` | CI 테스트 | H2 인메모리 |
| `prod` | 운영 서버 | MySQL 8 (접속 정보는 환경변수로 주입) |

운영 DB는 MySQL 8이다. 드라이버는 `mysql-connector-j` 를 `runtimeOnly` 로 사용한다.

### CI/CD

| 워크플로 | 트리거 | 동작 |
|---|---|---|
| `ci.yml` | `main`·`develop` 대상 PR | `./gradlew clean build` (test 프로파일) |
| `cd.yml` | `develop` push, 수동 실행 | 빌드 → Docker 이미지를 GHCR에 push → EC2에 SSH로 접속해 컨테이너 교체 |

CD에 필요한 GitHub Secrets

| Secret | 설명 |
|---|---|
| `EC2_HOST` | 배포 서버 주소. 비어 있으면 이미지 push까지만 하고 배포는 건너뛴다 |
| `EC2_USERNAME` | SSH 사용자 (예: `ubuntu`) |
| `EC2_SSH_KEY` | SSH 개인키 (PEM 전체) |
| `DB_URL` | JDBC URL (예: `jdbc:mysql://{host}:3306/{db}`) |
| `DB_USERNAME` | DB 사용자 |
| `DB_PASSWORD` | DB 비밀번호 |

서버 구성(nginx·HTTPS)은 배포 서버 세팅 후 `docs/DEPLOYMENT.md` 에 정리한다.

### CORS

`CorsConfig` 가 `app.cors.allowed-origins` 출처 패턴을 허용한다.
허용 출처는 `application.yml` 한 곳에서 관리한다. 현재 `localhost:3000`, `localhost:5173`, `https://tongtong-frontend.vercel.app` 을 허용한다.

## 성능 고려사항

### N+1 주의

- 연관관계는 기본 `LAZY`
- 목록 조회는 `fetch join` 또는 `@BatchSize` 적용
- 쿼리 수를 확인하며 개발한다

### 인덱스 설계

- 선택도가 낮은 칼럼에 단독 인덱스를 걸지 않는다
- 복합 인덱스는 등호 조건 칼럼을 앞에, 범위 조건을 뒤에 배치한다
