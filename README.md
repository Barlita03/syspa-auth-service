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

> **Note:** In all endpoint URLs, `{version}` is a placeholder for the current API version (e.g., `V1`, `V2`). Replace `{version}` with the actual version used by your deployment.

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
