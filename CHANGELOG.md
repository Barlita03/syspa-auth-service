# Changelog

## [1.4.0] - 2025-09-15

### Added

- Exposed standard well-known endpoints:
  - `/.well-known/jwks.json`: Publishes the RSA public key in JWKS format for external JWT validation. Key values (`n`, `e`) are configured via environment variables (`JWT_RSA_MODULUS`, `JWT_RSA_EXPONENT`).
  - `/.well-known/openid-configuration`: Publishes basic OIDC configuration (issuer, endpoints, algorithms, JWKS URI, etc).
- New endpoint `/auth/V1/logout`: Revokes the refresh token and logs out the user. The refresh token is invalidated in the backend and cannot be reused. The event is recorded in the audit log. The access token (JWT) must be deleted on the frontend.

## [1.3.0] - 2025-09-15

### Added

- Real role-based access control (RBAC) with USER/ADMIN roles, enforced via JWT and Spring Security.
- Endpoints can be protected by role using `@PreAuthorize`. Example: `/admin/hello` (ADMIN only), `/user/hello` (USER only).
- JWT now includes the user's role as a claim, always reflecting the value in the database.
- If a user tries to access an endpoint without the required role, the API returns HTTP 403 with a clear error message.
- Documentation updated with RBAC usage and error examples.
- Advanced auditing: dedicated logger for security events (user registration, login, refresh token rotation, invalid refresh token attempts), configured in `logback-spring.xml` and writing to `logs/audit.log` in JSON format with daily rotation and 90-day retention.
- Audited events include relevant details (username, email, role, user ID, involved tokens).
- Documentation updated with examples of audited events and audit logger configuration.

### Changed

- User and RefreshToken entities now use UUID as the primary key instead of Long. All related repositories, services, and API responses updated accordingly.
- Updated API documentation and examples to reflect UUID usage for IDs.

## [1.2.0] - 2025-09-14

### Added

- Support for refresh tokens with automatic rotation and invalidation.
- `/auth/V1/refresh` endpoint to renew access tokens using refresh tokens.
- Refresh tokens are invalidated after use (rotation) and can be revoked.
- Security improvements in the authentication flow.
- Input validation using standard Bean Validation annotations (`@NotBlank`, `@Size`, `@Email`, etc.) in models and controllers. Invalid requests now return detailed error messages per field.
- HTTP security headers added: X-Content-Type-Options, X-Frame-Options, Content-Security-Policy, Referrer-Policy, and Strict-Transport-Security (when using HTTPS). These headers help protect against XSS, clickjacking, and information leakage.
- Rate limiting (Bucket4j) on /login and /signup endpoints: max 5 requests per minute per IP. Exceeding the limit returns HTTP 429. Helps prevent brute-force attacks.
- Configurable CORS: allow all origins by default, or restrict using the ALLOWED_ORIGINS environment variable. Supports comma-separated list for multiple domains. Documented in README.
- Google reCAPTCHA v2 integration: login and signup endpoints now require a valid reCAPTCHA token in the request body (`recaptchaToken`).
- If the token is missing or invalid, the API returns HTTP 400 with a clear error message.
- Uses Google test keys by default for local/dev testing (any token accepted). In production, set the RECAPTCHA_SECRET environment variable.
- Updated API documentation and request examples to reflect the new required structure.

## [1.1.0] - 2025-08-27

### Added

- Production-ready logging: JSON format, daily rotation, 30-day retention, and error log separation.
- Global exception handler for uniform API error responses. Controller code simplified to leverage centralized error handling.
- Extensive unit and integration tests for UserService, RegistrationController, JwtUtil y UserRepository.
- Test configuration improvements: H2 database for tests, JWT testability, and security disabled for controller tests.

## [1.0.1] - 2025-08-26

### Changed

- JWT secret is now configurable via environment variable (`JWT_SECRET`) and must be at least 32 ASCII characters. Removed hardcoded/generate-on-the-fly secret for security.

## [1.0.0] - 2025-08-25

### Added

- Initial release with user registration and login.
- JWT authentication.
- PostgreSQL and HSQLDB support.
