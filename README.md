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

# Syspa Auth Service

**Current API version:** 1.0.0

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
  "username": "johndoe",
  "password": "password123",
  "email": "johndoe@example.com"
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
**Description:** Authenticates a user and returns a JWT.

**Example body:**

```json
{
  "username": "johndoe",
  "password": "password123"
}
```

**Responses:**

- 200 OK: Returns the JWT token as a string.
- 400 Bad Request: Returns an error message if authentication fails.

**Example success response:**

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example error:**

```
Invalid username or password
```

---

## User Model

```json
{
  "id": 1,
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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
