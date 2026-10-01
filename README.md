# GiftMe

GiftMe is a Spring Boot API and web interface for storing personal measurements and sharing them with accepted friends.

## Run

Requirements: JDK 21 and Maven 3.9+.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\latest\jdk-21"
& "$env:USERPROFILE\tools\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
```

Open `http://localhost:8080/` for the interface or `http://localhost:8080/swagger-ui/index.html` for Swagger UI.

## Account security

Registration emits an email verification link through the configured `EmailSender`. Without SMTP configuration, links are logged locally and verification is not required for login. Enable SMTP and set `GIFTME_EMAIL_REQUIRE_VERIFICATION=true` to require verification.

For Gmail SMTP, use a Google App Password (requires 2-Step Verification), not the normal Google account password. Set these variables in the same PowerShell session used to start the app:

```powershell
$env:GMAIL_USERNAME = "your-account@gmail.com"
$env:GMAIL_APP_PASSWORD = "<Google App Password>"
$env:GIFTME_EMAIL_SMTP_ENABLED = "true"
$env:GIFTME_EMAIL_REQUIRE_VERIFICATION = "true"
```

The sender address defaults to `GMAIL_USERNAME`; set `GIFTME_EMAIL_FROM` if the account uses a permitted alternate sender. Set `GIFTME_EMAIL_SMTP_ENABLED=false` to use the local log-only sender. Never commit SMTP credentials to the repository.

Account endpoints include `/api/auth/verify-email`, `/api/auth/password/forgot`, `/api/auth/password/reset`, and `/api/auth/logout`. Logout persists a revocation marker for the bearer token.

## Abuse protection

The application returns `429` after 120 API requests per minute from one source IP. This is an application-level guard. Production deployments still need a reverse proxy, WAF, network rate limiting, and autoscaling for volumetric DDoS protection.

## Storage

The MVP uses CSV files under `data/`. The directory is intentionally ignored by Git because it contains local account and profile data. A relational database is recommended before multi-instance deployment.
