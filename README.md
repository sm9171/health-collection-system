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
- Spring Kafka & Kafka Streams
- Spring Batch

### Database & Cache
- H2 Database (개발환경)
- MySQL 8.0 (운영환경)
- Redis 7.x (캐싱 및 실시간 집계)
- Flyway (DB Migration)

### Message Queue
- Apache Kafka 7.5.0
- Zookeeper

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

---

## Health Data Service 하이브리드 처리 전략

### 데이터 수집 및 처리 아키텍처

```
┌────────────────────────────────────────┐
│      POST /health-data/collect         │
└────────┬───────────────────────────────┘
         │
         ├──→ MySQL (health_data)
         │    - 원본 데이터 영구 보관
         │
         ├──→ Kafka Topic (health-data-collected)
         │    └──→ Kafka Streams (10분 윈도우)
         │         └──→ Redis Hot Data (24시간 TTL)
         │              - 실시간 집계
         │
         └──→ Batch Job (매일 새벽 2시)
              └──→ MySQL (health_data_summary)
                   └──→ Redis Warm Data (7일 TTL)
                        - 일별 요약 캐싱
```

### 데이터 계층별 역할

#### 1. MySQL 원본 데이터 (`health_data`)
- **용도**: 모든 건강 데이터의 원본 저장
- **특징**: 영구 보관, 정확한 데이터
- **조회**: 상세 데이터 필요 시

#### 2. Kafka Streams 실시간 집계
- **토픽**: `health-data-collected`
- **윈도우**: 10분 타임 윈도우
- **집계 단위**: recordKey + 날짜별
- **출력**: Redis Hot Data

#### 3. Redis Hot Data
- **Key 패턴**: `health:hot:{recordKey}:{date}`
- **TTL**: 24시간
- **용도**: 당일 실시간 집계 데이터
- **갱신 주기**: Kafka Streams가 10분마다 업데이트

#### 4. MySQL Summary (`health_data_summary`)
- **생성 시점**: 매일 새벽 2시 배치 작업
- **용도**: 일별 집계 데이터 영구 보관
- **데이터**: 전날 하루치 집계 (총 걸음수, 칼로리, 거리, 엔트리 수)

#### 5. Redis Warm Data
- **Key 패턴**: `health:warm:{recordKey}:{date}`
- **TTL**: 7일
- **용도**: 배치로 생성된 요약 데이터 캐싱
- **갱신**: 배치 작업 실행 시

### 주요 컴포넌트

**Kafka 관련:**
- `HealthDataEventPublisher` - 데이터 수집 시 Kafka 이벤트 발행
- `HealthDataStreamsProcessor` - 실시간 집계 및 Redis Hot Data 업데이트
- `KafkaProducerConfig` - Kafka Producer 설정
- `KafkaStreamsConfig` - Kafka Streams 설정

**배치 관련:**
- `DailyHealthDataAggregationJob` - 일별 집계 배치 작업
- `BatchScheduler` - 스케줄러 (매일 새벽 2시 실행, Cron: `0 0 2 * * *`)

**캐시 관련:**
- `HealthDataCacheRepository` - Redis Hot/Warm Data 조회 및 저장
- `RedisConfig` - Redis 연결 및 직렬화 설정

### 성능 최적화 전략

#### 실시간 조회 (당일 데이터)
1. Redis Hot Data 우선 조회
2. Cache Miss 시 MySQL 원본 조회

#### 과거 데이터 조회
1. Redis Warm Data 조회
2. Cache Miss 시 MySQL Summary 조회
3. Summary 없을 시 원본 데이터 조회

#### 상세 데이터 조회
- MySQL 원본 데이터 직접 조회

### Kafka & Redis 모니터링

#### Kafka Topics 확인
```bash
docker exec -it health-kafka kafka-topics --list --bootstrap-server localhost:9092

# Consumer로 이벤트 확인
docker exec -it health-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic health-data-collected \
  --from-beginning
```

#### Redis 데이터 확인
```bash
docker exec -it health-redis redis-cli

# Hot Data 조회
GET health:hot:{recordKey}:2024-11-15

# Warm Data 조회
GET health:warm:{recordKey}:2024-11-15

# 모든 건강 데이터 키 조회
KEYS health:*
```

#### MySQL Summary 확인
```bash
docker exec -it health-mysql mysql -uroot -proot healthdb

# 요약 데이터 조회
SELECT * FROM health_data_summary
WHERE record_key = '{recordKey}'
ORDER BY summary_date DESC;
```

### 배치 작업 관리

#### 배치 스케줄 변경
```java
// BatchScheduler.java
@Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
public void runDailyAggregation()
```

#### 수동 배치 실행 (테스트용)
```java
// BatchScheduler.java에서 활성화
@Scheduled(fixedDelay = 60000)  // 1분마다
public void runTestAggregation()
```

---

## 라이센스

MIT License

## 기여자

- Generated with Claude Code
- Co-Authored-By: Claude <noreply@anthropic.com>