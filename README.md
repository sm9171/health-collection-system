# Health Data Collection System

건강 활동 데이터(걸음수, 칼로리, 이동거리)를 수집하고 관리하는 마이크로서비스 기반 백엔드 시스템

## 프로젝트 개요

삼성헬스, 애플건강 등의 앱에서 수집된 사용자 건강 데이터를 서버로 전송하여 저장, 조회, 분석하는 서비스의 백엔드 시스템입니다.

## 아키텍처

- **마이크로서비스 아키텍처**: 독립적으로 배포 가능한 5개의 서비스
- **DDD (Domain-Driven Design)**: 전략적 설계를 통한 도메인 모델링
- **Hexagonal Architecture**: 비즈니스 로직과 외부 의존성 분리
- **Event-Driven Architecture**: 확장 가능한 비동기 처리 지원

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.4.6
- Spring Cloud 2024.0.0 (Gateway, Config, Eureka)
- Spring Data JPA
- Spring Security + JWT (JJWT 0.12.6)

### Database & Cache
- H2 Database (개발환경)
- MySQL 8.0 (운영환경)
- Flyway (DB Migration)

### Infrastructure
- Docker & Docker Compose
- Gradle Multi-Module

## 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| config-server | 8888 | 중앙 설정 관리 |
| discovery-server | 8761 | 서비스 디스커버리 (Eureka) |
| gateway-server | 8080 | API Gateway & JWT 인증 |
| user-service | 8081 | 회원가입/로그인 |
| health-data-service | 8082 | 건강 데이터 수집/조회 |

## 시작하기

### 사전 요구사항

- JDK 17 이상
- Docker & Docker Compose (선택사항)
- Gradle 8.x

### 실행 방법

#### 1. 자동 실행 (권장)

```bash
# 모든 서비스 자동 시작
./infra/start-services.sh

# 모든 서비스 중지
./infra/stop-services.sh
```

#### 2. 수동 실행

```bash
# 1. 프로젝트 빌드
./gradlew clean bootJar -x test

# 2. 서비스 실행 (순서 중요)
java -jar config-server/build/libs/config-server-0.0.1-SNAPSHOT.jar
java -jar discovery-server/build/libs/discovery-server-0.0.1-SNAPSHOT.jar
java -jar gateway-server/build/libs/gateway-server-0.0.1-SNAPSHOT.jar
java -jar user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar
java -jar health-data-service/build/libs/health-data-service-0.0.1-SNAPSHOT.jar
```

#### 3. Docker로 인프라 실행 (선택사항)

```bash
docker-compose -f infra/docker/docker-compose.yml up -d
```

## API 사용 예시

### 1. 회원가입

```bash
curl -X POST http://localhost:8080/api/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "nickname": "gildong",
    "email": "hong@example.com",
    "password": "Password123!"
  }'
```

### 2. 로그인

```bash
curl -X POST http://localhost:8080/api/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hong@example.com",
    "password": "Password123!"
  }'
```

### 3. 건강 데이터 수집

```bash
curl -X POST http://localhost:8080/api/health/health-data/collect \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
    "data": {
      "entries": [
        {
          "period": {
            "from": "2024-11-15 00:00:00",
            "to": "2024-11-15 00:10:00"
          },
          "distance": {
            "unit": "km",
            "value": 0.04223
          },
          "calories": {
            "unit": "kcal",
            "value": 2.03
          },
          "steps": 54
        }
      ]
    }
  }'
```

### 4. 건강 데이터 조회

```bash
curl -X GET "http://localhost:8080/api/health/health-data?recordKey=7836887b-b12a-440f-af0f-851546504b13&from=2024-11-15T00:00:00&to=2024-11-15T23:59:59" \
  -H "Authorization: Bearer {accessToken}"
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :user-service:test

# 빌드 (테스트 제외)
./gradlew clean build -x test
```

## 모니터링

### 서비스 상태 확인

- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888/actuator/health
- **Gateway Health**: http://localhost:8080/actuator/health

### API 문서

- **User Service API**: http://localhost:8081/swagger-ui.html
- **Health Data Service API**: http://localhost:8082/swagger-ui.html

### 메트릭스

- **Prometheus Metrics**: `http://localhost:808{1,2}/actuator/prometheus`
- **Application Metrics**: `http://localhost:808{1,2}/actuator/metrics`

## 개발 가이드

### 코드 구조

각 서비스는 Hexagonal Architecture를 따릅니다:

```
src/main/java/com/healthdata/{service}/
├── adapter/
│   ├── in/web/          # REST Controllers
│   └── out/persistence/ # JPA Repositories
├── application/
│   ├── port/
│   │   ├── in/          # Use Case Interfaces
│   │   └── out/         # Repository Interfaces
│   └── service/         # Use Case Implementations
├── domain/
│   ├── model/           # Domain Models
│   ├── vo/              # Value Objects
│   └── event/           # Domain Events
└── config/              # Configuration Classes
```

### 데이터베이스 마이그레이션

Flyway를 사용하여 데이터베이스 스키마를 관리합니다:

```bash
./gradlew flywayMigrate
```

## 트러블슈팅

### 일반적인 문제

1. **포트 충돌**: 8080-8082, 8761, 8888 포트가 사용 중인지 확인
2. **서비스 시작 순서**: Config → Discovery → Gateway → User → Health Data 순서로 시작
3. **메모리 부족**: JVM 힙 메모리 설정 조정 (`-Xmx512m`)

### 로그 확인

```bash
# 실시간 로그 확인
tail -f logs/{service-name}.log

# 에러 로그 검색
grep -i error logs/*.log
```

## 라이센스

MIT License

## 기여자

- Generated with Claude Code
- Co-Authored-By: Claude <noreply@anthropic.com>