# Development Guidelines

Technical conventions for the Jira Lite codebase. This document describes *how* the system is built — architecture, idioms, patterns. Functional requirements (*what* the system does) live in `SPEC.md`.

`CLAUDE.md` summarizes the most load-bearing rules below and points here for depth. When `CLAUDE.md` and this document say the same thing, this document is the source of truth; `CLAUDE.md` is the map.


## 1. Architecture

Three-layer separation, strict.

- **Controllers** translate HTTP into service calls. They parse and validate the request, call exactly one service method, and shape the response. Controllers contain no business logic, no authorization checks, no filtering, no querying, no transaction management.
- **Services** hold business logic. Authorization is enforced here. Filtering and search logic live here. Transaction boundaries are here (`@Transactional` on service methods, not controllers or repositories). Services may call other services, but the call graph is acyclic — circular service dependencies are a smell that something is misplaced.
- **Repositories** do data access only. They return what services ask for. Repositories contain no business logic and no cross-aggregate queries that obscure the access pattern; complex queries live in `@Query`-annotated methods or in JPA `Specification`s composed inside the service.

**Layer dependencies are one-way (top-down).** Controllers depend on services — never directly on repositories. Services depend on repositories. The upward direction never holds: services don't import controllers; repositories don't import services or controllers. Tools like ArchUnit can enforce this mechanically; absent that, the discipline is the convention. A violation is a structural smell even when it compiles.

**Package structure: by feature, not by layer.** Each aggregate (User, Project, Sprint, Task, Comment, AuditLog) gets its own package containing its controller, service, repository, entity, and DTOs. Cross-cutting concerns (security config, exception handling, JWT plumbing) live in their own packages (`config`, `security`, `web`).

Why by-feature: a single aggregate's code is co-located, so changes to *one* aggregate touch *one* package. By-layer packaging (`controller/`, `service/`, `repository/`) scatters every aggregate's code across the codebase and makes feature-level changes touch every layer's package.


## 2. Dependency Injection

**Constructor injection with `final` fields** — Spring's official guidance since 2018; dependencies can't change after construction, the class can't be half-instantiated, and tests construct it directly without a Spring context.

Don't use `@Autowired` on private fields or setters. Don't use Lombok's `@RequiredArgsConstructor` either — Lombok is excluded per `CLAUDE.md`, so write the constructor by hand.

If a class needs more than 5–6 dependencies, that's a smell that it's doing too much — extract a collaborator. Don't fix it by switching to field injection.


## 3. Authorization

**Use Spring Security's `@PreAuthorize` on service methods** (with `@EnableMethodSecurity` on the security config). It's the framework default — declarative, well-known, and the simplest path. Don't write custom infrastructure when the framework already provides it; reach for helpers only where the annotation is genuinely awkward (see *When to use helpers* below).

```java
// Yes — annotation on the service method
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public Project createProject(CreateProjectRequest request) {
    // ... business logic
}

// No — annotation on the controller
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@PostMapping
public ProjectResponse createProject(@RequestBody CreateProjectRequest request) {
    return projectService.createProject(request);
}
```

**Two principles, both load-bearing:**

1. **Every service method that needs authorization carries the annotation, not just the ones a test happens to cover.** A test for one site doesn't certify the others. The convention is *uniformity* — if a service entry point can change state or read project-scoped data, the check is on the method. No exceptions for "trivial" operations.

2. **Auth lives at the service layer, not the controller.** Service methods are the unit of business operation; placing the check there means service-to-service calls are also covered, and the rule sits at the same level as the logic it protects. Controllers stay thin (per §1).

**When to use helpers.** Reach for a typed helper method only when SpEL gets unwieldy — typically *parameter-driven* checks (project membership, ownership) and *composed predicates*. SpEL like `@PreAuthorize("@projectService.isMember(#projectId, principal.id) and hasRole('ADMIN')")` does the same job as Java but reads worse; pick the form that's clearer. For plain role checks, the annotation wins.


## 4. Persistence

### 4.1. Field references — typed, not stringly

**Compile-checked field references. No string-constant field names.** Either the JPA static metamodel or a `@Query`-annotated method works — both produce references that build-time generation or startup validation will catch when a field is renamed.

```java
// Yes — metamodel (for dynamic predicate composition)
return taskRepository.findAll((root, query, cb) ->
    cb.equal(root.get(Task_.project).get(Project_.id), projectId));

// Yes — @Query (for static queries)
@Query("select t from Task t where t.project.id = :projectId")
List<Task> findByProjectId(@Param("projectId") Long projectId);

// No — stringly typed
private static final String FIELD_PROJECT = "project";
private static final String FIELD_ID = "id";
return taskRepository.findAll((root, query, cb) ->
    cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId));
```

The metamodel (`Task_`, `Project_`, etc.) is generated at build time by the JPA static metamodel processor; `@Query` JPQL is validated by Spring Data at startup. A renamed field surfaces as a compile or startup error, not a runtime exception.

**When to pick which.** Metamodel for dynamic predicates (optional filters, runtime sort orders, search forms where the where-clause is composed at request time). `@Query` for queries with a fixed shape (list endpoints, single lookup methods). The failure mode to avoid is Specification-with-string-fields — neither dynamic nor compile-checked.

### 4.2. Fetch strategy — explicit, not accidental

**Default every `@ManyToOne` and `@OneToOne` to `LAZY`.**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "assignee_id")
private User assignee;
```

Eager fetching by default is how the N+1 query problem hides. Lazy by default surfaces the question *"how do I want to load this association?"* at every read site.

**For list endpoints, declare the fetch explicitly** with `@EntityGraph` on the repository method, or `JOIN FETCH` in the JPQL:

```java
@EntityGraph(attributePaths = {"assignee", "reporter", "sprint"})
List<Task> findByProjectId(Long projectId);
```

**No association traversal in DTOs without an explicit fetch.** A DTO that calls `task.getAssignee().getUsername()` requires the assignee to have been fetched; if it wasn't, the lazy proxy will trigger a query *per task*, producing the N+1 fan-out. Either fetch the association explicitly, or expose an id-only reference in the DTO (see §5).

### 4.3. Repository conventions

- Repository methods return `Optional<T>` for single-record lookups, never `null`.
- Custom queries use `@Query` with named parameters (`:projectId`, not `?1`).
- Pagination uses `Pageable` parameter and `Page<T>` return type, never custom offset/limit.
- No JPA `find*` derived-name methods longer than three predicates — past three, switch to `@Query`. Long derived names are unreadable and brittle.


## 5. DTOs and the Response Boundary

**Entities never leak across the controller boundary.** Controllers return DTOs (`TaskResponse`, `ProjectResponse`, etc.), never `Task` or `Project` directly.

**Naming:**
- `<Aggregate>Request` for input DTOs (`CreateTaskRequest`, `UpdateTaskRequest`).
- `<Aggregate>Response` for output DTOs (`TaskResponse`, `ProjectResponse`).
- *Not* `<Aggregate>DTO` — the request/response distinction matters and the suffix encodes it.

### 5.1. Id-only references on response DTOs

**Response DTOs expose ids for associations, not denormalized name/title fields.**

```java
// Yes
public record TaskResponse(
        Long id,
        String title,
        TaskStatus status,
        Long assigneeId,    // id only
        Long reporterId,    // id only
        Long sprintId       // id only (nullable for backlog)
) { }

// No — denormalized fields force a fetch on every list access
public record TaskResponse(
        Long id,
        String title,
        TaskStatus status,
        String assigneeUsername,  // forces lazy proxy init
        String reporterUsername,
        String sprintName
) { }
```

**Why:** name/title fields on response DTOs are convenient for the client but turn every list endpoint into an N+1 fan-out. The protection-of-the-protection is fragile — one developer adds `assigneeUsername` to the DTO without realizing the cost.

If the client genuinely needs names alongside ids (e.g., a dashboard view), expose them through a *separate* endpoint that joins explicitly via `@EntityGraph`, or accept the cost and document it. The decision is explicit, not accidental.

### 5.2. DTO conversion

DTO conversion lives in a mapper class (e.g., `TaskMapper`) called from the service layer, *not* in the controller and *not* in a static method on the DTO. Controllers don't see entities; mappers don't see HTTP.

```java
@Component
public class TaskMapper {
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getAssignee() == null ? null : task.getAssignee().getId(),
                task.getReporter().getId(),
                task.getSprint() == null ? null : task.getSprint().getId());
    }
}
```


## 6. Error Handling

### 6.1. Domain-specific exceptions

**Throw domain exceptions, not generic ones.** Each translates to a specific HTTP status.

| Exception | HTTP status | When |
|-----------|-------------|------|
| `NotFoundException` | 404 | Entity lookup returns empty |
| `ForbiddenException` | 403 | Authorization check fails |
| `UnauthorizedException` | 401 | No valid auth credentials |
| `ConflictException` | 409 | State transition or unique-constraint conflict |
| `ValidationException` | 400 | Domain-rule violation beyond bean-validation |

`IllegalArgumentException`, `IllegalStateException`, and bare `RuntimeException` are not thrown from service code. If something is wrong, it has a domain meaning — name it.

### 6.2. Single global handler

One `@RestControllerAdvice` translates the domain exceptions above into HTTP responses. Each handler method targets one exception type; no generic `Exception.class` fallback that swallows everything into a 500.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
    }

    // ... one per domain exception
}
```

**No catch-all `@ExceptionHandler(Exception.class)`.** Unhandled exceptions bubble to Spring's default handler and produce a 500 — that's what 500 means. Catching everything and returning a synthetic 500 hides bugs.

### 6.3. Stable error response shape

Error responses have a stable shape so clients can rely on it:

```json
{
  "message": "Task 42 not found"
}
```

If validation errors expose multiple fields, extend the shape — but extend it once and uniformly, not per-handler.

### 6.4. No 401-vs-403 conflation

Spring Security's defaults can blur the boundary. Configure the chain so a missing or invalid token produces 401 (no or bad credentials), and a role or membership check failure produces 403 (authenticated but not allowed).


## 7. Testing

The suite has a shape; the shape is not accidental. A flat suite — every test built the same way — is the signal that the test ask was *"write tests"* without naming what kind.

### 7.1. Pyramid shape

- **Unit tests** — services tested in isolation, collaborators stubbed or substituted. Cover business rules and branches at the smallest scope they live in. Most numerous; cheapest to run.
- **Slice tests** — `@DataJpaTest` for repositories, `@WebMvcTest` for controllers. Verify each layer's contract with Spring's machinery (query parsing, request mapping, JSON binding) without spinning up the full context.
- **Integration tests** — `@SpringBootTest` + MockMvc, full HTTP-to-DB. One per feature happy path plus cross-cutting concerns (security, transactions, exception translation). Far fewer than unit/slice — they're slow.
- **End-to-end tests** — only where contract verification needs the full deployed stack (real DB, real HTTP). Slow, brittle, scarce.

A suite that's *only* `@SpringBootTest` MockMvc is flat: every test pays the integration startup cost, and logic-level branches go uncovered because exercising one branch demands an HTTP setup. The flatness is a smell, not a strategy.

### 7.2. Positive and negative pairs

**Every rule worth writing has at least one positive test (the rule allows what it should) and one negative test (the rule blocks what it should).** Authorization (§3) is the prototypical case; the same pattern applies to validation (valid input persists, invalid returns 400), state transitions (legal succeeds, illegal returns 409), and any other rule with a *yes* and a *no* branch.

The negative test catches *"the rule is in the doc but not in the code"* — the most common silent failure mode. Without it, the rule is documentation, not behavior.

### 7.3. Test isolation

- Tests don't depend on order. Each starts from a known state and ends without leaking state to the next.
- Shared fixtures (`@Sql`, builders, factories) are explicit. A test that secretly relies on a previous test's writes is broken even when it passes.
- Database state resets between tests — `@Transactional` with rollback at the test-class level, or explicit cleanup. `@DirtiesContext` only when the test genuinely mutates the application context.

### 7.4. Test naming

- Method names read as sentences naming the claim: `nonMemberCannotReadProjectAuditLog`, `transitionFromClosedToInProgressFails`. Not `testAuditLog1`, not `testCase42`.
- A reader scanning the test class should understand each test's assertion from the name alone, without reading the body. The name *is* the spec the test enforces.


## 8. Naming and Structure

Conventions Checkstyle can't capture but readers expect.

### 8.1. Class naming

- **Service classes:** `<Aggregate>Service`.
- **Controllers:** `<Aggregate>Controller`.
- **Repositories:** `<Aggregate>Repository`.
- **Entities:** `<Aggregate>` (no suffix).
- **DTOs:** `<Aggregate><Verb>Request` / `<Aggregate>Response` (see §5).
- **Mappers:** `<Aggregate>Mapper`.
- **Helpers:** descriptive name ending in role (`RoleChecker`, `JwtTokenProvider`, `PasswordEncoder`).

### 8.2. Method naming

- Service methods read like English: `createProject`, `listTasksByProject`, `transitionTask`. No `doX`, `processY`, `handleZ` — those names hide the action.
- Repository methods use Spring Data conventions when derived (`findById`, `findByProjectIdAndStatus`); past three predicates, switch to `@Query` with a named query method (`findVisibleTasksFor`, not `findByProjectIdAndStatusInAndAssigneeIdOrReporterId`).
- Mapper methods are `toResponse`, `toEntity`, `toRequest` — verb-based, with the target shape in the name.

### 8.3. Package layout (recap)

```
com.baeldung.jiralite/
├── user/           # User aggregate (controller, service, repository, entity, DTOs)
├── project/        # Project aggregate
├── sprint/         # Sprint aggregate
├── task/           # Task aggregate
├── comment/        # Comment aggregate
├── audit/          # AuditLog aggregate
├── auth/           # Authentication endpoints + JWT plumbing
├── security/       # Spring Security config, RoleChecker, filters
├── web/            # @RestControllerAdvice, error response shape
└── config/         # @Configuration classes (CORS, JSON, etc.)
```

Cross-cutting concerns (security, web, config, auth) get their own packages; aggregates each get one. No `service/`, `controller/`, `repository/` top-level packages.


## 9. What's Out of Scope

This document deliberately does not cover:

- **Functional requirements** (what endpoints exist, who can do what, what the workflow is) — see `SPEC.md`.
- **Operational concerns** (deployment, monitoring, alerting, log aggregation) — separate doc when the project reaches operational maturity.
- **Performance tuning beyond N+1 avoidance** — query optimization, caching, async processing — separate doc when the project hits a performance ceiling.
- **Frontend conventions** — out of scope for a backend codebase.

When this document and another say the same thing, this document is the source for *technical-how* questions and `SPEC.md` is the source for *functional-what* questions. `CLAUDE.md` summarizes both at map-level.
