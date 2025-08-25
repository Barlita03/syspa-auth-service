# Syspa Auth Service

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
2. Configure your database in `src/main/resources/application.yml`:
   ```yml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/login-service
       username: <your_username>
       password: <your_password>
   ```
   Change the values according to your environment.

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

### 1. User Registration (Signup)

**URL:** `/auth/V1/signup`  
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

**URL:** `/auth/V1/login`  
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

## Authentication Flow

1. The user registers via `/auth/V1/signup`.
2. The user logs in via `/auth/V1/login` and receives a JWT.
3. The JWT is used in the header to access protected resources.

---
