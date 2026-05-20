# Jira Lite — Project Context

## Tech Stack

- Java 21
- Spring Boot 3.3.x with Maven
- H2 (in-memory, for local dev)
- Spring Security + JWT for authentication
- Spring Data JPA for persistence

Use only what's listed above. Don't pull in Lombok, MapStruct, or any other code-generation or extra libraries — plain Java with what comes from these starters.

## Layering

Three layers, with strict separation:

- **Controllers** parse and validate requests, call services, return responses. No business logic, no authorization checks, no filtering logic in controllers.
- **Services** hold business logic, enforce authorization, and own transaction boundaries. Use the same approach for role checks across all services.
- **Repositories** do data access only.
