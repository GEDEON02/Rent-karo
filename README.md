# RentKaro

RentKaro is a full-stack property rental platform built with Spring Boot. It provides both:

- A **web application** (Thymeleaf UI) for guests, hosts, and admins
- A **REST API** secured with JWT for programmatic access

The project focuses on listing approvals, booking lifecycle management, reviews, wishlist management, notifications, and role-based administration.

## What this project does

RentKaro supports the core workflow of a rental marketplace:

- Users register/login and manage their profiles
- Hosts create and manage property listings
- Admins approve or reject listings
- Guests browse listings, book stays, and leave reviews
- Users manage wishlists and receive booking/approval notifications
- Admins monitor users, listings, bookings, reviews, and payments

## Key features

- **Authentication & authorization**
  - Form-based login for web
  - JWT auth for `/api/**`
  - Role-based access (`GUEST`, `HOST`, `ADMIN`)
- **Property management**
  - Create, update, delete listings
  - Admin approval flow (pending/approved/rejected)
  - Search/filter by city, price range, and guests
- **Booking system**
  - Guest booking requests
  - Host confirmation/cancellation flows
  - Booking status transitions
- **Payments**
  - Mock payment + refund endpoints
- **Reviews**
  - Property reviews for completed-stay users
- **Wishlist & notifications**
  - Save/unsave properties
  - Track user notifications
- **Documentation**
  - Swagger/OpenAPI UI available out of the box

## Tech stack

- **Java 21**
- **Spring Boot 4**
- Spring Security, Spring Web MVC, Spring Data JPA
- Thymeleaf (server-side rendered UI)
- MySQL (runtime database)
- H2 (test database)
- Maven

## Project structure

```text
src/main/java/com/RentKaro/RentKaro
├── config         # bootstrap & seed data
├── controller     # REST + web controllers
├── dto            # request/response models
├── exception      # custom exceptions + global handling
├── model          # JPA entities and enums
├── repository     # Spring Data repositories
├── security       # JWT + security config
└── service        # business logic

src/main/resources
├── templates      # Thymeleaf pages
└── static         # CSS/assets
```

## Getting started

### 1) Prerequisites

- Java 21
- Maven (or use `./mvnw`)
- MySQL 8+ running locally

### 2) Configure environment

The application reads DB settings from environment variables (with local defaults):

- `DB_URL` (default: `jdbc:mysql://localhost:3306/rentkaro?...`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD`

Example:

```bash
export DB_URL="jdbc:mysql://localhost:3306/rentkaro?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
```

### 3) Run the app

```bash
./mvnw spring-boot:run
```

App URLs:

- Web UI: `http://127.0.0.1:8080`
- Swagger UI: `http://127.0.0.1:8080/swagger-ui.html`
- OpenAPI JSON: `http://127.0.0.1:8080/v3/api-docs`

## Default seeded accounts (local dev)

On first run, the app seeds demo users and sample property/booking data:

- Admin: `admin@rentkaro.com` / `admin123`
- Host: `rahul@rentkaro.com` / `host123`
- Guest: `arjun@rentkaro.com` / `guest123`

Use these only for local development.

## Running tests

```bash
./mvnw test
```

Tests use an in-memory H2 database configuration under `src/test/resources/application.properties`.
