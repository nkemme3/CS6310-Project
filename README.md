# Power Grid

CS6310 Software Architecture & Design, Spring 2026, Group 30. Power grid simulation with a Spring Boot backend and a React frontend.

## Prerequisites

- JDK 17+
- Node.js 20+
- Docker (for containerized runs)

## Local development

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend starts on http://localhost:8080. Health check: `GET /api/health`.

Run tests:

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on http://localhost:5173 and proxies `/api` to the backend.

## Docker

From the repo root:

```bash
docker compose -f docker/docker-compose.yml up --build
```

- Frontend: http://localhost:8081
- Backend:  http://localhost:8080

## Layout

```
backend/    Spring Boot app (Java 17, Maven)
frontend/   React + Vite
docker/     Dockerfiles + compose
docs/       Design diagrams
```
