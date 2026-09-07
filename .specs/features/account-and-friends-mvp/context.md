# Account and Friends MVP Context

**Gathered:** 2026-09-07
**Spec:** `.specs/features/account-and-friends-mvp/spec.md`
**Status:** Approved for design

## Feature Boundary

The first GiftMe vertical slice provides local account authentication, a personal profile with standard and custom sizes plus an optional PNG photo, authenticated user search and profile consultation, and friendship request state. CSV is the temporary persistence layer.

## Implementation Decisions

### Workspace and technology

- Work in the newly cloned `ftabah/giftme` repository, separate from `time-spock-2`.
- Use Java Spring Boot with Maven.
- Start with a backend API; the client screen can consume the API without coupling the domain to a UI framework.

### Authentication

- Use local email/password accounts.
- Store password hashes only.
- Return an authenticated bearer session from registration and login.

### Profile and image data

- Store the requested fields: shoe size, waist size, shirt size, name, age, height, custom named sizes, and optional photo.
- Use string values for measurements and an integer for age.
- Accept PNG photos up to 2 MiB decoded and preserve the `image/png` media type.

### Discovery and friendship

- Authenticated users may search by partial display name or exact email.
- A matching profile can be consulted before friendship acceptance, according to the confirmed product choice.
- Adding a friend creates a request that the recipient accepts or rejects.

## Agent's Discretion

- API path naming, DTO shape, CSV column layout, token implementation details, and package structure, provided they satisfy the spec and keep storage replaceable.
- Error response envelope and exact CSV file names.
- Test framework details within the Maven/Spring ecosystem.

## Declined / Undiscussed Gray Areas -> Assumptions

- Password recovery, email verification, rate limiting, account deletion, and concurrent multi-process CSV writers are deferred as documented in the spec.
- The default bearer-token session and measurement representation are implementation assumptions pending production review.

## Specific References

The requested profile fields and the 2 MB PNG limit come directly from the user's initial product description. The user selected local email/password login, request-based friendship, and profile consultation for any authenticated user who finds a matching account.

## Deferred Ideas

- Password recovery and email verification.
- Notifications.
- Gift lists and recommendation workflows.
- Migration from CSV to a relational database.