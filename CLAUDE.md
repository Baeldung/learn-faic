# Jira Lite — Project Context

For detailed technical guidelines, see `DEV-GUIDELINES.md`.
For the functional spec, see `SPEC.md`.

## Tech Stack

- Java 21
- Spring Boot 3.3.x with Maven
- H2 (in-memory, for local dev)
- Spring Security + JWT for authentication
- Spring Data JPA for persistence

Use only what's listed above. Don't pull in Lombok, MapStruct, or any other code-generation or extra libraries — plain Java with what comes from these starters.

## Layering

Three-layer separation: controllers handle HTTP; services hold business logic and enforce authorization; repositories do data access only. Detailed conventions (transaction boundaries, what can't live in each layer, package structure) live in `DEV-GUIDELINES.md`.

## Testing Approach

Test through the public surface, not internals. Pyramid shape, isolation, and the positive+negative pair convention live in `DEV-GUIDELINES.md` § 7.

## Code Style

Checkstyle is the style baseline — satisfy its rules. The configuration lives in `checkstyle.xml`. Apply it to `src/main` only; tests aren't part of the Checkstyle scope.

Don't introduce any suppressions, and don't modify `checkstyle.xml`. Refactor the code to satisfy the rules.

## Workflow

After each change to the code:

- Run `mvn test` — fix any failing tests before moving on.
- Run `mvn checkstyle:check` — fix any style violations before moving on.

Don't continue with a failing build.

## Sub-Agents

When the build has bulky, bounded sections (e.g., the full API surface), dispatch the work to a sub-agent rather than implementing inline. The orchestrator plans and integrates; the sub-agent executes.

## Models

Use Opus for planning, decomposition, and judgment calls. Use Sonnet for mechanical execution where the decisions have already been made.

This applies at every agent layer. When the main agent finishes planning and starts executing, switch to Sonnet. Sub-agents dispatched for execution work run on Sonnet from the start.
