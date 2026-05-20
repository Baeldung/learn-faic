# Jira Lite — Functional Specification

What the Jira Lite system *does*. This document describes the domain, the public API, and the functional invariants the system has to uphold — *who can do what*, *what data is reachable*, *what the contract promises*.

Technical conventions (*how* the system is built — architecture, idioms, persistence patterns) live in `DEV-GUIDELINES.md`. The two documents are complementary: this one says a project member can read project data; the other says authorization is enforced at the service layer with `@PreAuthorize`. Neither implies the other.

`CLAUDE.md` is the always-loaded map and points here for functional depth.


## 1. Domain Model

The system manages projects with sprint-based task workflows, scoped to project members, with a queryable audit log of significant state changes.


### 1.1. User

Fields: `id`, `username`, `password` (hashed), `role`, `projects` (a user belongs to zero or more).

Roles are an enum: `ADMIN`, `MANAGER`, `DEVELOPER`, `VIEWER`. The role is the user's *system-wide* capability tier; project membership is orthogonal.

- **ADMIN** — manages everything, including role assignment.
- **MANAGER** — runs projects, manages sprint lifecycle, closes/reopens tasks.
- **DEVELOPER** — works on tasks within projects they're a member of.
- **VIEWER** — read-only within projects they're a member of.


### 1.2. Project

Fields: `id`, `name`, `description`, `members` (Users).

A project's data — sprints, tasks, comments, audit entries — is reachable only by its members. Membership is the access boundary; see [§3.1](#31-access-boundaries).


### 1.3. Sprint

Fields: `id`, `project`, `name`, `start date`, `end date`, `status`.

Sprint status is an enum: `PLANNED`, `ACTIVE`, `COMPLETED`. Lifecycle transitions: `PLANNED → ACTIVE → COMPLETED`. A sprint cannot return to a prior status.


### 1.4. Task

Fields: `id`, `project`, `title`, `description`, `status`, `priority`, `assignee` (User), `reporter` (User), `sprint` (optional — null means the task is in the project's backlog), `due date`.

Status is an enum: `OPEN`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`, `CLOSED`. Priority is an enum: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

**Workflow.** The forward path is `OPEN → IN_PROGRESS → IN_REVIEW → DONE → CLOSED`. Reopening from `CLOSED` back to `OPEN` is permitted, but only by `MANAGER` and `ADMIN`. Any other transition is invalid and must be rejected; see [§3.5](#35-workflow-integrity).


### 1.5. Comment

Fields: `id`, `task`, `author` (User), `body`, `created at`.

Any project member — including `VIEWER` — may add comments to tasks within their projects. Comments are not editable or deletable in this version of the API.


### 1.6. Audit Log

Records significant state changes across the system. An entry has: `id`, `eventType` (typed enum), `actor` (User), a reference to the affected entity (`entityType` + `entityId`), `timestamp`, and an optional `details` field for event-specific payload.

`eventType` is an enum, never a free-form string. The taxonomy is part of the public contract — see [§3.3](#33-contract-stability). The current vocabulary:

- `PROJECT_CREATED`
- `MEMBER_ADDED`, `MEMBER_REMOVED`
- `SPRINT_CREATED`, `SPRINT_STARTED`, `SPRINT_COMPLETED`
- `TASK_CREATED`, `TASK_UPDATED`, `TASK_STATUS_CHANGED`
- `COMMENT_ADDED`
- `USER_ROLE_CHANGED`

The audit log is queryable per project and per task (see [§2.7](#27-audit-log)).


## 2. API Surface

All endpoints are under the `/api/` prefix. Request and response bodies are JSON. HTTP status codes follow the standard semantics:

- `200 OK` — successful read.
- `201 Created` — successful creation; the response body carries the created resource.
- `204 No Content` — successful state mutation that returns no body.
- `400 Bad Request` — malformed body, invalid enum value, validation failure (missing required fields, wrong types).
- `401 Unauthorized` — missing or invalid credentials/JWT.
- `403 Forbidden` — authenticated but the role or membership doesn't permit the action.
- `404 Not Found` — resource does not exist *or* the caller is not permitted to know it exists (see [§3.1](#31-access-boundaries)).
- `409 Conflict` — invalid state transition (workflow violation, sprint already completed, etc.).

Error response bodies have a stable shape (carried by the framework's global exception handler, per `DEV-GUIDELINES.md` §6).


### 2.1. Auth

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user. Returns `201` with the created user (id, username, role). |
| `POST` | `/api/auth/login` | Authenticate. Returns `200` with a JWT in the response body. |

Registration creates a `DEVELOPER` unconditionally. The request body MUST NOT carry a `role` field; if present, it MUST be ignored. See [§3.2](#32-role-assignment-policy).


### 2.2. Users

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/users` | List users. ADMIN-only. |
| `PATCH` | `/api/users/{id}/role` | Change a user's role. ADMIN-only. Request body: `{ "role": "<RoleEnum>" }`. |

`PATCH /api/users/{id}/role` is the *only* API surface that mutates a user's role. There is no other path to role assignment through the public API.


### 2.3. Projects

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/projects` | Create a project. `MANAGER` or `ADMIN`. The creator is added as a member. |
| `GET` | `/api/projects` | List projects the caller is a member of. ADMIN sees all projects. |
| `GET` | `/api/projects/{id}` | Get a project. Members only. |
| `POST` | `/api/projects/{id}/members` | Add a member. Project `MANAGER` or `ADMIN`. Body: `{ "userId": <id> }`. |
| `DELETE` | `/api/projects/{id}/members/{userId}` | Remove a member. Project `MANAGER` or `ADMIN`. |

Project membership lookups don't require membership themselves — the response on a non-member access is `404`, not `403` (see [§3.1](#31-access-boundaries)).


### 2.4. Sprints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/sprints` | Create a sprint. Project `MANAGER` or `ADMIN`. Initial status: `PLANNED`. |
| `POST` | `/api/sprints/{id}/start` | Transition `PLANNED → ACTIVE`. Project `MANAGER` or `ADMIN`. `409` if status is not `PLANNED`. |
| `POST` | `/api/sprints/{id}/complete` | Transition `ACTIVE → COMPLETED`. Project `MANAGER` or `ADMIN`. `409` if status is not `ACTIVE`. |
| `GET` | `/api/sprints?projectId={id}` | List sprints for a project. Project members only. |


### 2.5. Tasks

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/tasks` | Create a task. Any project member. Initial status: `OPEN`. |
| `PUT` | `/api/tasks/{id}` | Update task fields (title, description, priority, assignee, sprint, due date — not status). Any project member. |
| `POST` | `/api/tasks/{id}/transition` | Transition task status. Body: `{ "status": "<TaskStatusEnum>" }`. See workflow rules in [§1.4](#14-task) and [§3.5](#35-workflow-integrity). |
| `GET` | `/api/tasks` | List tasks. Filterable by `status`, `assignee`, `priority`, `sprint`, `projectId`. Caller sees only tasks in projects they're members of. |
| `GET` | `/api/tasks/{id}` | Get a task. Project members only. |

The `assignee` and `reporter` fields on a task must reference users who are members of the same project. A request that names a non-member returns `400`.


### 2.6. Comments

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/tasks/{taskId}/comments` | Add a comment. Any project member, including `VIEWER`. |
| `GET` | `/api/tasks/{taskId}/comments` | List comments on a task. Project members only. |


### 2.7. Audit Log

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/projects/{id}/audit` | Audit entries scoped to a project. Project members only. |
| `GET` | `/api/tasks/{id}/audit` | Audit entries scoped to a task. Project members only. |

The audit log is read-only through the API. Entries are written by the system as a side-effect of state-changing operations; clients have no write surface.


## 3. Functional Invariants

The four invariants below are part of the contract. They're not implementation details — they describe properties the system has to uphold regardless of *how* it's built. Tests assert each one; deviations are bugs.


### 3.1. Access Boundaries

Project data is reachable only by project members. This applies uniformly across:

- **List endpoints** — `GET /api/projects`, `GET /api/sprints`, `GET /api/tasks`, `GET /api/tasks/{id}/comments`. The response is filtered to entities in projects the caller is a member of. A non-member sees an empty result for projects they don't belong to, never a leaked id, name, or count.
- **Per-resource endpoints** — `GET /api/projects/{id}`, `GET /api/tasks/{id}`, `GET /api/sprints?projectId=X`, audit-log readouts. Membership is checked before responding.
- **Write endpoints** — `POST /api/tasks`, comment creation, sprint actions. Membership is checked before any state changes.

**`ADMIN` is the only system-wide role that bypasses project-membership filtering.** No other role gets cross-project visibility, regardless of capability tier.

**Existence-leak rule.** A non-member querying a project they don't belong to receives `404`, not `403`. Returning `403` would confirm the project exists; that confirmation is itself an information leak. Same rule for tasks, sprints, audit entries: the resource is invisible to non-members.


### 3.2. Role Assignment Policy

A user's role is assigned by the system, not the client.

- **Registration creates a `DEVELOPER`.** The `POST /api/auth/register` request MAY carry a `role` field but the system MUST ignore it. The role on the created user is `DEVELOPER`, always.
- **Role changes go through `PATCH /api/users/{id}/role` only.** The endpoint is `ADMIN`-only. There is no other path through the public API for changing a user's role.
- **No self-promotion.** A user cannot grant themselves a role they don't have, by any path. `PATCH /api/users/{id}/role` is the only mutation, and `ADMIN` is the only caller; an `ADMIN` calling it on themselves to *demote* is permitted, but the system MUST refuse a self-demotion that would leave zero `ADMIN`s in the system (a `409`).

The discipline here is that *role-assignment is an authorization-policy concern*, separate from any individual endpoint's role check. A `DEVELOPER` who registers, then mutates their own user record, must not end up `ADMIN` — by any combination of API calls.


### 3.3. Contract Stability

Three things are part of the public contract and must not change silently between releases:

- **URL paths.** Existing endpoints keep their paths. New endpoints are added; old paths are not renamed, moved, or pluralized in place. `/api/tasks/{id}/transition` does not become `/api/tasks/{id}/transitions` (or vice versa) without a deprecation cycle.
- **Audit `eventType` enum values.** New events extend the enum (additive). Existing values do not get renamed (`USER_ROLE_CHANGED` does not become `ROLE_CHANGED`), and existing values are not removed. Consumers of the audit log read by event-type name; renames break them silently.
- **HTTP status codes per endpoint.** A 201 endpoint stays a 201 endpoint; a 204 endpoint stays a 204 endpoint. Validation-error shape is stable across endpoints.

The contract is not "what feels reasonable to the regenerated codebase" — it's the set of strings, codes, and shapes external callers depend on. Every change in this layer is a breaking change unless explicitly versioned.


### 3.4. Performance Contract

List endpoints return in a bounded number of database queries, independent of the result set size. This applies to:

- `GET /api/projects`
- `GET /api/sprints?projectId={id}`
- `GET /api/tasks` (with or without filters)
- `GET /api/tasks/{id}/comments`
- `GET /api/projects/{id}/audit`, `GET /api/tasks/{id}/audit`

Per-row fan-out (the N+1 pattern) is a contract violation, not a performance optimization. Tests assert query counts on each list endpoint; the assertion is the protection. Implementation mechanism (`@EntityGraph`, `JOIN FETCH`, response-shape choice) is technical-axis territory and lives in `DEV-GUIDELINES.md` §4 — but the *contract* (bounded queries; tested) is here.

The specific query-count bound for each endpoint is a test-level detail. The spec asserts the *category* (bounded, not linear in row count); the test pins the number.


### 3.5. Workflow Integrity

The task workflow in [§1.4](#14-task) is enforced as a state machine, not as advisory text:

- Forward transitions follow `OPEN → IN_PROGRESS → IN_REVIEW → DONE → CLOSED` only. Skipping states (`OPEN → DONE`, `IN_PROGRESS → CLOSED`) is rejected with `409`.
- Backward transitions are not permitted on the forward path — once `IN_PROGRESS`, a task cannot return to `OPEN` except via the `CLOSED → OPEN` reopen path.
- `CLOSED → OPEN` (reopen) is permitted, restricted to `MANAGER` and `ADMIN`.
- Any transition to `CLOSED` other than `DONE → CLOSED` is rejected.
- `MANAGER` and `ADMIN` are the only roles that may transition a task into `CLOSED`.

Sprint lifecycle ([§1.3](#13-sprint)) is also a state machine: `PLANNED → ACTIVE → COMPLETED` is the only path, and each transition is a dedicated endpoint with a dedicated `409` for wrong-state attempts.

The integrity rule is that workflow violations come back as `409 Conflict`, not as `400 Bad Request` and not as a silent no-op. The status-code distinction matters because workflow violations are state-dependent — the same request is valid in one state and invalid in another.


## 4. Build Expectations

The project builds with Maven. Two commands are part of the contract and must work as independent standalone invocations:

- `mvn test` — runs the test suite.
- `mvn checkstyle:check` — runs the style baseline against `src/main`.

Neither command is bound to the other's build phase. Checkstyle reads its configuration from `checkstyle.xml` at the project root and applies it to `src/main` only — tests are not in the Checkstyle scope.
