# Account and Friends MVP Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: activate it by name and follow its Execute flow and Critical Rules. Each task is implemented, tested, gated, marked complete, and committed before the next task starts.

**Design**: `.specs/features/account-and-friends-mvp/design.md`
**Status**: In Progress

## Test Coverage Matrix

> Generated from the approved spec and the user's selected test strategy. Guidelines found: none - strong defaults applied.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain and application services | unit | All branches; one assertion path for every mapped acceptance criterion and listed edge case | `src/test/java/**/*Test.java` | `mvn -q test` |
| CSV repositories | integration | Key read/write paths, malformed data, atomic failure behavior, and preservation of previous state | `src/test/java/**/*IntegrationTest.java` | `mvn -q test` |
| REST controllers and security | integration | Every endpoint: happy path, authentication boundary, validation errors, and state-preservation failures | `src/test/java/**/*IntegrationTest.java` | `mvn -q test` |
| Entity and configuration | none | Build gate only | `src/main/java/**` | `mvn -q verify` |

## Gate Check Commands

> Generated from the selected Maven test strategy. The project has no existing lint or formatter command; `verify` is the build gate.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | After unit-test tasks | `mvn -q test` |
| Full | After integration-test tasks | `mvn -q test` |
| Build | After phase completion or config/entity-only tasks | `mvn -q verify` |

## Execution Plan

Phases are ordered and run sequentially. Tasks within a phase execute in order.

### Phase 1: Foundation

```text
T1 -> T2 -> T3 -> T4 -> T5
```

### Phase 2: Persistence

```text
T6 -> T7
```

### Phase 3: Authentication

```text
T8 -> T9
```

### Phase 4: Profile and Discovery

```text
T10 -> T11 -> T12
```

### Phase 5: Friendships and Acceptance

```text
T13 -> T14
```

## Task Breakdown

### Phase 1: Foundation Tasks

### T1: Create Maven build

**What**: Create the Spring Boot Maven build with web, validation, security, CSV, token, and test dependencies.
**Where**: `pom.xml`
**Depends on**: None
**Requirement**: AUTH-01

**Done when**:

- [ ] `pom.xml` defines the approved Java/Spring build and test dependencies.
- [ ] `mvn -q verify` completes successfully with no application source required beyond the generated skeleton.

**Tests**: none
**Gate**: build

**Status**: Done - `mvn -q verify` passed.

### T2: Create application bootstrap

**What**: Create the Spring Boot entry point and typed application configuration for the CSV data directory.
**Where**: `src/main/java/com/ftabah/giftme/GiftMeApplication.java`
**Depends on**: T1
**Requirement**: AUTH-01

**Done when**:

- [ ] The application starts with the configured data directory property.
- [ ] A context-load test proves the Spring application context starts.

**Tests**: integration
**Gate**: full

**Status**: Done - `mvn -q test` passed with JDK 21.

### T3: Implement account and profile domain models

**What**: Implement validated account, profile, custom-size, and photo domain types.
**Where**: `src/main/java/com/ftabah/giftme/domain/`
**Depends on**: T2
**Requirement**: AUTH-06, PROFILE-01, PROFILE-03, PROFILE-04, PROFILE-05, PROFILE-06, PROFILE-07, PROFILE-08

**Done when**:

- [ ] Domain tests cover age bounds, blank fields, duplicate custom names, PNG size, and photo media type.
- [ ] Domain tests prove invalid values are rejected without mutating the previous valid profile.

**Tests**: unit
**Gate**: quick

### T4: Implement friendship domain state

**What**: Implement friendship request status and recipient-only transition rules.
**Where**: `src/main/java/com/ftabah/giftme/domain/FriendshipRequest.java`
**Depends on**: T3
**Requirement**: FRIEND-01, FRIEND-02, FRIEND-03, FRIEND-04, FRIEND-05

**Done when**:

- [ ] Unit tests cover pending, accepted, rejected, self-request, duplicate, and unauthorized decision outcomes.
- [ ] Invalid transitions preserve the existing request status.

**Tests**: unit
**Gate**: quick

### T5: Define application ports

**What**: Define repository, password, token, and transaction-facing ports used by application services.
**Where**: `src/main/java/com/ftabah/giftme/application/port/`
**Depends on**: T4
**Requirement**: AUTH-01, PROFILE-01, SEARCH-01, FRIEND-01

**Done when**:

- [ ] Ports express every read/write operation required by the approved design.
- [ ] Port contracts do not depend on CSV classes or web DTOs.
- [ ] The build passes with the port interfaces compiled.

**Tests**: none
**Gate**: build

### Phase 2: Persistence Tasks

### T6: Implement CSV codec and atomic file writer

**What**: Implement escaped CSV serialization, Base64 photo fields, configurable file paths, and atomic replacement writes.
**Where**: `src/main/java/com/ftabah/giftme/adapter/storage/csv/CsvFileStore.java`
**Depends on**: T5
**Requirement**: PROFILE-07, PROFILE-08

**Done when**:

- [ ] Integration tests round-trip commas, quotes, empty optional photos, and Base64 bytes.
- [ ] Integration tests reject malformed CSV and verify a failed write leaves the previous target content unchanged.

**Tests**: integration
**Gate**: full

### T7: Implement CSV repository adapters

**What**: Implement account, profile, and friendship repository adapters using the CSV file store.
**Where**: `src/main/java/com/ftabah/giftme/adapter/storage/csv/`
**Depends on**: T6
**Requirement**: AUTH-02, PROFILE-01, SEARCH-01, FRIEND-01, FRIEND-06

**Done when**:

- [ ] Repository integration tests cover save, lookup, update, search, friendship listing, and missing files.
- [ ] Malformed records fail the operation without returning partially loaded state.

**Tests**: integration
**Gate**: full

### Phase 3: Authentication Tasks

### T8: Implement password and bearer-token security adapters

**What**: Implement one-way password hashing, signed bearer-token creation/validation, and the Spring Security request filter.
**Where**: `src/main/java/com/ftabah/giftme/adapter/security/`
**Depends on**: T7
**Requirement**: AUTH-04, AUTH-05, AUTH-06, PROFILE-02, SEARCH-02

**Done when**:

- [ ] Unit tests prove correct credentials authenticate and incorrect credentials do not.
- [ ] Security tests prove raw passwords and password hashes are absent from token claims and API response DTOs.
- [ ] Requests with no or invalid bearer token return HTTP 401.

**Tests**: unit and integration
**Gate**: full

### T9: Implement registration and login API

**What**: Implement registration/login use cases, DTOs, controllers, and duplicate/short-password error handling.
**Where**: `src/main/java/com/ftabah/giftme/adapter/web/AuthController.java`
**Depends on**: T8
**Requirement**: AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05, AUTH-06

**Done when**:

- [ ] Integration tests cover valid registration, duplicate email, short password, valid login, and invalid credentials with exact status outcomes.
- [ ] Tests verify registration and login return a usable authenticated session.

**Tests**: integration
**Gate**: full

### Phase 4: Profile and Discovery Tasks

### T10: Implement profile update API

**What**: Implement the authenticated profile read/update use case and REST contract for standard and custom sizes.
**Where**: `src/main/java/com/ftabah/giftme/adapter/web/ProfileController.java`
**Depends on**: T9
**Requirement**: PROFILE-01, PROFILE-02, PROFILE-03, PROFILE-04, PROFILE-05, PROFILE-06

**Done when**:

- [ ] Integration tests cover profile creation, update, retrieval, unauthenticated access, invalid age, blank values, and duplicate custom sizes.
- [ ] Tests prove rejected updates preserve the last valid profile.

**Tests**: integration
**Gate**: full

### T11: Add profile photo handling

**What**: Add multipart PNG validation and profile photo persistence/retrieval to the profile API.
**Where**: `src/main/java/com/ftabah/giftme/application/ProfilePhotoService.java`
**Depends on**: T10
**Requirement**: PROFILE-07, PROFILE-08

**Done when**:

- [ ] Integration tests accept a valid PNG at the 2 MiB boundary and return its media type/data.
- [ ] Integration tests reject non-PNG and oversized payloads and preserve the previous photo.

**Tests**: integration
**Gate**: full

### T12: Implement profile search and consultation

**What**: Implement authenticated search and public profile consultation for matching users.
**Where**: `src/main/java/com/ftabah/giftme/adapter/web/ProfileSearchController.java`
**Depends on**: T11
**Requirement**: SEARCH-01, SEARCH-02, SEARCH-03, SEARCH-04, SEARCH-05

**Done when**:

- [ ] Integration tests cover partial case-insensitive name, normalized exact email, empty results, blank query, and unauthenticated access.
- [ ] Tests verify profile results include requested data but never credentials or tokens.

**Tests**: integration
**Gate**: full

### Phase 5: Friendships and Acceptance Tasks

### T13: Implement friendship request API

**What**: Implement send/list friendship request use cases and endpoints with duplicate and self-request validation.
**Where**: `src/main/java/com/ftabah/giftme/adapter/web/FriendshipController.java`
**Depends on**: T12
**Requirement**: FRIEND-01, FRIEND-02, FRIEND-06

**Done when**:

- [ ] Integration tests cover a pending request, request listing, nonexistent recipient, self-request, and duplicate request.
- [ ] Tests prove invalid requests preserve existing friendship records.

**Tests**: integration
**Gate**: full

### T14: Implement friendship decision API

**What**: Implement recipient-only accept/reject transitions and final end-to-end API integration coverage.
**Where**: `src/main/java/com/ftabah/giftme/adapter/web/FriendshipDecisionController.java`
**Depends on**: T13
**Requirement**: FRIEND-03, FRIEND-04, FRIEND-05

**Done when**:

- [ ] Integration tests cover recipient acceptance, recipient rejection, and non-recipient HTTP 403.
- [ ] A full Maven verification passes with all unit and integration tests.
- [ ] The feature validation report can cite evidence for all 26 requirement IDs.

**Tests**: integration
**Gate**: build

## Dependency Validation

| Phase | Task | Depends on | Diagram edge present | Result |
| --- | --- | --- | --- | --- |
| 1 | T1 | None | N/A | PASS |
| 1 | T2 | T1 | T1 -> T2 | PASS |
| 1 | T3 | T2 | T2 -> T3 | PASS |
| 1 | T4 | T3 | T3 -> T4 | PASS |
| 1 | T5 | T4 | T4 -> T5 | PASS |
| 2 | T6 | T5 | Cross-phase dependency | PASS |
| 2 | T7 | T6 | T6 -> T7 | PASS |
| 3 | T8 | T7 | Cross-phase dependency | PASS |
| 3 | T9 | T8 | T8 -> T9 | PASS |
| 4 | T10 | T9 | Cross-phase dependency | PASS |
| 4 | T11 | T10 | T10 -> T11 | PASS |
| 4 | T12 | T11 | T11 -> T12 | PASS |
| 5 | T13 | T12 | Cross-phase dependency | PASS |
| 5 | T14 | T13 | T13 -> T14 | PASS |

## Test Co-location Validation

| Task group | Required tests | Matrix alignment | Result |
| --- | --- | --- | --- |
| T3-T4 domain | Unit tests for every rule and edge case | Domain layer strong default | PASS |
| T6-T7 persistence | Integration tests for round-trip and failure preservation | CSV repository integration | PASS |
| T8-T14 API/security | Integration tests for every endpoint and auth/error path | REST/security integration | PASS |
| T1-T2, T5 configuration/ports | Build/context gate | Entity/config exception | PASS |

## Approval

Tasks are ready for execution after the user selects inline execution or accepts the sub-agent offer required by the spec-driven workflow.