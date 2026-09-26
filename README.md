# perfume-shop

Spring Boot backend for the perfume shop.

## Setup (required before running)

1. **Environment variables**
   Copy `.env.example` to `.env` and fill in your own values
   (database, JWT secret, Gmail app password, admin account).
   `.env` is git-ignored on purpose.

2. **Firebase service account**
   Download your Firebase service account JSON and place it at:
   ```
   src/main/resources/firebase-service-account.json
   ```
   This file is git-ignored on purpose. The app still boots without it,
   but push notifications stay disabled and you'll see an error in the logs.

## Run

```
mvn spring-boot:run
```