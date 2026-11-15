# tongtong-server

통통 백엔드 서버

## 기술 스택

- Java 21 / Spring Boot 4.1.2
- Spring Data JPA / MySQL 8
- springdoc-openapi (Swagger)
- GitHub Actions (CI/CD), Docker, AWS EC2

## 로컬 실행

환경변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 를 설정한 뒤 실행한다.

```bash
./gradlew bootRun
```

Swagger: http://localhost:8080/swagger-ui/index.html

## 문서

- 컨벤션: [CLAUDE.md](CLAUDE.md)
- 아키텍처: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
