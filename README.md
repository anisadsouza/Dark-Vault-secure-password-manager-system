# Secure Password Manager System

This project is a console-based Java application built to satisfy the assignment requirements for OOP, JDBC, CRUD operations, and layered architecture.

## Why this topic works well

The system is a realistic cybersecurity-focused application that demonstrates:

- Object-oriented design in Java
- Data persistence using JDBC
- Layered architecture
- CRUD operations on a relational database
- Basic password protection using hashing and AES encryption

## Features

- User registration and login
- Role-based users: `AdminUser` and `StandardUser`
- Add, view, update, delete, and search saved credentials
- Site-wise credential summary using collections
- Admin-only user listing
- Exception handling and input validation

## Technologies Used

- Java 17
- JDBC
- SQLite
- Maven

## Layered Architecture

- Presentation Layer: console UI classes
- Business Layer: authentication and credential services
- Data Access Layer: DAO interfaces and JDBC implementations
- Database Layer: SQLite tables and stored data

## OOP Concepts Covered

- Classes and Objects: `User`, `Credential`, DAO/service classes
- Encapsulation: private fields with getters and setters
- Inheritance: `AdminUser` and `StandardUser` extend `User`
- Polymorphism: overridden methods such as `canManageUsers()` and `getDisplayRole()`
- Abstraction: `User` is an abstract class and `EncryptionService` is an interface
- Method Overloading: overloaded `addCredential(...)` and `searchCredentials(...)` methods
- Constructors: parameterized constructors across model classes
- Collections: `ArrayList` and `HashMap`

## Project Structure

```text
src/main/java/com/passwordmanager
├── config
├── dao
├── model
├── service
├── ui
└── util
```

## Database Design

### `users`

- `user_id` INTEGER PRIMARY KEY
- `username` UNIQUE NOT NULL
- `password_hash` NOT NULL
- `role` NOT NULL

### `credentials`

- `cred_id` INTEGER PRIMARY KEY
- `user_id` FOREIGN KEY REFERENCES `users(user_id)`
- `site_name` NOT NULL
- `site_username` NOT NULL
- `encrypted_password` NOT NULL
- `notes`

## How to Run

1. Build the project:

```bash
mvn clean compile
```

2. Run the application:

```bash
mvn exec:java
```

The SQLite database file `password_manager.db` will be created automatically in the project folder.

## Sample Report Resources

- Database schema: `database/schema.sql`
- Sample records: `database/sample-data.sql`
- Class diagram: `docs/class-diagram.md`
- Project report template: `docs/project-report-template.md`

## Academic Note

This project uses a fixed application secret for AES encryption so the demo stays easy to run and evaluate. In a production-grade password manager, key management would be more advanced and security controls would be stronger.
