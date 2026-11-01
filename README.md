# School Management System Backend

**Student:** IFITUMUGAMBI RAFIKI  
**Student ID:** 26689  
**Project:** School Management System  
**Course:** INSY 8322 Web Technology and Internet

Spring Boot REST API for school subjects, classrooms, teachers, students, exams, and grades. It uses Spring Web, Spring Data JPA, and PostgreSQL.

## Requirements

- Java 21
- PostgreSQL

## Run locally

1. Create a PostgreSQL database:

   ```sql
   CREATE DATABASE school_management_system;
   ```

2. Set database connection environment variables in PowerShell (replace the password with your local PostgreSQL password):

   ```powershell
   $env:DB_USERNAME = "postgres"
   $env:DB_PASSWORD = "your-postgres-password"
   $env:DATABASE_URL = "jdbc:postgresql://localhost:5432/school_management_system"
   ```

3. Start the API from this project directory:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Hibernate updates the schema on startup. SQL statements are logged to the console. The API listens at `http://localhost:8080`.

## Postman

Import [`school-management-system-midterm.postman_collection.json`](./school-management-system-midterm.postman_collection.json). The collection defaults to `http://localhost:8080`; change its `baseUrl` variable if the API runs elsewhere. The seed requests create the fixtures through the API and capture generated UUIDs automatically.

For a complete run, use a fresh database and run the whole collection with Postman or Newman:

```powershell
npx --yes newman run .\school-management-system-midterm.postman_collection.json
```

The seed and A2 setup requests create records, so do not rerun those folders against a database that already contains the fixtures. The collection cleans up its temporary A2 and D1 records, retains the required seeded grade, and removes only the temporary CRUD exam and its test grade during E3. D4 restores the teachers' original classroom assignments. The remaining folders cover requirements B1–F2 and both bonus endpoints.

## Seed data and date constraint

The exam paper requires two exams dated **2026-10-20**, including one VOIDED exam, but also requires `/api/exams/save` to reject any exam date before the current date. Since the assignment document is dated October 28, 2026, those two requirements cannot both be demonstrated by saving the specified exams through that endpoint. The API enforces the safe-save rule as written. Confirm the intended seed-data handling with the lecturer.

The suggested test records should include four subjects (Mathematics, Physics, English, Kinyarwanda), four classrooms (leave room 4 unassigned), three teachers, five students (including two with last name Iradukunda), ten exams across October and November 2026 in all four statuses, and grades attached only to MARKED exams.
