# PayFlow — Digital Wallet & Transaction Processing System

A Java 21 / Spring Boot REST API for a **simulated** digital wallet. It is a learning and portfolio project, not a real banking/payment product.

## Features in this source package
- User registration and login with BCrypt password hashing
- JWT access tokens and stateless Spring Security
- One INR wallet created for each registered user
- Wallet balance endpoint
- Wallet-to-wallet transfer with `@Transactional`
- Pessimistic row locks acquired in ascending wallet-ID order
- Idempotency key to prevent a retried request from applying the same transfer twice
- Transaction history
- Admin-only user and transaction listing
- Request validation and consistent exception responses

## Requirements
- JDK 21
- Maven 3.9+
- MySQL 8+
- Eclipse IDE (or another Java IDE)
- Postman

## 1. Create database
Run in MySQL Workbench:
```sql
CREATE DATABASE payflow_db;
```

## 2. Configure database credentials
Edit `src/main/resources/application.properties`, or set environment variables:
- `DB_URL` (default `jdbc:mysql://localhost:3306/payflow_db`)
- `DB_USERNAME` (default `root`)
- `DB_PASSWORD` (your MySQL password)
- `PAYFLOW_JWT_SECRET` (Base64-encoded random key of at least 32 bytes)
- `PAYFLOW_JWT_EXPIRATION_MS` (default 86400000 = 24 hours)

The bundled JWT secret is a development fallback only. Generate a private key with:
```bash
openssl rand -base64 32
```
Set the result as `PAYFLOW_JWT_SECRET`. Never commit real secrets.

## 3. Import into Eclipse
1. Extract the ZIP.
2. Eclipse → File → Import → Maven → Existing Maven Projects.
3. Select the extracted `PayFlow` folder.
4. Finish, then right-click project → Maven → Update Project.
5. Set Project JRE to Java 21.
6. Run `PayflowApplication.java` as Java Application.

Hibernate creates/updates tables because `ddl-auto=update` is enabled for this learning project.

## 4. API quick test
Base URL: `http://localhost:8080`

### Register user A
`POST /api/auth/register`, Body → raw → JSON:
```json
{
  "fullName": "User One",
  "email": "one@example.com",
  "password": "Password123",
  "phone": "9000000001"
}
```
The response includes `accessToken`, user profile, and wallet details. Save the token privately.

### Register user B
Repeat with a different email and password.

### Login
`POST /api/auth/login`:
```json
{
  "email": "one@example.com",
  "password": "Password123"
}
```

### Authenticated request
In Postman → Authorization → Bearer Token, paste the access token:
- `GET /api/auth/profile`
- `GET /api/wallet`
- `GET /api/transfers/history`

### Fund a demo wallet
This project deliberately has no public money-creation endpoint. For local demonstration only, credit a test wallet directly in MySQL:
```sql
UPDATE wallets SET balance = 1000.00 WHERE user_id = 1;
```
Use only test data. Do not describe this as a real deposit feature.

### Transfer
`POST /api/transfers`
Headers:
- `Authorization: Bearer <token>` (Postman Bearer Token tab)
- `Idempotency-Key: demo-transfer-001` (use a new unique key for each new transfer)

Body:
```json
{
  "receiverEmail": "two@example.com",
  "amount": 125.50,
  "description": "Demo transfer"
}
```
The transaction should debit sender and credit receiver in one database transaction.

### Admin APIs
Promote a local test user manually:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'one@example.com';
```
Log in again to receive a token containing the updated role.
- `GET /api/admin/users`
- `GET /api/admin/transactions`

## API summary
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register + create wallet |
| POST | `/api/auth/login` | Public | Login + JWT |
| GET | `/api/auth/profile` | JWT | Current user's profile |
| GET | `/api/wallet` | JWT | Current wallet and balance |
| POST | `/api/transfers` | JWT | Transfer to another user |
| GET | `/api/transfers/history` | JWT | Current user's transaction history |
| GET | `/api/admin/users` | ADMIN | List users |
| GET | `/api/admin/transactions` | ADMIN | List all transactions |

## Important engineering notes
- `BigDecimal` is used for currency, not `double`.
- The transfer method is `@Transactional`; a failure should roll back both balance updates and the transaction insert.
- Pessimistic row locks serialize concurrent updates to the same wallet rows. Locks are acquired in ascending wallet ID order to reduce deadlocks.
- The idempotency key is unique in the database. A repeated key from the same sender returns the original transaction instead of applying it again.
- This is still a portfolio-grade learning implementation, not a production financial system. A production wallet needs a double-entry immutable ledger, reconciliation, audit trails, rate limits, refresh-token/revocation strategy, key rotation, monitoring, fraud controls, regulatory/compliance review, robust integration tests, and carefully designed race handling for idempotency conflicts.
- Do not claim a feature is production-ready or tested until you have run the project and verified it locally.
