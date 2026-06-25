# SafeMine — Incident & Safety Reporting System

A full-stack mine safety incident reporting platform built with Spring Boot + Angular.

## Project Structure

```
incident-safety-reporting-system/
├── logo.svg                    # SafeMine brand logo
├── backend/                    # Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/mining/safety/
│       ├── entity/             # JPA entities (User, Incident, CorrectiveAction)
│       ├── repository/         # Spring Data JPA repositories
│       ├── service/            # Business logic
│       ├── controller/         # REST controllers
│       ├── dto/                # Request/response DTOs
│       ├── security/           # JWT auth filter & provider
│       ├── config/             # Security configuration
│       └── enums/              # Role, IncidentType, Severity, Status
└── frontend/                   # Angular 17 SPA
    └── src/app/
        ├── pages/
        │   ├── login/          # Login page with demo credentials
        │   ├── dashboard/      # Safety dashboard with stats
        │   ├── incidents/      # Incident list, form, detail
        │   └── reports/        # Analytics & compliance reports
        ├── core/
        │   ├── auth/           # AuthService + AuthGuard
        │   ├── interceptors/   # JWT interceptor
        │   └── services/       # IncidentService
        ├── models/             # TypeScript interfaces
        └── shared/             # Layout component
```

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

API runs on `http://localhost:8080`

**Default users (auto-seeded on first run):**
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@safemine.co.za | Admin@123 |
| Safety Officer | safety@safemine.co.za | Safety@123 |
| Worker | worker@safemine.co.za | Worker@123 |

**H2 Console:** http://localhost:8080/h2-console

### Frontend

```bash
cd frontend
npm install
ng serve
```

App runs on `http://localhost:4200`

## Key Features

- **Incident Reporting** — Workers report incidents with type, severity, location, injury details
- **Dashboard** — Real-time stats (open incidents, critical, injuries, overdue actions)
- **Status Workflow** — Reported → Under Investigation → Corrective Action → Closed → DMR Notified
- **Root Cause Analysis** — Safety officers add root cause to closed incidents
- **Reports** — Incident breakdown by type and severity with visual charts
- **Role-based Access** — Worker, Supervisor, Safety Officer, Manager, Admin
- **JWT Security** — Spring Security + JWT token authentication
- **DMR Compliance** — Track DMR notification status per incident

## Production Deployment

1. Switch `application.properties` to PostgreSQL datasource
2. Update `app.jwt.secret` to a strong secret
3. Update CORS `allowed-origins` to your frontend domain
4. Build: `mvn clean package` and `ng build --configuration production`
