# Attendance Microservice (`attendance-service`)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Traditional%20Layered%20(3--Tier)-blue.svg)](#architectural-style)
[![Tests](https://img.shields.io/badge/Tests-81%20Passed-brightgreen.svg)](#build-quality--testing)
[![Coverage](https://img.shields.io/badge/Coverage-93.10%25%20(Min.%2080%25%20Enforced)-success.svg)](#build-quality--testing)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-grade RESTful microservice for employee attendance management built strictly according to **Traditional Layered Architecture (Arquitectura en Capas / 3-Tier Architecture)** principles using **Java 21**, **Spring Boot 3.3.4**, and **Maven**.

---

## Table of Contents
- [Architectural Style](#architectural-style)
- [Directory & Package Structure](#directory--package-structure)
- [Technology Stack](#technology-stack)
- [Business Rules & Domain Invariants](#business-rules--domain-invariants)
- [REST API Specifications & Contracts](#rest-api-specifications--contracts)
- [cURL Execution Examples](#curl-execution-examples)
- [Build, Quality & Testing](#build-quality--testing)
- [Swagger UI & Database Console](#swagger-ui--database-console)
- [IDE Configuration (.vscode, IntelliJ, Eclipse)](#ide-configuration)

---

## Architectural Style

This microservice strictly follows **Traditional Layered Architecture (3-Tier Architecture)**, organizing components by technical responsibility rather than Ports and Adapters / Hexagonal patterns:

```
┌────────────────────────────────────────────────────────┐
│               Presentation Layer (Controller)          │
│ - AttendanceController (@Validated)                    │
│ - GlobalExceptionHandler (@RestControllerAdvice)       │
│ - DTOs, Custom Deserializers & ConstraintValidators   │
└──────────────────────────┬─────────────────────────────┘
                           │ (Direct method calls)
                           ▼
┌────────────────────────────────────────────────────────┐
│               Business Logic Layer (Service)           │
│ - AttendanceService (Interface)                        │
│ - AttendanceServiceImpl (@Service, @Transactional)     │
│ - Business Rules & Invariant Validation                │
└──────────────────────────┬─────────────────────────────┘
                           │ (Spring Data JPA)
                           ▼
┌────────────────────────────────────────────────────────┐
│               Data Access Layer (Repository)           │
│ - AttendanceRepository (extends JpaRepository)         │
│ - Attendance JPA Entity (@Table(name = "attendances")) │
└──────────────────────────┬─────────────────────────────┘
                           │ (JDBC / SQL)
                           ▼
┌────────────────────────────────────────────────────────┐
│               Relational Database (H2 In-Memory)       │
└────────────────────────────────────────────────────────┘
```

### Architectural Principles:
1. **Separation of Concerns:** Each layer has a well-defined boundary (HTTP transport & mapping, transaction & business orchestration, data persistence & querying).
2. **Strict Unidirectional Dependency:** Controllers depend on Service interfaces; Service implementations depend on Repositories; Repositories query database tables mapped to JPA Entities.
3. **Immutability at Boundaries:** Read models (`AttendanceResponseDto`, `PaginatedResponseDto`, `ErrorResponseDto`) are implemented as immutable **Java 21 Records**.
4. **Declarative Validation & Encapsulated Domain Exceptions:** Controllers delegate pre-conditions to custom `ConstraintValidator` annotations (`@ValidEmployeeId`, `@NotFutureTimestamp`, `@ValidPageNumber`, `@ValidPageSize`), eliminating imperative checking logic, while core entities and services enforce self-contained domain invariants.
5. **API-First & Contract-Driven Design:** The OpenAPI 3.0.3 specification (`src/main/resources/static/openapi.yaml`) serves as the authoritative Single Source of Truth (SSOT). Controllers remain clean and free of intrusive `@Operation` and `@ApiResponse` boilerplate annotations, and Swagger UI directly renders the static specification.

---

## Directory & Package Structure

```text
com.company.attendance/
├── AttendanceApplication.java
├── controller/
│   ├── AttendanceController.java
│   └── GlobalExceptionHandler.java              (@RestControllerAdvice translating to ErrorResponseDto)
├── service/
│   ├── AttendanceService.java                   (Interface)
│   └── impl/
│       └── AttendanceServiceImpl.java           (Implementation with @Service & @Transactional)
├── repository/
│   └── AttendanceRepository.java                (Extends JpaRepository<Attendance, Long>)
├── model/
│   ├── Attendance.java                          (JPA Entity with @Entity, @Table(name = "attendances"))
│   └── AttendanceType.java                      (Enum: CHECK_IN, CHECK_OUT)
├── validation/
│   ├── ValidEmployeeId.java                     (Constraint annotation for positive employee ID)
│   ├── EmployeeIdValidator.java                 (Validator implementation for @ValidEmployeeId)
│   ├── NotFutureTimestamp.java                  (Constraint annotation for non-future timestamps)
│   ├── NotFutureTimestampValidator.java         (Validator implementation for @NotFutureTimestamp)
│   ├── ValidPageNumber.java                     (Constraint annotation for non-negative page index)
│   ├── PageNumberValidator.java                 (Validator implementation for @ValidPageNumber)
│   ├── ValidPageSize.java                       (Constraint annotation for page size 1..100)
│   └── PageSizeValidator.java                   (Validator implementation for @ValidPageSize)
├── dto/
│   ├── AttendanceRequestDto.java                (Class DTO with Lombok & Jakarta Validation)
│   ├── AttendanceResponseDto.java               (Immutable Java 21 Record)
│   ├── PaginatedResponseDto.java                (Generic immutable Java 21 Record for pagination)
│   ├── ErrorResponseDto.java                    (Immutable Java 21 Record for error responses)
│   └── FlexibleLocalDateTimeDeserializer.java (Custom Jackson deserializer for ISO/UTC)
└── exception/
    ├── FutureDateException.java                 (Runtime exception with encapsulated default messages)
    └── InvalidAttendanceException.java          (Runtime exception with encapsulated default messages)
```

Additional Resources:
- `src/main/resources/application.yml`: Configuration for Datasource, JPA, Hibernate, and Swagger UI.
- `src/main/resources/static/openapi.yaml`: Official OpenAPI 3.0.3 static specification.
- `lombok.config`: Configured with `lombok.addLombokGeneratedAnnotation = true` for JaCoCo exclusion.
- `.vscode/`: Pre-configured VS Code settings, build tasks, launch profiles, and recommended extensions.

---

## Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | 21 (LTS) | Modern language features (Records, Pattern Matching, Sealed types) |
| **Framework** | Spring Boot | 3.3.4 | Core framework, DI, Auto-configuration |
| **Web MVC** | Spring Web | 3.3.4 | RESTful controllers, serialization, error advice |
| **Persistence** | Spring Data JPA | 3.3.4 | Repository abstractions, Hibernate ORM |
| **Validation** | Jakarta Bean Validation | 3.0 | Declarative input validation (`ConstraintValidator`) |
| **Database** | H2 Database Engine | In-Memory | Embedded fast SQL database for dev and testing |
| **Documentation** | SpringDoc OpenAPI UI | 2.6.0 | OpenAPI 3.0.3 specification & Swagger UI |
| **Productivity** | Project Lombok | 1.18.34 | Boilerplate elimination in mutable DTOs and entities |
| **Code Formatting**| DiffPlug Spotless | 2.43.0 | Enforces Google Java Format (AOSP style) |
| **Test Coverage** | JaCoCo Maven Plugin | 0.8.12 | Enforces minimum 80% line coverage at build time |
| **Testing** | JUnit 5 & Mockito | 5.10.3 / 5.11 | Comprehensive unit and WebMvc slice testing |

---

## Business Rules & Domain Invariants

1. **Employee ID:**
   - Must be non-null and strictly positive (`> 0`), validated declaratively via `@ValidEmployeeId`.
   - Domain violations throw `InvalidAttendanceException`.
2. **Attendance Timestamp:**
   - Must be non-null. Cannot occur in the future relative to system time, validated via `@NotFutureTimestamp`.
   - Domain violations throw `FutureDateException(timestamp)` with dynamic diagnostics.
3. **Attendance Type:**
   - Must be either `CHECK_IN` or `CHECK_OUT`. Cannot be null.
4. **Pagination Parameters:**
   - Page index must be zero-based (`page >= 0`), enforced via `@ValidPageNumber`.
   - Page size must be positive and within range (`1 <= size <= 100`), enforced via `@ValidPageSize`.
5. **Self-Contained Domain Exceptions:**
   - `InvalidAttendanceException()` defaults to `"Invalid attendance data."`
   - `FutureDateException(LocalDateTime timestamp)` generates `"Cannot register an attendance with a future timestamp: " + timestamp`
6. **Pagination Contract Enforcement:**
   - When an employee has no attendance marks, returns HTTP 200 OK with `content: []`, `totalElements: 0`, and `totalPages: 0` (never HTTP 404).
   - The Service layer returns strictly `PaginatedResponseDto<AttendanceResponseDto>` via `PaginatedResponseDto.of(...)`.
7. **Flexible Timestamp Parsing:**
   - Accepts standard local ISO strings (`"2026-09-26T08:30:00"`).
   - Accepts UTC Z strings (`"2026-09-27T01:24:12.246Z"`, Swagger UI default) via custom `FlexibleLocalDateTimeDeserializer`.

---

## REST API Specifications & Contracts

### Summary Table

| HTTP Method | Path | Summary | Success Response | Error Responses |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/attendances` | Register employee attendance | `201 Created` + `Location` header | `400 Bad Request`, `500 Internal Server Error` |
| `GET` | `/api/v1/attendances/employee/{employeeId}` | Get paginated attendance marks | `200 OK` (`PaginatedResponseDto`) | `400 Bad Request`, `500 Internal Server Error` |

---

## cURL Execution Examples

### 1. Register Attendance Mark (UTC 'Z' format - Swagger default)
```bash
curl -X POST "http://localhost:8080/api/v1/attendances" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 101,
    "timestamp": "2026-09-27T01:24:12.246Z",
    "type": "CHECK_IN"
  }'
```
**Response (`201 Created`):**
```json
{
  "id": 1,
  "employeeId": 101,
  "timestamp": "2026-09-27T01:24:12.246",
  "type": "CHECK_IN"
}
```
**Header:** `Location: /api/v1/attendances/1`

---

### 2. Query Paginated Attendance Marks for Employee
```bash
curl -X GET "http://localhost:8080/api/v1/attendances/employee/101?page=0&size=10"
```
**Response (`200 OK`):**
```json
{
  "content": [
    {
      "id": 1,
      "employeeId": 101,
      "timestamp": "2026-09-27T01:24:12.246",
      "type": "CHECK_IN"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

### 3. Attempt Register Attendance with Future Date (Fails 400)
```bash
curl -X POST "http://localhost:8080/api/v1/attendances" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 101,
    "timestamp": "2099-01-01T08:00:00",
    "type": "CHECK_IN"
  }'
```
**Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-09-27T12:05:00",
  "status": 400,
  "error": "Bad Request",
  "code": "FUTURE_DATE_NOT_ALLOWED",
  "message": "Cannot register an attendance with a future timestamp: 2099-01-01T08:00",
  "path": "/api/v1/attendances"
}
```

---

### 4. Attempt Register Attendance with Invalid Fields (Fails 400)
```bash
curl -X POST "http://localhost:8080/api/v1/attendances" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": -5,
    "type": null
  }'
```
**Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-09-27T12:05:00",
  "status": 400,
  "error": "Bad Request",
  "code": "FIELD_VALIDATION_FAILED",
  "message": "Validation failed for one or more fields.",
  "path": "/api/v1/attendances",
  "details": {
    "employeeId": "Employee ID must be a positive number.",
    "timestamp": "Attendance timestamp is mandatory.",
    "type": "Attendance type is mandatory (CHECK_IN or CHECK_OUT)."
  }
}
```

---

### 5. Query Employee with No Attendance Records
```bash
curl -X GET "http://localhost:8080/api/v1/attendances/employee/999?page=0&size=10"
```
**Response (`200 OK`):**
```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

---

### 6. Query with Invalid Employee ID (Fails 400)
```bash
curl -X GET "http://localhost:8080/api/v1/attendances/employee/-1"
```
**Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-09-27T12:05:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_ATTENDANCE_DATA",
  "message": "Invalid attendance data.",
  "path": "/api/v1/attendances/employee/-1"
}
```

---

### 7. Attempt Query with Invalid Pagination Parameters (Fails 400)
```bash
curl -X GET "http://localhost:8080/api/v1/attendances/employee/101?page=-1&size=0"
```
**Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-09-27T12:05:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_REQUEST",
  "message": "Page number cannot be negative.",
  "path": "/api/v1/attendances/employee/101"
}
```

---

## Build, Quality & Testing

### Prerequisites
- **JDK 21** installed and configured in `JAVA_HOME`.
- **Apache Maven 3.9+** installed.

### Build and Package
To build the executable JAR:
```bash
mvn clean package
```

### Run Tests and Verify Quality Standards
Executes Spotless validation, runs all unit and WebMvc tests, generates the JaCoCo report, and enforces the **>= 80% line coverage** requirement:
```bash
mvn clean verify
```

### Test Suite Breakdown (81 Automated Tests)
The test suite achieves **93.10% line coverage** (surpassing the enforced 80% build gate) and executes in ~1.5 seconds:

| Test Class / Suite | Tests | Verification Scope & Tested Invariants |
| :--- | :---: | :--- |
| `AttendanceServiceImplTest` | 10 | Business invariants, boundary dates, mock persistence, transaction boundaries |
| `AttendanceControllerTest` | 10 | `@WebMvcTest` slice, JSON serialization, headers, declarative validation errors |
| `GlobalExceptionHandlerTest` | 10 | Exception translation for domain exceptions, bean validations, constraint violations |
| `AttendanceTest` | 9 | Entity invariants, negative IDs, null timestamps/types, getters/setters |
| `PageSizeValidatorTest` | 10 | Positive integers 1..100, boundary checks, zero/negative rejection |
| `PageNumberValidatorTest` | 8 | Non-negative integers, negative rejection, edge values |
| `FlexibleLocalDateTimeDeserializerTest` | 8 | Local ISO parsing, UTC 'Z' offsets, nano precision, malformed strings |
| `EmployeeIdValidatorTest` | 7 | Positive ID enforcement, null and negative handling |
| `DtoAndExceptionTest` | 6 | Record immutability, builder patterns, default exception messages |
| `NotFutureTimestampValidatorTest` | 3 | Past/present acceptance, future date rejection with tolerance |
| **Total** | **81** | **0 failures, 0 errors, 0 skipped (100% Green)** |

### Apply Code Formatting (Spotless)
To automatically format all Java source files according to Google Java Format (AOSP):
```bash
mvn spotless:apply
```

### Run Locally
```bash
mvn spring-boot:run
```

---

## Swagger UI & Database Console

The microservice follows a **Contract-First (API-First)** architecture. Swagger UI directly consumes the static OpenAPI 3.0.3 contract file (`/openapi.yaml`), ensuring high-fidelity API documentation without polluting Java controllers with metadata annotations:

- **Swagger UI Interactive Documentation:**
  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (configured via `springdoc.swagger-ui.url: /openapi.yaml`)
- **Static OpenAPI 3.0.3 Specification File:**
  [http://localhost:8080/openapi.yaml](http://localhost:8080/openapi.yaml) (served from `src/main/resources/static/openapi.yaml`)
- **Database Console:**
  [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL:** `jdbc:h2:mem:attendancedb`
  - **Username:** `sa`
  - **Password:** *(leave empty)*

---

## IDE Configuration

### Visual Studio Code
The project includes a ready-to-use `.vscode/` directory:
- `settings.json`: Configured for Java 21 formatting and whitespace management (delegating formatting to Spotless).
- `tasks.json`: Quick tasks for `Maven Build`, `Maven Test & Coverage`, `Spotless Format Apply`, `Spotless Check`, and `Run Spring Boot App`.
- `launch.json`: Native F5 execution and debugging profile targeting the main application class.
- `extensions.json`: Recommended extensions for Spring Boot, Lombok, and OpenAPI.

### IntelliJ IDEA
- Import the project as an existing Maven project.
- In **Settings > Build, Execution, Deployment > Compiler > Annotation Processors**, verify **Enable annotation processing** is checked.
- Lombok is fully supported via `lombok.config`.

### Eclipse / STS
- Select **File > Import > Existing Maven Projects**.
- Ensure Lombok is installed in your Eclipse installation if running older Eclipse builds without built-in Lombok.
