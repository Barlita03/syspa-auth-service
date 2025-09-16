# Syspa Auth Service

**Current API version:** 1.3.0

User authentication microservice built with Spring Boot. Allows user registration and login, returning a **JWT** for authentication in protected endpoints.

---

## Table of Contents

- [Technologies](#technologies)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Running](#running)
- [API Endpoints](#api-endpoints)
- [User Model](#user-model)
- [Validations](#validations)
- [Common Errors](#common-errors)
- [JWT Usage](#jwt-usage)

---

## Technologies

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT (JSON Web Token)

---

## Requirements

- Java 17+
- Maven
- PostgreSQL

---

## Configuration

1. Clone the repository:

```bash
git clone https://github.com/Barlita03/syspa-auth-service.git
```

2. **Database configuration:**

By default, the application uses PostgreSQL and expects credentials via environment variables. The `src/main/resources/application.yml` file is set up for this, using:

- `AUTH_DB_HOST` (default: `localhost`)
- `AUTH_DB_PORT` (default: `5432`)
- `AUTH_DB_NAME` (default: `login-service`)
- `AUTH_DB_USERNAME` (default: `postgres`)
- `AUTH_DB_PASSWORD` (no default, required for production)
- `ALLOWED_ORIGINS` (default: `*`)
- `RECAPTCHA_SECRET` (default: Google test key)

You can override these by setting them in your environment before running the application.

3. **Quickly switch database for local testing:**

If you want to run the service locally without a PostgreSQL instance, you can use the provided alternative config files:

- `src/main/resources/application.hsqldb.yml` — Uses an in-memory HSQLDB database (no setup required)
- `src/main/resources/application.postgre.yml` — Uses PostgreSQL with environment variables (default)

To use one of these profiles, simply copy the desired file over `application.yml` before starting the app. For example, to use HSQLDB:

```bash
cp src/main/resources/application.hsqldb.yml src/main/resources/application.yml
```

This is useful for quick local tests or development without needing to configure a real database. Remember to switch back to the PostgreSQL config (or your own) for production or integration with your real database.

4. **JWT Secret configuration:**

The application requires a secret key for signing JWT tokens. You must set the environment variable `JWT_SECRET` with a value of at least 32 ASCII characters (letters, numbers, and symbols). Example:

```bash
export JWT_SECRET=mysupersecretkeythatismorethan32byteslong!
```

If the secret is too short, the application will fail to start for security reasons.

---

## Running

1. Install dependencies and build:
   ```bash
   mvn clean install
   ```
2. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   By default, it runs at `http://localhost:8080`

---

## API Endpoints

> **Note:** In all endpoint URLs, `{version}` is a placeholder for the current API version (e.g., `V1`, `V2`). Replace `{version}` with the actual version used by your deployment. This makes the documentation version-agnostic and easier to maintain.

### 1. User Registration (Signup)

**URL:** `/auth/{version}/signup`  
**Method:** `POST`  
**Description:** Registers a new user.

**Example body:**

```json
{
  "user": {
    "username": "johndoe",
    "password": "password123",
    "email": "johndoe@example.com"
  },
  "recaptchaToken": "<token-from-frontend>"
}
```

**Responses:**

- 201 Created: Returns the created user.
- 400 Bad Request: Returns an error message if validation fails.

**Example success response:**

```json
{
  "id": 1,
  "username": "johndoe",
  "email": "johndoe@example.com",
  "role": "USER"
}
```

**Example error:**

```
Username is already in use
```

---

### 2. Login

**URL:** `/auth/{version}/login`  
**Method:** `POST`  
**Description:** Authenticates a user and returns an access token (JWT) and a refresh token.

**Example body:**

```json
{
  "user": {
    "username": "johndoe",
    "password": "password123"
  },
  "recaptchaToken": "<token-from-frontend>"
}
```

**Responses:**

- 200 OK: Returns an object with `accessToken` and `refreshToken`.
- 400 Bad Request: Returns an error message if authentication fails.

**Example success response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg..."
}
```

**Example error:**

```
Invalid username or password
```

---

### 3. Refresh Token

**URL:** `/auth/{version}/refresh`  
**Method:** `POST`  
**Description:** Renews the access token using a valid refresh token. The refresh token is rotated (invalidated and replaced by a new one).

**Example body:**

```json
{
  "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg..."
}
```

**Responses:**

- 200 OK: Returns a new `accessToken` and a new `refreshToken`.
- 401 Unauthorized: If the refresh token is invalid, expired, or already used.

**Example success response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "new_refresh_token..."
}
```

**Example error:**

```
Invalid or expired refresh token
```

**Security notes:**

- Refresh tokens can only be used once (rotation). If you try to reuse an old one, it will be rejected.
- If a refresh token is stolen and used, the legitimate user will be forced to re-authenticate.
- Never share or store refresh tokens insecurely on the frontend.

---

## Role-based Access Control (RBAC)

This service implements real role-based access control using JWT and Spring Security. There are two roles: `USER` and `ADMIN`.

- The user's role is stored in the database and included in the JWT as a claim.
- Endpoints can be protected using `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("hasRole('USER')")`.
- Example endpoints:
  - `/admin/hello` — Only accessible by users with the `ADMIN` role.
  - `/user/hello` — Only accessible by users with the `USER` role.

If a user tries to access an endpoint without the required role, the API returns:

```
HTTP/1.1 403 Forbidden
{
  "error": "Access denied: you do not have permission to perform this action."
}
```

The role is assigned at registration (default: USER) or can be set to ADMIN in the database. The login process always issues a JWT with the correct role from the database.

---

## User Model

```json
{
  "id": "b3b1c2d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "username": "johndoe",
  "password": "<encrypted>",
  "email": "johndoe@example.com",
  "role": "USER"
}
```

---

## Validations

- **Username:** minimum 5 characters, unique.
- **Password:** minimum 8 characters.
- **Email:** valid format and unique.

---

## Common Errors

- "Username is already in use"
- "Email is already in use"
- "Invalid email"
- "Invalid password: must be at least 8 characters long"
- "Invalid username: must be at least 5 characters long"
- "Invalid username or password"

---

## JWT Usage

After login, the backend returns a JWT. To access protected endpoints, send the token in the header:

```http
Authorization: Bearer <token>
```

Example using fetch (JavaScript):

```js
fetch("/api/protected", {
  headers: {
    Authorization: "Bearer " + token,
  },
});
```

---

## Logging for Production

This project is configured for production-grade logging using Logback:

- Logs are written in JSON format for easy integration with monitoring systems.
- Log files are rotated daily and kept for 30 days.
- General logs are saved to `logs/app.log` and errors to `logs/error.log`.
- You can adjust log levels and retention in `src/main/resources/logback-spring.xml`.

**Requirements:**

- The directory `logs/` must be writable by the application.
- The dependency `logstash-logback-encoder` is included in `pom.xml`.

**Recommendations:**

- Use `INFO` or `WARN` level in production, and `DEBUG` only for troubleshooting.
- If running in Docker, consider mapping the `logs/` directory to a persistent volume.
- For advanced monitoring, forward logs to a centralized system (ELK, Loki, etc.).

---

## Testing

This project includes unit and integration tests for the main services, controllers, utilities, and repositories.

### How to run the tests?

```bash
mvn test
```

This command runs all automated tests using an in-memory H2 database and a special test configuration.

### Testing configuration details

- **Database:** Tests use an in-memory H2 database, so you do not need PostgreSQL or any extra setup.
- **JWT:** The JWT secret for tests is defined in `src/test/resources/application.yml`.
- **Security:** Controller tests disable security and CSRF to allow endpoint validation without authentication.
- **Coverage:** There are tests for `UserService`, `RegistrationController`, `JwtUtil`, and `UserRepository`.

---

## Metrics (Actuator)

The service also exposes a metrics endpoint for monitoring and observability:

- **URL:** `/actuator/metrics`
- **Method:** `GET`
- **Response:**
  - 200 OK with a list of available metrics (e.g., JVM, memory, CPU, HTTP requests, etc.)

Example:

```json
{
  "names": [
    "jvm.memory.used",
    "jvm.gc.pause",
    "http.server.requests",
    "process.cpu.usage",
    "system.cpu.usage"
    // ... more metrics
  ]
}
```

You can query individual metrics by appending the metric name, e.g. `/actuator/metrics/jvm.memory.used`.

This endpoint is enabled by default and does not require authentication. It is useful for Prometheus, Grafana, and other monitoring tools.

---

## Health Check (Actuator)

The service exposes a standard health check endpoint for monitoring and orchestration tools:

- **URL:** `/actuator/health`
- **Method:** `GET`
- **Response:**
  - 200 OK with health status and details (e.g., database connection, disk space, etc.)

Example:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

This endpoint is enabled by default and does not require authentication. It is useful for readiness/liveness probes and external monitoring.

---

## HTTP Security Headers

This service applies the following HTTP security headers to all responses:

- **X-Content-Type-Options: nosniff** — Prevents browsers from MIME-sniffing a response away from the declared content-type.
- **X-Frame-Options: DENY** — Prevents the site from being embedded in an iframe, protecting against clickjacking.
- **Content-Security-Policy: default-src 'none';** — Blocks all resource loading by default. Adjust as needed if serving UI or static content.
- **Referrer-Policy: no-referrer** — No referrer information is sent with requests.
- **Strict-Transport-Security** — Enforced only when running behind HTTPS. Forces browsers to use HTTPS for all future requests.

---

## Rate Limiting (Brute-force Protection)

This service implements rate limiting on sensitive endpoints (`/login` and `/signup`) to protect against brute-force attacks and abuse.

- Each IP address is allowed up to **5 requests per minute** to these endpoints.
- If the limit is exceeded, the server responds with HTTP 429 (Too Many Requests).
- After 1 minute, the quota is reset and requests are allowed again.

This is implemented using [Bucket4j](https://github.com/bucket4j/bucket4j), a Java rate-limiting library. You can adjust the rate and scope in the `RateLimitingFilter` class.

**Example error response:**

```
HTTP/1.1 429 Too Many Requests
Content-Type: text/plain

Too Many Requests
```

---

## CORS (Cross-Origin Resource Sharing)

This service supports secure and flexible CORS configuration to control which frontends or domains can access the API from browsers.

- By default, all origins are allowed (open for development and open source use).
- You can restrict allowed origins by setting the `ALLOWED_ORIGINS` environment variable (comma-separated list).
- Example (Linux/macOS):
  ```bash
  export ALLOWED_ORIGINS=https://my-frontend.com,https://admin.my-frontend.com
  ```
- Example (Windows):
  ```powershell
  $env:ALLOWED_ORIGINS="https://my-frontend.com,https://admin.my-frontend.com"
  ```
- If set, only those origins will be able to make CORS requests to the API.
- You can always change this variable at deployment time without code changes.

This is implemented via a global CORS filter in `SecurityConfig`.

---

## CAPTCHA (Bot Protection)

This service requires a valid Google reCAPTCHA token for login and signup endpoints to prevent automated abuse and bots.

- The frontend must obtain a reCAPTCHA token (v2) and send it in the `recaptchaToken` field of the request body.
- The backend validates the token with Google before processing authentication or registration.
- If the token is missing or invalid, the server responds with HTTP 400 and a clear error message.

**Environment variable:**

- `RECAPTCHA_SECRET` — Your Google reCAPTCHA secret key. If not set, a public test key is used for development (`6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe`).

**Testing locally:**

- You can use the official Google test keys:
  - Site key: `6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI`
  - Secret key: `6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe`
- With these, any token or string will be accepted as valid for local/dev testing.

**Example error responses:**

```
HTTP/1.1 400 Bad Request
Missing reCAPTCHA token
```

or

```
HTTP/1.1 400 Bad Request
Invalid reCAPTCHA
```

---

# Advanced Auditing & Monitoring

The system implements a dedicated audit logger for security-related events, configured in `logback-spring.xml` and writing to `logs/audit.log` in JSON format. Audited events include:

- User registration
- User login
- Refresh token rotation and usage
- Invalid refresh token attempts

Each event includes relevant details such as username, email, role, user ID, and involved tokens. The audit log file is rotated daily and retained for 90 days.

Example of an audited event:

```json
{
  "timestamp": "2025-09-15T12:34:56.789Z",
  "level": "INFO",
  "event": "USER_LOGIN",
  "username": "johnsmith",
  "id": "b1c2d3e4-...",
  "role": "USER"
}
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
