# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`eumBeV2` — the backend for **이음(Eum)**, a mentor/mentee mentoring-class platform. Spring Boot (4.1.0), Java 21, Gradle. Package root: `com.example.eumbev2`.

The implementation follows `EumMVPApi.json` (OpenAPI 3.0, in the repo owner's design tool — not committed here) and `erd.txt` (dbdiagram ERD, not committed here). MVP domain scope: 인증(Auth) · 프로필(Profile, folded into `User`) · 클래스(Classroom) · 멤버(Member) · 지원서(Application) · 과제(Assignment) · 제출(Submission) · 공지사항(Notice). Gamification (badges/items/currency), community (posts/reviews/Q&A), and scheduling are explicitly out of scope for this version — do not add them without re-checking the spec.

## Commands

Use the Gradle wrapper (`./gradlew`), not a system-installed Gradle.

- Build: `./gradlew build`
- Run the app: `./gradlew bootRun`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.example.eumbev2.EumBeV2ApplicationTests"`
- Run a single test method: `./gradlew test --tests "com.example.eumbev2.EumBeV2ApplicationTests.contextLoads"`
- Clean build: `./gradlew clean build`

Test reports are written to `build/reports/tests/test/index.html`. Tests run against an in-memory H2 database (`src/test/resources/application.properties`) so `./gradlew test` doesn't require a real MySQL instance; `bootRun`/`build` against a real datastore does.

> This sandbox could not reach `services.gradle.org` / Maven Central, so the Gradle build was never actually executed here — only reviewed statically (package/import resolution, brace balance, accessor-name cross-checks). Run `./gradlew build` locally before trusting this compiles.

## Stack

- Java 21 (via Gradle toolchain)
- Spring Boot starters: `data-jpa`, `security`, `validation`, `webmvc`, `mail`
- `io.jsonwebtoken:jjwt` 0.12.6 (access-token JWTs)
- MySQL (`com.mysql:mysql-connector-j`) at runtime; H2 for tests
- Lombok
- JUnit 5 (`useJUnitPlatform()`)

JSON uses **snake_case** wire format (`spring.jackson.property-naming-strategy=SNAKE_CASE` in `application.properties`) to match the API spec's field names (`access_scope`, `banner_image_url`, `due_date`, …), while Java fields/DTO record components stay camelCase. Don't add `@JsonProperty` overrides for casing — the global strategy already handles it.

## Auth

JWT bearer auth, stateless (`common/security/SecurityConfig`). Access tokens are short-lived signed JWTs (`JwtTokenProvider`); refresh tokens are opaque random strings persisted in `refresh_tokens` (rotated on every `/auth/refresh`, deleted on `/auth/signout`). `JwtAuthenticationFilter` resolves the bearer token, loads the `User`, and sets a `UserPrincipal` on the security context — there's no `AuthenticationManager`/`UserDetailsService` flow; `AuthService.signin` checks the password directly with `PasswordEncoder`.

Public (no token required) endpoints are enumerated explicitly in `SecurityConfig`: signup/signin/refresh, check-username/check-email, email send/verify, password reset request/confirm, `GET /classes/search`, and `GET /classes/{classId:[0-9]+}` (note the numeric-only pattern — it deliberately excludes `/classes/my`). Everything else requires `Authorization: Bearer <token>`. Inside services, get the caller via `SecurityUtils.getCurrentUser()` / `getCurrentUserId()` (throws 401 if unauthenticated) or `currentUserIdOrNull()` for endpoints that behave differently when logged in vs anonymous (e.g. classroom detail's `my_role`).

`EmailVerification`, `PasswordResetToken`, and `RefreshToken` (under `entity/auth`) are not in the ERD — they exist purely to make the email-verification-before-signup and password-reset flows in the API spec actually work. `EmailService` sends real mail via `spring-boot-starter-mail`, but swallows `MailException` (logs a warning) so local/dev environments without real SMTP credentials don't break the underlying flow — the verification code is still persisted either way.

## Domain model vs. ERD

Entities live under `entity/<domain>` with matching `repository/<domain>` and `dto/<domain>` packages, plus `dto/common` for `UserSummaryResponse` (reused wherever the API embeds an author/mentor/mentee). A few deliberate simplifications versus `erd.txt`:

- **User IDs are `Long`** (auto-increment), not the `varchar` the ERD specifies — the API spec's `UserSummary.user_id` and every mentor/author/mentee reference is `int64`, so entity IDs follow the API contract. `username` is still the separate unique human-facing handle used in URLs/login.
- **`classroom_members` absorbs `waiting_list`.** The API models one `Member`/`WaitingMember` concept with a `status` enum (`WAITING`/`ACCEPTED`/`REJECTED`), so `ClassroomMember` is a single table with that status column instead of two ERD tables. A join request creates a `WAITING` row; mentor accept flips it to `ACCEPTED`; the "kick/reject" endpoint (`DELETE /classes/{classId}/members/{userId}`) deletes the row outright rather than keeping a `REJECTED` tombstone. Re-applying after rejection is allowed (old row is deleted, a fresh one is created).
- **`classroom_tag` and `application_question_options`** are `@ElementCollection` string lists (with `@OrderColumn`) on `Classroom`/`ApplicationQuestion` instead of standalone entities — the API never exposes tag/option IDs, only ordered string arrays.
- The classroom mentor also gets an `ACCEPTED`/`MENTOR` row in `classroom_members` at creation time, so membership queries ("my classes", mentee counts, etc.) don't need special-casing for the mentor.
- `Notice` (not `Announcement`) is the entity name, matching the API's `Notice` schema; it maps to the `announcements` table.
- No `Profile` entity/endpoints exist — this MVP spec doesn't include `/profiles/*` (may come later; see `EumMVPApi.json`'s top-level description). `nickname` defaults to `username` at signup since `SignupRequest` doesn't collect one, and there's currently no way to change it.

Deleting a classroom (`ClassroomService.delete`) cascades manually through submissions → assignments → notices → application answers/questions → members before deleting the `Classroom` row itself, since the JPA associations are unidirectional (`@ManyToOne` only, no `cascade = REMOVE`) — if you add a new classroom-scoped entity, add its cleanup there too or deletes will fail on FK constraints.

## Structure

```
src/main/java/com/example/eumbev2/
  EumBeV2Application.java
  common/
    exception/   - ErrorCode (enum: HTTP status + default message), ApiException, GlobalExceptionHandler
    response/    - ErrorResponse (spec's `Error` schema), PageMetaResponse, PageResponse<T> (spec's `*Page` schemas)
    security/    - JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, UserPrincipal, SecurityUtils
    util/        - CodeGenerator (verification codes, opaque tokens, invite codes)
  entity/<domain>/     - JPA entities (auth, user, classroom, application, assignment, submission, notice)
  repository/<domain>/ - Spring Data JPA repositories, mirrors entity packages
  dto/<domain>/        - request/response records, mirrors entity packages, plus dto/common
  service/<domain>/    - business logic; ClassroomService exposes getOrThrow/requireMentor/menteeCount
                         helpers reused by Member/Application/Assignment/Submission/Notice services
  controller/          - one controller per domain, paths matching the OpenAPI spec exactly
```

Layer flow: `controller` binds HTTP (path/query params, `@Valid` request bodies) and delegates to `service`; `service` holds business rules (ownership checks via `requireMentor`, membership checks, cascades) and talks to `repository`; `dto` records shape the wire format (snake_case) separately from `entity` (camelCase, JPA-mapped). All list endpoints return `PageResponse<T>` (`{ items, page }`) built via `PageResponse.from(Page<E>, Function<E,T>)`.

Error handling is centralized: throw `new ApiException(ErrorCode.XXX)` (or the two-arg constructor for a custom message) from any service method; `GlobalExceptionHandler` renders it as the spec's `Error` JSON with the right HTTP status. Add new failure cases as `ErrorCode` entries rather than throwing ad-hoc exceptions.
