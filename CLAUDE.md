# CLAUDE.md

## 프로젝트 개요

통통(멋쟁이사자처럼 팀 프로젝트)의 백엔드 서버.

<!-- TODO: 서비스 한 줄 소개, 핵심 기능, 범위 밖 기능을 적는다 -->

## 기술 스택

- Java 21 / Spring Boot 4.1.2
- Spring Data JPA
- DB: MySQL 8
- springdoc-openapi (Swagger)
- 패키지 루트: `com.likelion.tongtong`

## 아키텍처

계층형 아키텍처 (Controller - Service - Repository).
패키지는 도메인별로 묶는다.

상세 구조는 @docs/ARCHITECTURE.md 참조.

---

## 깃 컨벤션

### 브랜치 전략

Git Flow 기반.

| 브랜치 | 역할 | 분기 | 머지 |
|---|---|---|---|
| `main` | 운영 배포 | - | - |
| `develop` | 개발 통합 | `main` | `main` |
| `feat/*` | 기능 개발 | `develop` | `develop` |
| `release/*` | 배포 준비 | `develop` | `main`, `develop` |
| `hotfix/*` | 긴급 수정 | `main` | `main`, `develop` |

**`feat` 브랜치를 `main`에 직접 머지하지 않는다.**

### 브랜치 네이밍

feat/#{이슈번호}/{기능명}
hotfix/#{이슈번호}/{버그명}
release/{버전}

예시
- `feat/#12/kakao-login`
- `feat/#34/product-search`
- `hotfix/#56/payment-timeout`
- `release/1.0.0`

금지
- `feature/login` (접두사 틀림, 이슈 번호 없음)
- `Feat/Login` (대문자 사용)

### 커밋 메시지

Angular 컨벤션 기반. **싱글 라인으로 작성한다.**

<type>(<scope>): <subject> (#이슈번호)

**Type**

| Type | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `refactor` | 코드 리팩토링 |
| `perf` | 성능 개선 |
| `test` | 테스트 코드 추가·수정 |
| `build` | 빌드 시스템·외부 의존성 변경 |
| `ci` | CI 설정 변경 |
| `chore` | 기타 변경사항 (소스·테스트 변경 없음) |
| `revert` | 이전 커밋 되돌리기 |

**Scope**

| Scope | 설명 |
|---|---|
| `user` | 사용자 |
| `common` | 공통 모듈 |
| `config` | 설정 |
| `deps` | 의존성 |

<!-- 도메인이 추가되면 scope도 함께 추가한다 -->

**작성 규칙**

- subject는 50자 이내
- 마침표를 붙이지 않는다
- 명령문 형식을 사용한다 ("추가함" ❌ → "추가" ⭕)
- 상세 설명이 필요하면 PR 본문에 작성한다
- `Co-Authored-By` 등 트레일러를 붙이지 않는다

**좋은 예시**

feat(user): 회원가입 API 구현 (#12)
fix(user): 닉네임 중복 검사 오류 수정 (#34)
refactor(user): UserService 검증 로직 분리 (#56)
chore: .gitignore 업데이트 (#78)

**나쁜 예시**

수정함
Update code
feat: 작업
버그수정.
FIX: BUG FIX!!!
feat(user): 회원 목록 조회 API 구현 (페이징, 정렬, 필터링 지원) (#12)

마지막 예시는 50자를 초과하므로 상세 내용을 PR 본문으로 옮긴다.

### 이슈 / PR

- 이슈 제목: `[Type] 작업 내용` (예: `[Feat] 회원가입 API`)
- PR 제목: `[Type] 작업 내용 (#이슈번호)` (커밋 메시지 형식과 다르다)
- PR 템플릿의 모든 항목을 작성하고 본문에 `closes #이슈번호` 를 적는다
- CI 통과 후 머지한다

---

## 코드 컨벤션

### 네이밍

- Controller: `{Domain}Controller`
- Service: `{Domain}Service`
- Repository: `{Domain}Repository`
- Request DTO: `{Action}{Domain}Request`
- Response DTO: `{Domain}Response`

### 의존성 주입

- 생성자 주입을 사용하고 주입 필드는 `private final` 로 선언한다
- `@RequiredArgsConstructor` 사용을 허용한다
- 필드 주입(`@Autowired` 필드)을 사용하지 않는다

### API

- URL은 복수형 명사 + 케밥케이스 (`/api/v1/users`)
- 응답은 반드시 `ApiResponse<T>` 로 감싼다
- 예외는 `BusinessException` 을 던지고 `ErrorCode` 를 사용한다

### DTO

- `record` 를 사용한다
- Entity를 Controller 밖으로 노출하지 않는다
- 변환은 DTO 내부 `from()` 정적 팩토리 메서드로 처리한다
- 입력 검증은 요청 DTO에 Bean Validation(`@NotNull`, `@Size` 등)으로 선언하고 Controller에서 `@Valid` 로 트리거한다

### Entity

- `@Setter` 를 사용하지 않는다
- 모든 Entity는 `BaseTimeEntity` 를 상속한다
- 기본 생성자는 `@NoArgsConstructor(access = PROTECTED)` 로 제한한다

### 테스트

- 테스트명은 한국어로 무엇을 검증하는지 서술한다 (메서드명 또는 `@DisplayName`)
- given-when-then 구조를 따른다
- 테스트 패키지는 main 패키지 구조를 그대로 따른다
- 스프링 컨텍스트를 띄우는 테스트에는 `@ActiveProfiles("test")` 를 붙인다

---

## Claude 스킬

`.claude/skills/` 에 작업별 스킬이 있다. 작업 전에 호출하고 끝난 뒤 체크리스트로 자가 검증한다.

| 스킬 | 사용 시점 |
|---|---|
| `/conventions` | 코드 작성·리뷰 |
| `/git` | 커밋·브랜치·이슈·PR 작성 |

---

## 작업 규칙

- 한 브랜치에서는 하나의 기능만 개발한다
- 한 PR은 하나의 기능만 다룬다
- 작업 시작 전 `git pull` 을 실행한다
- 머지된 브랜치는 즉시 삭제한다
- 팀 전체에 영향을 주는 결정은 `docs/adr/` 에 ADR로 남긴다

## 하지 말 것

- Controller에 비즈니스 로직 작성
- `application.yml` 에 비밀값 하드코딩 (환경변수 사용)
- 기획 범위 밖 기능의 임의 추가
- 테스트 없이 대규모 리팩토링
- `feat` 브랜치에서 `main` 으로 직접 머지
