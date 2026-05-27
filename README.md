# Discodeit

[![CI](https://github.com/Fast-Normal/11-sprint-mission/actions/workflows/test.yml/badge.svg)](https://github.com/Fast-Normal/11-sprint-mission/actions/workflows/test.yml)
[![codecov](https://codecov.io/gh/Fast-Normal/11-sprint-mission/branch/main/graph/badge.svg)](https://codecov.io/gh/Fast-Normal/11-sprint-mission)

Discord를 모티브로 한 채팅 서비스 백엔드 API입니다.

---

## 기술 스택

| 분류            | 기술                             |
|---------------|--------------------------------|
| Language      | Java 17                        |
| Framework     | Spring Boot 3.5.7              |
| ORM           | Spring Data JPA / Hibernate    |
| Database      | PostgreSQL (운영) / H2 (테스트)     |
| Build         | Gradle                         |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Monitoring    | Spring Boot Actuator           |
| Test Coverage | JaCoCo + CodeCov               |

---

## 주요 기능

- **User** — 회원가입, 프로필 이미지 업로드, 정보 수정/삭제
- **Channel** — PUBLIC / PRIVATE 채널 생성, 조회, 수정, 삭제
- **Message** — 메시지 전송(첨부파일 포함), 커서 기반 페이지네이션 조회, 수정, 삭제
- **ReadStatus** — 채널별 마지막 읽은 시간 관리
- **UserStatus** — 유저 온라인 상태 관리
- **BinaryContent** — 프로필, 첨부파일 이미지 데이터 관리

---

## 시작하기

### 요구 사항

- Java 17
- Docker & Docker Compose

### 실행

```bash
# 저장소 클론
git clone https://github.com/Fast-Normal/11-sprint-mission.git
cd 11-sprint-mission

# 환경변수 설정 (.env 파일 생성)
cp .env.example .env

# Docker로 DB 실행
docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

서버가 실행되면 아래 주소에서 확인할 수 있습니다.

- API 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/docs

---

## 테스트

```bash
# 테스트 실행 + JaCoCo 커버리지 리포트 자동 생성
./gradlew test
```

리포트는 `build/reports/jacoco/test/html/index.html` 에서 확인할 수 있습니다.

---

## API 문서

Swagger UI: `http://localhost:8080/docs`

---

## 환경 변수

운영 환경(`prod` 프로파일)에서는 아래 환경변수가 필요합니다.

| 변수명                          | 설명                  |
|------------------------------|---------------------|
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자명             |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호             |