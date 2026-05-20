# Jira Lite

A lightweight project management REST API built with Spring Boot 3.3.x, Spring Security (JWT), Spring Data JPA, and H2.

## Running

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

## Default Admin

| Field    | Value      |
|----------|------------|
| username | `admin`    |
| password | `admin123` |

Login to get a JWT token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Use the returned `token` as `Authorization: Bearer <token>` on all subsequent requests.

## H2 Console

Available at `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:jiralite`
- Username: `sa`
- Password: *(empty)*

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, get JWT |
| GET | `/api/users` | List users |
| PUT | `/api/users/{id}/role` | Change user role (ADMIN only) |
| POST | `/api/projects` | Create project (ADMIN/MANAGER) |
| GET | `/api/projects` | List projects |
| POST | `/api/projects/{id}/members` | Add project member |
| DELETE | `/api/projects/{id}/members/{userId}` | Remove member |
| POST | `/api/projects/{projectId}/sprints` | Create sprint |
| POST | `/api/projects/{projectId}/sprints/{id}/start` | Start sprint |
| POST | `/api/projects/{projectId}/sprints/{id}/complete` | Complete sprint |
| POST | `/api/projects/{projectId}/tasks` | Create task |
| GET | `/api/projects/{projectId}/tasks` | List tasks (filterable) |
| PUT | `/api/projects/{projectId}/tasks/{id}` | Update task |
| POST | `/api/projects/{projectId}/tasks/{id}/transition` | Transition task status |
| POST | `/api/projects/{projectId}/tasks/{taskId}/comments` | Add comment |
| GET | `/api/projects/{projectId}/audit` | Project audit log |
| GET | `/api/projects/{projectId}/tasks/{taskId}/audit` | Task audit log |

## Roles

- **ADMIN** — full access
- **MANAGER** — manage their projects, run sprints, close tasks
- **DEVELOPER** — create/update tasks and comments (cannot close tasks)
- **VIEWER** — read-only

## Task Status Transitions

```
OPEN → IN_PROGRESS → IN_REVIEW → DONE → CLOSED
                                         ↓
                                        OPEN  (reopen, MANAGER/ADMIN only)
```
