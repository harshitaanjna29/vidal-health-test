# Vidal Health Webhook App

This small Spring Boot app performs the required flow on startup:

- POST to the generateWebhook API with `name`, `regNo`, `email`.
- Reads the returned `webhook` and `accessToken`.
- Chooses a SQL answer based on `regNo` parity (placeholder queries).
- Stores the chosen SQL in H2 database.
- POSTs the final SQL to the returned `webhook` URL with `Authorization: Bearer <accessToken>`.
