# Vidal Health Webhook App

This small Spring Boot app performs the required flow on startup:

- POST to the generateWebhook API with `name`, `regNo`, `email`.
- Reads the returned `webhook` and `accessToken`.
- Chooses a SQL answer based on `regNo` parity (placeholder queries).
- Stores the chosen SQL in H2 database.
- POSTs the final SQL to the returned `webhook` URL with `Authorization: Bearer <accessToken>`.

Run:

```
mvn spring-boot:run
```

You can override applicant properties via JVM system properties or environment variables:

```
mvn -Dapp.regno=REG12346 -Dapp.name="Alice" -Dapp.email=alice@example.com spring-boot:run
```

Notes:
- The SQL queries in this repo are placeholders; replace with the actual query solution if known.
- H2 console is available at `/h2-console` when running.
# vidal-health-test