# [1.2.0] - 2025-09-14

### Added

- Support for refresh tokens with automatic rotation and invalidation.
- `/auth/V1/refresh` endpoint to renew access tokens using refresh tokens.
- Refresh tokens are invalidated after use (rotation) and can be revoked.
- Security improvements in the authentication flow.
- Input validation using standard Bean Validation annotations (`@NotBlank`, `@Size`, `@Email`, etc.) in models and controllers. Invalid requests now return detailed error messages per field.
- HTTP security headers added: X-Content-Type-Options, X-Frame-Options, Content-Security-Policy, Referrer-Policy, and Strict-Transport-Security (when using HTTPS). These headers help protect against XSS, clickjacking, and information leakage.
- Rate limiting (Bucket4j) on /login and /signup endpoints: max 5 requests per minute per IP. Exceeding the limit returns HTTP 429. Helps prevent brute-force attacks.
- Configurable CORS: allow all origins by default, or restrict using the ALLOWED_ORIGINS environment variable. Supports comma-separated list for multiple domains. Documented in README.

# Changelog

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
