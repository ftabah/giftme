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

Registration emits an email verification link through the configured `EmailSender`. The local implementation logs the link. Password reset links are also logged locally until an SMTP adapter is configured. Verification is required for login by default.

Configure the application with:

```text
giftme.email.base-url=http://localhost:8080
giftme.email.token-expiration-minutes=30
giftme.email.require-verification=true
giftme.security.jwt-secret=<at-least-32-characters-secret>
```

Account endpoints include `/api/auth/verify-email`, `/api/auth/password/forgot`, `/api/auth/password/reset`, and `/api/auth/logout`. Logout persists a revocation marker for the bearer token.

## Abuse protection

The application returns `429` after 120 API requests per minute from one source IP. This is an application-level guard. Production deployments still need a reverse proxy, WAF, network rate limiting, and autoscaling for volumetric DDoS protection.

## Storage

The MVP uses CSV files under `data/`. The directory is intentionally ignored by Git because it contains local account and profile data. A relational database is recommended before multi-instance deployment.
