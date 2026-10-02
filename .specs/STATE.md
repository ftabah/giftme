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
- **Trade-off**: Password recovery and email verification were deferred from the initial MVP.
- **Scope**: Initial MVP authentication.
- **Date**: 2026-09-07
- **Status**: superseded by AD-004

### AD-004
- **Decision**: Keep account verification and password recovery behind the `EmailSender` port; enable SMTP through environment configuration and retain console logging as the local fallback.
- **Reason**: Support real email delivery without coupling application logic to Gmail or storing credentials in the repository.
- **Trade-off**: Email is not delivered until SMTP is enabled and provider credentials are configured in the runtime environment.
- **Scope**: Account registration, email verification, and password recovery.
- **Date**: 2026-10-01
- **Status**: active

## Handoff

- **Feature**: account-and-friends-mvp
- **Phase / Task**: MVP complete; post-MVP authentication and friends UI follow-up committed as `a7008ce`.
- **Completed**: T1-T14; Gmail-capable optional SMTP with console fallback; registration feedback and email verification page with five-second login redirect; password-reset page and masked auth/recovery diagnostics; correct HTTP auth error dispatch; friends and pending-requests views with received-request badge; accepted-friend cards containing friend-visible profile data; account-switch cleanup for photos, profile fields, and search results; 200x200 crop, display, and server-side image normalization. `mvn test` passed with 33 tests before the state update.
- **In-progress**: None.
- **Next step**: Configure `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD`, and `GIFTME_EMAIL_SMTP_ENABLED=true` in the application runtime, start the app, and verify password-recovery and registration email delivery. The last environment check found these variables absent from process, user, and machine scopes.
- **Blockers**: Gmail delivery remains unverified until runtime SMTP credentials are configured. The application was stopped at the user's request.
- **Uncommitted files**: `.specs/STATE.md`, `.vscode/` (untracked; preserve and exclude from feature commits).
- **Branch**: main