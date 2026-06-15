# 구구톡

<p>
  <img width="200" alt="1" src="https://github.com/user-attachments/assets/5bdb349a-989c-4c76-8d42-1b402f1e8a82" />
  <img width="200" alt="2" src="https://github.com/user-attachments/assets/ca4f43ee-7202-4b69-b2c3-4414dc587267" />
  <img width="200" alt="3" src="https://github.com/user-attachments/assets/21497c40-fc57-4b86-8d3f-b952b561b7e0" />
  <img width="200" alt="4" src="https://github.com/user-attachments/assets/3e40490a-e9b1-4584-8cd7-03a464b66625" />
</p>

## 아키텍처

<img width="1513" height="1039" alt="image" src="https://github.com/user-attachments/assets/c7d840f3-6c15-44c0-96d4-c5912cdc2a44" />

## 기술 스택

### Application

| 분류 | 기술 | 선택 이유 |
|------|------|-----------|
| 언어 | Kotlin 2.2 (JVM 21) | 널 안정성과 간결한 문법으로 보일러플레이트를 줄이고 런타임 안정성 확보 |
| 프레임워크 | Spring Boot 4.0 | 검증된 생태계와 풍부한 스타터로 빠르고 안정적인 서버 개발 |
| 영속성 | Spring Data JPA | 표준 ORM으로 데이터 접근 계층을 추상화하고 생산성 향상 |
| 쿼리 | Kotlin JDSL | 타입 안전한 동적 쿼리 작성 |
| DB | PostgreSQL | 안정성과 확장성을 갖춘 오픈소스 RDBMS |
| 마이그레이션 | Flyway | DB 스키마 버전 관리 및 마이그레이션 자동화 |
| 캐시 | Redis | 캐싱, 세션 데이터를 빠르게 처리 |
| 실시간 통신 | WebSocket | 채팅 등 양방향 실시간 통신 구현 |
| 인증 | JWT | 무상태(stateless) 토큰 기반 인증으로 확장성 확보 |
| 보안 | Bcrypt | 비밀번호 단방향 해싱 등 안전한 암호화 처리 |
| 비동기 | Kotlin Coroutines | 경량 비동기 처리로 효율적인 동시성 제어 |
| 푸시 알림 | Pushy | iOS APNs 푸시 알림 발송 |
| 문자 인증 | SolAPI | SMS 인증 번호 발송 |
| 외부 연동 | JDA | Discord 연동 (신고, 정지 알림) |
| 광고 검증 | Google Tink | 리워드 광고 보상의 무결성 검증 |

### Infra & Ops

| 분류 | 기술 | 선택 이유 |
|------|------|-----------|
| 리버스 프록시 | Caddy | 자동 HTTPS(인증서 발급, 갱신)와 간결한 설정으로 운영 부담 최소화 |
| 스토리지 | Cloudflare R2 | S3 호환 객체 스토리지이면서 egress(전송) 비용이 없어 이미지 서빙 비용 절감 |
| 로그 수집 | Fluent Bit | 경량 로그 수집기로 컨테이너 로그를 수집, 가공해 전달 |
| 로그 포맷 | Logstash Logback Encoder | JSON 구조화 로깅으로 로그 수집, 분석 연동 |
| 메트릭 노출 | Micrometer + Actuator | 애플리케이션 상태, 메트릭을 Prometheus 포맷으로 노출 |
| 텔레메트리 수집 | Grafana Alloy | 메트릭, 로그를 한 번에 수집해 Grafana Cloud로 전송하는 통합 에이전트 |
| 모니터링 | Grafana Cloud (Prometheus) | 메트릭 저장, 시각화, 알림을 관리형으로 운영해 인프라 부담 절감 |
| 컨테이너 | Docker Compose | App, Redis, Proxy, 모니터링을 선언적으로 정의해 일관된 배포 환경 구성 |
