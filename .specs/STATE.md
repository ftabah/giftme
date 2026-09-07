# STATE

## Decisions

### AD-001
- **Decision**: The initial GiftMe backend will use Java Spring Boot with Maven.
- **Reason**: It provides a conventional API foundation for the web client and keeps the first implementation easy to evolve.
- **Trade-off**: The first increment includes backend contracts before a dedicated frontend is selected.
- **Scope**: All initial backend features.
- **Date**: 2026-09-07
- **Status**: active

### AD-002
- **Decision**: CSV files are the initial persistence mechanism; photo bytes are stored as Base64 in the profile CSV.
- **Reason**: The product explicitly starts without a relational database.
- **Trade-off**: The repository is not intended for concurrent or high-volume production writes.
- **Scope**: Initial MVP only; repository interfaces must isolate this choice.
- **Date**: 2026-09-07
- **Status**: active

### AD-003
- **Decision**: Authentication uses local email and password accounts, with passwords stored only as one-way hashes.
- **Reason**: It supports a real login flow without introducing an external identity provider.
- **Trade-off**: Password recovery and email verification are deferred.
- **Scope**: Initial MVP authentication.
- **Date**: 2026-09-07
- **Status**: active

## Handoff

- **Feature**: account-and-friends-mvp
- **Phase / Task**: Execute / T12 - profile search and consultation
- **Completed**: Specification, context, design, task plan, Maven build, Spring Boot entry point, typed CSV configuration, context-load test, validated account/profile models, friendship transitions, application ports, CSV storage, repository adapters, BCrypt password hashing, JWT tokens, Bearer security, registration/login endpoints, authenticated profile endpoints, PNG photo handling, and authenticated profile search.
- **In-progress**: None.
- **Next step**: Implement friendship request API in T13.
- **Blockers**: None. JDK 21 and Maven 3.9.11 are installed under the user profile.
- **Uncommitted files**: `.specs/STATE.md`, `.specs/features/account-and-friends-mvp/tasks.md`, `.specs/features/account-and-friends-mvp/application/port/ProfileRepository.java`, `src/main/java/com/ftabah/giftme/adapter/storage/csv/CsvProfileRepository.java`, `src/main/java/com/ftabah/giftme/adapter/web/ProfileSearchController.java`, `src/test/java/com/ftabah/giftme/application/ProfilePhotoServiceTest.java`
- **Branch**: main