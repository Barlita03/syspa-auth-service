# User Authentication Microservice

A simple Spring Boot REST API microservice for user authentication.  
It provides endpoints for **user registration (signup)** and **user login**, returning a **JWT token** upon successful login.  
The JWT can then be used to access protected endpoints in your application.

---

## Table of Contents
- [Technologies](#technologies)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [User Model](#user-model)
- [Frontend Usage](#frontend-usage)

---

## Technologies
- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- H2 / MySQL (or your preferred database)
- JWT (JSON Web Token)

---

## Getting Started
1. Clone the repository:
```bash
git clone <https://github.com/Barlita03/syspa-auth-service.git>