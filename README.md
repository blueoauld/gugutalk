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

## 주요 기능

### 인증 (Authentication)
- 휴대폰 번호 인증(SMS 인증코드 발송, 검증)
- 회원가입 및 초기 프로필 설정
- 로그인 / 로그아웃
- JWT 기반 토큰 발급 및 토큰 재발급(rotate)
- 회원 탈퇴

### 회원 (Member)
- 내 정보 조회 및 프로필 수정
- 코멘트 수정, 갱신(bump), 채팅 허용 여부 토글
- 회원 상세 조회 / 목록 조회 / 지역별 조회 / 검색
- 공개, 비공개 프로필 이미지 업로드(presigned URL 발급)

### 활동 (Activity)
- 좋아요 / 좋아요 취소, 좋아요 목록, 랭킹 조회
- 싫어요 기능
- 리뷰 작성 / 삭제 / 조회 및 리뷰 랭킹
- 사용자 차단 / 차단 해제 / 차단 목록
- 비공개 이미지 열람 권한 부여 / 회수 / 목록

### 채팅 (Chat)
- 채팅방 생성 / 삭제 / 읽음 처리 / 목록, 검색
- 메시지 전송 및 조회
- 이미지, 영상 미디어 업로드(presigned URL 발급) 및 영상 조회
- WebSocket 기반 실시간 메시징

### 포인트 / 리워드 (Point & Reward)
- 포인트 잔액 및 적립, 사용 내역 조회
- 출석 보상 지급
- 광고 시청 보상 지급
- AdMob SSV(서버 측 보상 검증) 콜백 처리로 어뷰징 방지

### 신고 / 제재 (Report & Ban)
- 사용자 신고 접수 및 신고 이미지 첨부
- 제재(Ban) 생성 / 해제 / 조회 및 제재 이력 관리
- 스케줄러를 통한 제재 만료 자동 처리
- Discord 연동으로 신고, 차단 이벤트 실시간 알림

### 푸시 알림 (Push)
- 디바이스 토큰 등록 / 삭제
- APNs를 통한 iOS 푸시 알림 발송

### 운영 (Operations)
- 만료 데이터 자동 정리 스케줄러(회원, 채팅, 신고, 제재)
- Cloudflare R2 기반 미디어 스토리지 관리
- API 버전 관리(version 기반 라우팅)

## 성능 개선

> **측정 환경**: PostgreSQL 18, member 30만 건(활성 27만 건), page size 20 <br> **측정 방법**: `EXPLAIN (ANALYZE)` 기준 DB 실행 시간(6회 측정 중앙값)

### 1. 회원 목록 페이지네이션: OFFSET > 커서(Keyset) 방식 전환

OFFSET 방식은 뒤 페이지로 갈수록 앞 데이터를 읽고 버리는 비용이 커집니다.
<br>
`(updated_at DESC, id DESC)` 복합 인덱스를 활용한 커서 페이지네이션으로 전환해 조회 위치와 무관하게 일정한 성능을 확보했습니다.

| 조회 위치 | OFFSET 방식 | 커서(Keyset) 방식 | 개선 |
|-----------|------------:|------------------:|------|
| 1페이지          | 0.02 ms | 0.06 ms | - |
| 5,000페이지(100,000행~) | 38 ms   | 0.07 ms | **약 99.8% 단축** |

- OFFSET: 인덱스 항목 100,020개를 스캔 후 100,000개를 버림 (`Index Only Scan`, 깊이에 비례해 악화)
- 커서: 인덱스 범위 조건으로 20건만 조회 (조회 위치와 무관하게 일정)

### 2. 정렬 조회용 부분 인덱스 적용

소프트 삭제(`deleted_at`) 컬럼에 `WHERE deleted_at IS NULL` 부분 인덱스를 적용해
활성 데이터만 인덱싱하고, 풀스캔 정렬을 인덱스 스캔으로 대체했습니다.

| 구분 | 인덱스 없음 | 부분 인덱스 적용 | 개선 |
|------|------------:|-----------------:|------|
| 평균 실행 시간 | 97 ms | 0.06 ms | **약 99.9% 단축** |

- 인덱스 없음: `Parallel Seq Scan`(27만 행) + `top-N heapsort`
- 부분 인덱스: `Index Only Scan` (인덱스 크기 8.3MB)
