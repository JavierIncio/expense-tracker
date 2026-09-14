# Expense Tracker

Expense Tracker is a personal finance management web application for tracking income, expenses, categories, and monthly budgets.

The project is designed as a practical learning project focused on strengthening **Java and Spring Boot fundamentals** while applying a small **microservices architecture**.

The goal is to build a solid, maintainable product without introducing unnecessary architectural complexity.

---

## Features

### Authentication

- User registration and login
- JWT-based authentication
- Refresh tokens
- Protected API endpoints

### Transactions

- Create income and expenses
- Edit and delete transactions
- Categorize transactions
- Filter transactions by:
  - Type
  - Category
  - Date range

- Pagination and sorting

### Categories

- Create custom categories
- Edit and delete categories
- Categorize income and expenses

### Budgets

- Create monthly budgets
- Assign budgets to categories
- Compare budgeted and actual spending

### Dashboard

- Monthly income
- Monthly expenses
- Current balance
- Expenses by category
- Budget vs. actual spending

### Notifications

- Notifications for relevant events such as exceeded budgets
- Asynchronous communication between services

---

## Architecture

The application uses a small microservices architecture:

```text
                         Angular
                            │
                            ▼
                     ┌─────────────┐
                     │   Gateway   │
                     │    :8080    │
                     └──────┬──────┘
                            │
                 ┌──────────┴──────────┐
                 ▼                     ▼
        ┌─────────────────┐   ┌─────────────────┐
        │ Identity Service│   │ Expense Service │
        │      :8081      │   │      :8082      │
        └────────┬────────┘   └────────┬────────┘
                 │                     │
                 ▼                     ▼
           PostgreSQL             PostgreSQL

                         Optional
                            │
                            ▼
                    ┌─────────────┐
                    │    RabbitMQ │
                    └──────┬──────┘
                           │
                           ▼
                   Notification Service
```

### Services

#### Identity Service

Responsible for:

- User registration
- Authentication
- JWT access tokens
- Refresh tokens
- User identity

#### Expense Service

Responsible for:

- Transactions
- Categories
- Budgets
- Monthly summaries

#### API Gateway

Provides a single entry point for the frontend and routes requests to the appropriate service.

#### Notification Service

Responsible for consuming selected domain events and creating userId notifications.

This service is intentionally kept small to demonstrate asynchronous communication without making the application unnecessarily complex.

---

## Domain

The main domain entities are:

```text
User
 └── managed by Identity Service

Transaction
 ├── id
 ├── userId
 ├── type
 ├── amount
 ├── category
 ├── description
 ├── date
 └── createdAt

Category
 ├── id
 ├── userId
 ├── name
 └── type

Budget
 ├── id
 ├── userId
 ├── categoryId
 ├── year
 ├── month
 └── amount

Notification
 ├── id
 ├── userId
 ├── message
 ├── read
 └── createdAt
```

---

## Technology Stack

### Backend

- Java 25
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven

### Frontend

- Angular 21
- TypeScript
- Tailwind CSS

### Architecture & Infrastructure

- REST
- Microservices
- Spring Cloud Gateway
- RabbitMQ
- Docker
- Docker Compose

### Testing

- JUnit
- Mockito
- Spring Boot Test
- Testcontainers

### CI/CD

- GitHub Actions

---

## API

Main API endpoints:

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

### Transactions

```http
POST   /api/transactions
GET    /api/transactions
GET    /api/transactions/{id}
PUT    /api/transactions/{id}
DELETE /api/transactions/{id}
```

Example filters:

```http
GET /api/transactions?type=EXPENSE
GET /api/transactions?category=FOOD
GET /api/transactions?from=2026-09-01&to=2026-09-30
```

### Categories

```http
GET    /api/categories
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

### Budgets

```http
GET    /api/budgets
POST   /api/budgets
PUT    /api/budgets/{id}
DELETE /api/budgets/{id}
```

### Summary

```http
GET /api/summary/monthly?year=2026&month=9
```

---

## Event-Driven Communication

Selected domain events are published through RabbitMQ.

Example:

```text
Expense Service
      │
      │ BudgetExceeded
      ▼
   RabbitMQ
      │
      ▼
Notification Service
```

Initial events:

```text
TransactionCreated
TransactionDeleted
BudgetExceeded
```

The messaging layer is intentionally limited to a small number of meaningful use cases.

---

## Project Structure

```text
expense-tracker/
│
├── frontend/                   # planned
│
├── gateway/
│
├── identity-service/
│
├── expense-service/            # planned
│
├── notification-service/       # planned
│
├── infrastructure/
│   └── docker-compose.yaml
│
├── .github/
│   └── workflows/
│       └── ci.yaml
│
└── README.md
```

---

## Development

### Requirements

- Java 25
- Maven
- Node.js
- Angular CLI
- Docker
- Docker Compose

### Running the infrastructure

```bash
docker compose -f infrastructure/docker-compose.yaml --env-file .env up -d
```

### Running the backend

From the repository root, each service can be started independently:

```bash
./mvnw -pl identity-service spring-boot:run
./mvnw -pl gateway spring-boot:run
```

Services resolve their configuration from the environment or from the `.env` file at the repository root (loaded via `spring.config.import`). Run them from the repository root because the `.env` import is resolved relative to the working directory.

#### Local configuration

Create the environment file from the template if it does not exist:

```bash
cp .env.example .env
```

The template also includes the PostgreSQL credentials used by Docker Compose. For authentication, generate a JWT secret — any valid base64 string with at least 256 bits:

```bash
openssl rand -base64 48
```

and set it in `.env` as `APP_JWT_SECRET`.

> In Docker Compose the containers receive these values directly via `env_file`/`environment`, so the `.env` import is not used there.

### Running the frontend

```bash
npm install
npm start
```

The application will be available at:

```text
http://localhost:4200
```

The API Gateway will be available at:

```text
http://localhost:8080
```

---

## Testing

Run the backend tests with:

```bash
./mvnw test
```

Integration tests use Testcontainers to run the required infrastructure in isolated containers.

---

## Development Goals

This project is primarily focused on practicing and consolidating backend development fundamentals.

The main learning goals are:

- Java fundamentals
- Object-oriented design
- Clean separation of responsibilities
- Spring Boot
- REST API design
- Spring Security
- JWT authentication
- JPA and Hibernate
- PostgreSQL
- Database migrations
- Validation and error handling
- Unit and integration testing
- Testcontainers
- Microservices
- API Gateway
- Asynchronous messaging
- Docker
- CI/CD

The architecture intentionally avoids unnecessary complexity such as CQRS, Event Sourcing, multiple databases, Kubernetes, or distributed caching.

---

## Roadmap

### Phase 1 — Core Backend

- [x] Create Expense Services
- [x] Transaction CRUD
- [x] PostgreSQL integration
- [x] JPA/Hibernate
- [x] Flyway migrations
- [x] Validation
- [x] Global exception handling

### Phase 2 — Domain Features

- [x] Categories
- [x] Budgets
- [x] Monthly summary
- [x] Filtering
- [x] Pagination
- [x] Sorting

### Phase 3 — Authentication

- [x] Identity Service
- [x] Registration
- [x] Login
- [x] JWT
- [x] Refresh tokens
- [x] Authorization

### Phase 4 — Microservices

- [x] API Gateway
- [x] Service-to-service communication
- [x] Docker networking
- [ ] Configuration management
- [x] Health checks

### Phase 5 — Frontend

- [x] Angular application
- [x] Authentication screens
- [ ] Dashboard
- [ ] Transaction management
- [ ] Categories
- [ ] Budgets

### Phase 6 — Messaging

- [ ] RabbitMQ
- [ ] Domain events
- [ ] Notification Service
- [ ] Budget exceeded notifications

### Phase 7 — Quality

- [x] Unit tests
- [x] Integration tests
- [x] Testcontainers
- [x] API documentation
- [x] Docker Compose
- [x] GitHub Actions
- [ ] Production-ready configuration

---

## Project Philosophy

The project follows a simple principle:

> **Build a small product well before making it a complex system.**

Technologies are introduced only when they provide a clear learning opportunity or solve an actual problem in the application.

The objective is not to maximize the number of technologies used, but to understand the fundamentals behind a production-oriented Java web application.

---

## Status

**In development**

This project is being developed as a personal portfolio and learning project focused on Java backend development and practical software engineering.

---

## License

TBD
