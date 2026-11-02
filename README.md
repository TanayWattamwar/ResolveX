# ResolveX — Enterprise IT Service Management Platform

A Java 21 + Spring Boot + React + MySQL full-stack service desk application. Employees create support tickets; engineers work their assigned queue; admins manage the workspace and team.

## Included

- JWT authentication and BCrypt password hashing
- Employee, Engineer and Admin roles
- Ticket creation, listing, priority and status workflow
- Engineer assignment and admin team provisioning
- Ticket activity/audit history (API)
- Resolution notes, root-cause capture, resolver identity and resolution timestamp
- Server-side authorization: only admins or the assigned engineer can update a ticket; engineers cannot reassign tickets or change priority/SLA deadlines
- Dashboard counters, critical ticket and SLA overdue indicators
- Optional Gmail/SMTP notifications (disabled by default)
- Swagger/OpenAPI documentation
- Docker Compose MySQL + backend setup
- Responsive React dashboard

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+
- MySQL 8 (or Docker Desktop)

## Quick start (recommended: Docker for database + local app)

1. Install/start Docker Desktop.
2. From the project root, copy `.env.example` to `.env`.
3. Edit `.env`: set a strong `DB_PASSWORD`, a random `JWT_SECRET` (at least 32 characters), and a unique `BOOTSTRAP_ADMIN_PASSWORD` (at least 12 characters). Keep `.env` private.
4. Start MySQL and backend:

   ```bash
   docker compose up --build -d
   ```

5. In a second terminal:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. Open http://localhost:5173 and sign in using the bootstrap admin email/password from `.env`.
7. API docs: http://localhost:8080/swagger-ui.html

The backend creates the initial admin only when `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` are supplied and that email does not already exist. Changing these variables later does not reset an existing account.

## Run without Docker

1. Create a MySQL database named `resolvex`.
2. Set environment variables before starting the backend:

   ```bash
   DB_URL=jdbc:mysql://localhost:3306/resolvex?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   DB_USERNAME=root
   DB_PASSWORD=your_mysql_password
   JWT_SECRET=your-random-secret-at-least-32-characters-long
   BOOTSTRAP_ADMIN_EMAIL=admin@example.com
   BOOTSTRAP_ADMIN_PASSWORD=your-unique-password-at-least-12-chars
   ```

3. Start backend:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. Start frontend in another terminal:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Roles and first use

- Public registration creates an `EMPLOYEE` account only.
- The bootstrap account is `ADMIN`.
- Sign in as admin, open **Team**, and create Engineer accounts (or additional admins).
- Employees create tickets. Admins see all tickets. Engineers see tickets assigned to them and can update the status of their assigned tickets. Only admins can assign engineers or change priority and SLA deadlines. Resolving or closing a ticket requires resolution notes; root cause is optional.
- Assign a ticket to an engineer by calling `PATCH /api/tickets/{id}` with `{ "assigneeId": 2 }` as an admin. The API docs show all request schemas.

## Optional Gmail notifications

Set `MAIL_ENABLED=true`, `MAIL_USERNAME` to your Gmail address, and `MAIL_PASSWORD` to a Gmail App Password in your private environment. Do not put credentials in JavaScript, source files, screenshots, or Git commits. Gmail app passwords require 2-Step Verification. Email delivery is best-effort and does not block ticket operations.

## Main API routes

| Method | Route | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register employee |
| POST | `/api/auth/login` | Public | Login / receive JWT |
| GET | `/api/tickets` | Authenticated | Role-scoped ticket list |
| POST | `/api/tickets` | Authenticated | Create ticket |
| PATCH | `/api/tickets/{id}` | Admin or assigned Engineer | Admin can update assignment, priority and due date; assigned engineer can update status; resolution notes required for resolve/close |
| GET | `/api/tickets/{id}/history` | Authorized ticket participants | Audit history |
| GET | `/api/dashboard` | Authenticated | Role-scoped metrics |
| GET | `/api/engineers` | Engineer/Admin | Engineer directory |
| POST | `/api/admin/users` | Admin | Create engineer/admin |

Send `Authorization: Bearer <token>` for protected API calls.

## Resolution workflow

When an engineer or admin changes a ticket to `RESOLVED` or `CLOSED`, the frontend prompts for required resolution notes and optional root cause. The API rejects resolution/closure requests without notes. The ticket stores the notes, root cause, resolver and resolution timestamp, and records the action in ticket history. Hibernate `ddl-auto=update` adds the new nullable columns to an existing local database; back up the database before upgrading.

## Before production deployment

- Replace every development secret with strong platform environment variables.
- Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, bootstrap admin values, and optional mail values in the hosting provider.
- Set `CORS_ALLOWED_ORIGINS` to the exact deployed frontend origin(s), not `*`.
- Use managed MySQL with backups, HTTPS, a non-root DB account, and a production migration strategy (Flyway recommended; avoid `ddl-auto=update` for production).
- Disable/remove bootstrap admin environment variables after the first admin is created.
- Review logs, rate limiting, account lockout, password reset, email verification, privacy policy and retention requirements before public launch.

## Notes

This is a runnable placement-project starter, not a certified enterprise ITSM product. The current implementation includes the core workflow and security baseline. Features such as file attachments, comments, password reset, SLA escalation scheduler, advanced search/pagination, and comprehensive automated tests are suitable next iterations. Run it locally and validate your configuration before deployment.
