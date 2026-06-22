# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

This is the **backend server** for 구구톡 (GuguTalk), an iOS dating/chat app already live on the App Store. It sits in the
`server/` subdirectory of a monorepo alongside `app/` (SwiftUI iOS client) and `web/`. Treat it as a production system —
keep changes scoped to what's asked.

Kotlin 2.2 (JVM 21) / Spring Boot 4.0 / Spring Data JPA + Kotlin JDSL / PostgreSQL / Redis. Korean is the primary
language for error messages, commit messages, and domain terminology.

## Commands

```bash
./gradlew bootRun                              # run locally (uses 'local' profile by default)
./gradlew build                                # compile + test + assemble
./gradlew test                                 # run all tests
./gradlew test --tests "com.blueoauld.server.ServerApplicationTests"   # single test class
./gradlew bootJar -x test                      # build runnable jar (what the Dockerfile does)
```

Running locally requires a reachable **PostgreSQL** and **Redis** (see `application-local.yml`). `ddl-auto: validate`
means Hibernate does **not** create tables — schema is owned by Flyway migrations in
`src/main/resources/db/migration/` (`V{n}__name.sql`). Add a new versioned migration for any schema change rather than
relying on JPA.

Deployment is image-based: `docker compose pull app && docker compose up -d` on the server (see `deploy.sh`,
`compose.yml`, `Caddyfile`).

## Architecture

### Package-by-feature with a layered structure

Each domain under `com.blueoauld.server.<domain>` (`member`, `chat`, `activity`, `point`, `report`, `ban`, `push`,
`authentication`, `discord`, `r2`, `admin`) follows the same internal layering:

```
presentation/   # @RestController — thin, delegates to application
application/     # @Service business logic; also request/, response/, event/, scheduler/ subpackages
repository/      # Spring Data interfaces + impl/ (custom JDSL) + result/ (projection DTOs)
entity/          # JPA entities + entity/type/ (enums)
```

`common/` holds cross-cutting concerns: `configuration/` (one `@Configuration` per integration — R2, APNs, JDA/Discord,
WebSocket, scheduler, JDSL, async), `authentication/`, `exception/`, `filter/`, `properties/`, `util/`, `dto/`.

When adding a feature, mirror this layout exactly — controllers stay thin, logic lives in `application/`, and
request/response DTOs go in `application/request` and `application/response`.

### Authentication flow

There is **no Spring Security filter chain**; auth is hand-rolled:

- `common/authentication/filter/AuthenticationFilter` runs per-request. It requires an `X-Device-Id` header, checks
  device/account/phone bans via `BanRepository`, validates the `Bearer` JWT (`TokenProvider`), checks the Redis
  access-token blacklist, loads the member, then stashes the member id as a request attribute + MDC. A `whitelist` (
  verify/signup/login/token-rotate) skips token checks; an `exclude` list (`/ws/**`, `/actuator/**`, `/admob-ssv`,
  `DELETE /api/push`) skips the filter entirely.
- Controllers receive the authenticated id via `@Login memberId: Long`, resolved by
  `AuthenticationPrincipalArgumentResolver` (registered in `WebConfiguration`). Use this rather than re-parsing tokens.
- WebSocket/STOMP auth goes through `StompChannelInterceptor` instead of the servlet filter.

### Error handling

Throw `CustomException(ErrorCode.XXX)`. `ErrorCode` (in `common/exception/type/`) is a single enum mapping a stable
string code (e.g. `MEMBER_06`, `ACTIVITY_11`) to an `HttpStatus` and a Korean message; `GlobalExceptionHandler` renders
it. Add new cases to that enum following the `DOMAIN_NN` naming — the iOS client keys off these codes, so don't renumber
existing ones.

### API versioning

Versioning is header-based via `Api-Version` (configured in `WebConfiguration`, default `"1"`, supported `"1"`/`"2"`).
Every mapping declares it explicitly, e.g. `@GetMapping("/members", version = "1")`. New endpoints should set `version`
too.

### Pagination

List endpoints use **cursor (keyset) pagination**, not OFFSET — e.g. `(cursorId, cursorDateAt)` params returning
`CursorResponse`, backed by `(updated_at DESC, id DESC)` composite + partial (`WHERE deleted_at IS NULL`) indexes.
Follow this pattern for new list queries; entities are soft-deleted via `deleted_at`.

### Realtime, async, scheduling

- **WebSocket/STOMP** (`WebSocketConfiguration`): endpoint `/ws`, simple broker on `/topic` + `/queue`, app prefix
  `/app`, user prefix `/user`. Used for live chat.
- **Coroutines + `@Async`** (`AsyncConfig`) for non-blocking work like push fan-out.
- **Schedulers** (`@Scheduled`, `SchedulerConfiguration`) live in each domain's `application/scheduler/` — they expire
  bans and clean up stale member/chat/report data.

### External integrations (each behind a `common/configuration` bean)

R2 (Cloudflare object storage via AWS S3 SDK — media served by issuing presigned URLs, never proxied through the app),
Pushy (APNs), SolAPI (SMS verification codes), JDA (Discord notifications for reports/bans), Google Tink (
`apps-rewardedads`, AdMob SSV reward verification at `/admob-ssv`).

## Testing

Stack: **JUnit5** (runner) + **MockK** (mocking) + **AssertJ** (assertions) + **Testcontainers** (real Postgres +
Redis). Do NOT use Mockito or H2 — Kotlin final classes and Postgres-specific SQL (Flyway migrations, partial indexes,
JDSL keyset queries) make them a poor fit.

### Test layers

- **Unit** — `application/` service logic in isolation. Mock collaborators with MockK (`mockk()`,
  `every {} returns ...`, `coEvery` for suspend funcs, `verify`). No Spring context — fast.
- **Web slice** — `@WebMvcTest(XxxController::class)` + MockMvc. Replace the service with a MockK test bean. The
  hand-rolled auth needs explicit setup — see "Testing authenticated endpoints".
- **Persistence slice** — `@DataJpaTest` for repository/JDSL queries, run against Testcontainers Postgres (never H2).
  Annotate `@AutoConfigureTestDatabase(replace = NONE)`.
- **Integration** — `@SpringBootTest` for flows crossing WebSocket/STOMP, schedulers, or multiple domains. Reuse the
  shared container setup.

### Testing authenticated endpoints

Auth is hand-rolled (no Spring Security), so `@WebMvcTest` does not wire `AuthenticationFilter` /
`AuthenticationPrincipalArgumentResolver` like prod.

- Provide a `@TestConfiguration` that registers a stub argument resolver resolving `@Login` to a fixed `memberId` (e.g.
  `1L`), so controller tests need no real JWT or `X-Device-Id`. Expose a `withLogin(memberId)` MockMvc helper.
- For full integration tests that must exercise the real filter, mint a token via `TokenProvider` and send
  `Authorization: Bearer ...` + `X-Device-Id`.
- Never re-implement JWT parsing in tests; always go through `TokenProvider`.
- Put shared test support under `src/test/.../support/`.

### Integration infrastructure

- A single abstract `IntegrationTestSupport` base class starts ONE Postgres + ONE Redis container (`companion object`,
  reused across the suite — not per-test) and wires them via `@DynamicPropertySource`.
- Flyway runs against the container, so every integration test also validates that migrations apply cleanly on the real
  schema. This is intentional — keep it.
- Keep tests independent: `@Transactional` rollback or explicit cleanup between tests.

### Conventions for tests

- Backtick behavior-describing names, Korean OK: `` fun `밴된 디바이스는 요청이 차단된다`() ``. Group scenarios with `@Nested`.
- Build test entities via fixture/builder functions under `src/test/.../fixture/`, not inline.
- Assert failures by `ErrorCode`, never by message string — codes are the stable contract the iOS client depends on;
  Korean messages may change.

### Must-cover behaviors (regression-critical)

- Cursor pagination: ordering by `(updated_at DESC, id DESC)`; the next cursor excludes the boundary row.
- Soft delete: `deleted_at IS NULL` filtering — deleted rows never leak into list/detail queries.
- Auth/ban: blacklisted access token rejected; device/account/phone bans block requests.
- Error mapping: each thrown `CustomException` renders the correct `HttpStatus` + `ErrorCode`.

### Test commands

```bash
./gradlew test                                           # all tests (starts containers)
./gradlew test --tests "com.blueoauld.server.member.*"   # one domain
```

## Conventions

- Match existing style: constructor injection with a blank line after the class paren, `KotlinLogging.logger {}` for
  logging, `Instant` for timestamps.
- Config is profile-based (`application-{local,prod}.yml`); the active profile defaults to `local`. Note that
  `application-local.yml` currently contains real dev credentials committed to the repo — don't add production secrets
  to source.