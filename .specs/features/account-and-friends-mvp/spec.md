# Account and Friends MVP Specification

## Problem Statement

GiftMe needs a private, authenticated place where a person can record gift-relevant personal measurements and discover those details for people they know. The first increment must work without a relational database, while preserving a storage boundary that can later move to a database.

## Goals

- [ ] Allow an authenticated user to create an account and log in with email and password.
- [ ] Allow a user to maintain the requested personal data, including custom named sizes and an optional PNG photo up to 2 MB.
- [ ] Allow authenticated users to search for other users and inspect their registered data.
- [ ] Allow users to send, accept, and reject friendship requests while persisting all data in CSV files.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Password recovery and email verification | Requires external email delivery and is not needed for the first vertical slice. |
| OAuth or social login | Local authentication is the confirmed initial strategy. |
| Real-time notifications | The first increment can expose request state through normal queries. |
| Relational database or migration tooling | CSV is the explicit initial persistence choice. |
| Gift lists, recommendations, or event reminders | These are product features beyond personal data and friendship discovery. |
| Administrative user management | No administrator role is defined for the MVP. |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Account creation | The login surface also exposes registration for a new email and password. | A login-only system cannot create the first user. | yes |
| Password policy | Passwords must be at least 8 characters; the API returns a validation error for shorter values. | Provides a concrete MVP bound without inventing complex policy. | no |
| Password storage | Store a one-way password hash and never the raw password in CSV or responses. | Required baseline for local authentication. | yes |
| Session mechanism | Use a stateless bearer token for authenticated API calls. | Keeps the initial backend client-agnostic. | no |
| Search matching | Case-insensitive substring matching on display name and exact normalized email matching. | Makes friend discovery useful while avoiding case-sensitive surprises. | no |
| Search result privacy | Any authenticated user may inspect the public profile data returned for a matching user, even before friendship acceptance. | This follows the user's selected “qualquer usuário encontrado” behavior. | yes |
| Friendship duplicate handling | A repeated request for the same pair is rejected without creating another request. | Prevents duplicate relationships and gives retries a deterministic result. | no |
| Friendship direction | A request moves from `PENDING` to `ACCEPTED` or `REJECTED`; only the recipient may decide. | Makes the confirmation flow explicit. | yes |
| Personal data fields | Shoe size, waist size, shirt size, name, age, height, custom sizes, and optional photo are all profile fields. | Directly reflects the requested initial data. | yes |
| Measurement representation | Store the three standard sizes and height as strings so values such as `42`, `M`, `90 cm`, and `1.80 m` remain possible. Age is an integer. | Avoids imposing a unit system before product decisions are made. | no |
| Custom sizes | Each custom size has a non-empty name and non-empty value; names are unique within one profile after case folding. | Prevents ambiguous duplicate attributes. | no |
| Photo encoding | Accept only PNG content with a maximum decoded size of 2 MiB; store it as Base64 with its media type. | CSV cannot hold raw binary safely, and the requested limit is explicit. | no |
| CSV write consistency | A failed multi-file write leaves the previous in-memory state and reports an error; atomic replacement is used where supported. | Prevents partial updates from silently corrupting the local data set. | no |
| Concurrent writers | Single application instance is the supported MVP mode. | CSV locking and multi-process conflict resolution are outside the initial scope. | yes |
| Deletion lifecycle | Account deletion is deferred; no profile or friendship deletion flow is included in this increment. | Keeps the first slice focused. | yes |

**Open questions:** none for the initial implementation; unconfirmed defaults above are implementation assumptions and should be revisited before production deployment.

## User Stories

### P1: Register and Log In - MVP

**User Story**: As a person using GiftMe, I want to create an account and log in so that my profile can be associated with me.

**Why P1**: All profile and friend data is user-scoped.

**Acceptance Criteria**:

1. WHEN a visitor registers with a valid unused email and password of at least 8 characters THEN the system SHALL create exactly one account and return an authenticated session. <!-- AUTH-01 -->
2. IF a visitor registers with an email already in use THEN the system SHALL reject the request with HTTP 409 and SHALL NOT create another account. <!-- AUTH-02 -->
3. IF a visitor registers with a password shorter than 8 characters THEN the system SHALL reject the request with HTTP 400 and SHALL NOT create the account. <!-- AUTH-03 -->
4. WHEN a registered user submits the correct email and password THEN the system SHALL return an authenticated session. <!-- AUTH-04 -->
5. IF a user submits an unknown email or an incorrect password THEN the system SHALL return HTTP 401 without revealing which credential was incorrect. <!-- AUTH-05 -->
6. The system SHALL never persist or return a user's raw password. <!-- AUTH-06 -->

**Independent Test**: Register two accounts, log in with both valid credentials, and verify duplicate, short-password, and invalid-credential outcomes.

### P1: Maintain Personal Profile - MVP

**User Story**: As an authenticated user, I want to save my gift-relevant personal data so that friends can consult accurate sizes.

**Why P1**: The profile is the core value of GiftMe.

**Acceptance Criteria**:

1. WHEN an authenticated user creates or updates a profile with name, age, height, shoe size, waist size, and shirt size THEN the system SHALL persist those values for that user. <!-- PROFILE-01 -->
2. IF an unauthenticated user requests profile creation or update THEN the system SHALL return HTTP 401 and SHALL leave profile data unchanged. <!-- PROFILE-02 -->
3. IF age is not an integer from 0 through 150 inclusive THEN the system SHALL reject the profile update with HTTP 400 and SHALL preserve the previous profile. <!-- PROFILE-03 -->
4. IF a required text profile value is blank or a custom size name/value is blank THEN the system SHALL reject the profile update with HTTP 400 and SHALL preserve the previous profile. <!-- PROFILE-04 -->
5. WHEN an authenticated user adds a custom size with a new case-insensitive name THEN the system SHALL persist the name and value in that user's profile. <!-- PROFILE-05 -->
6. IF an authenticated user adds a custom size whose name already exists in that profile ignoring case THEN the system SHALL reject the update with HTTP 400 and SHALL preserve the previous custom sizes. <!-- PROFILE-06 -->
7. WHEN an authenticated user uploads a PNG photo no larger than 2 MiB decoded THEN the system SHALL persist the photo bytes and media type `image/png` in the profile. <!-- PROFILE-07 -->
8. IF an uploaded photo is not PNG or exceeds 2 MiB decoded THEN the system SHALL reject the profile update with HTTP 400 and SHALL preserve the previous photo. <!-- PROFILE-08 -->

**Independent Test**: Log in, save all standard fields, add two custom sizes, upload a valid PNG, reload the profile, and verify rejected invalid updates leave prior values intact.

### P1: Search and Consult Profiles - MVP

**User Story**: As an authenticated user, I want to find another user and consult their registered data so that I can choose an appropriate gift.

**Why P1**: Discovery is the mechanism that makes stored profile data useful.

**Acceptance Criteria**:

1. WHEN an authenticated user searches with a non-blank query THEN the system SHALL return matching users by case-insensitive display-name substring or normalized exact email. <!-- SEARCH-01 -->
2. IF an unauthenticated user searches for users THEN the system SHALL return HTTP 401 and SHALL return no profile data. <!-- SEARCH-02 -->
3. WHEN an authenticated user opens a matching user's profile THEN the system SHALL return that user's name, age, height, standard sizes, custom sizes, and photo metadata/data when present. <!-- SEARCH-03 -->
4. IF no user matches the search query THEN the system SHALL return HTTP 200 with an empty result list. <!-- SEARCH-04 -->
5. The system SHALL exclude password hashes and authentication tokens from every profile or search response. <!-- SEARCH-05 -->

**Independent Test**: Create two accounts with distinct profiles, search by partial name and exact email, inspect one profile, and verify unauthenticated and no-match behavior.

### P1: Manage Friendship Requests - MVP

**User Story**: As an authenticated user, I want to request and confirm friendships so that my known contacts are recorded for future GiftMe features.

**Why P1**: Friendship state gives the product a durable relationship model without blocking the initial profile lookup behavior.

**Acceptance Criteria**:

1. WHEN an authenticated user sends a friendship request to another existing user THEN the system SHALL create exactly one `PENDING` request owned by the recipient. <!-- FRIEND-01 -->
2. IF a user sends a request to themself, to a nonexistent user, or for an already pending/accepted pair THEN the system SHALL reject the request with HTTP 400 and SHALL preserve existing friendship state. <!-- FRIEND-02 -->
3. WHEN the recipient accepts a pending request THEN the system SHALL transition that request to `ACCEPTED`. <!-- FRIEND-03 -->
4. WHEN the recipient rejects a pending request THEN the system SHALL transition that request to `REJECTED`. <!-- FRIEND-04 -->
5. IF a user other than the recipient attempts to accept or reject a pending request THEN the system SHALL return HTTP 403 and SHALL preserve the request state. <!-- FRIEND-05 -->
6. WHEN an authenticated user lists friendship requests THEN the system SHALL return the requests relevant to that user with their current state and counterpart identity. <!-- FRIEND-06 -->

**Independent Test**: Create two users, send a request, verify pending state, accept it as the recipient, and verify duplicate/self/unauthorized decisions are rejected.

## Edge Cases

- IF a CSV file is missing, malformed, or contains an invalid record THEN the system SHALL fail the affected operation with an error and SHALL not expose partially loaded records.
- IF a profile update fails after validation during persistence THEN the system SHALL preserve the previous profile in memory and report the failure.
- IF a photo is absent THEN the system SHALL return a profile with no photo rather than a fabricated placeholder.
- IF a search query is blank or whitespace-only THEN the system SHALL return HTTP 400.
- IF the same friendship request is retried THEN the system SHALL not create a second relationship record.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| AUTH-01 | P1: Register and Log In | Design | In Design |
| AUTH-02 | P1: Register and Log In | Design | In Design |
| AUTH-03 | P1: Register and Log In | Design | In Design |
| AUTH-04 | P1: Register and Log In | Design | In Design |
| AUTH-05 | P1: Register and Log In | Design | In Design |
| AUTH-06 | P1: Register and Log In | Design | In Design |
| PROFILE-01 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-02 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-03 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-04 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-05 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-06 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-07 | P1: Maintain Personal Profile | Design | In Design |
| PROFILE-08 | P1: Maintain Personal Profile | Design | In Design |
| SEARCH-01 | P1: Search and Consult Profiles | Design | In Design |
| SEARCH-02 | P1: Search and Consult Profiles | Design | In Design |
| SEARCH-03 | P1: Search and Consult Profiles | Design | In Design |
| SEARCH-04 | P1: Search and Consult Profiles | Design | In Design |
| SEARCH-05 | P1: Search and Consult Profiles | Design | In Design |
| FRIEND-01 | P1: Manage Friendship Requests | Design | In Design |
| FRIEND-02 | P1: Manage Friendship Requests | Design | In Design |
| FRIEND-03 | P1: Manage Friendship Requests | Design | In Design |
| FRIEND-04 | P1: Manage Friendship Requests | Design | In Design |
| FRIEND-05 | P1: Manage Friendship Requests | Design | In Design |
| FRIEND-06 | P1: Manage Friendship Requests | Design | In Design |

**Coverage:** 26 total, 0 mapped to tasks, 26 in design.

## Success Criteria

- [ ] A new user can register, log in, save all requested profile fields, and retrieve the same values after an application restart.
- [ ] A second authenticated user can find the first by partial name or exact email and inspect the first profile without receiving credentials.
- [ ] Friendship requests have deterministic pending, accepted, and rejected transitions and cannot be duplicated.
- [ ] Invalid age, custom sizes, credentials, and photos never overwrite previously valid data.