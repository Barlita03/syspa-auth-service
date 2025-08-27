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
