# Dark Vault: Secure Password Manager System

A browser-based Java application for securely storing and managing website credentials using JDBC and a relational database.

This project was developed as a real-world Object-Oriented Programming and Database Connectivity assignment. It demonstrates software engineering practices, layered architecture, modular programming, CRUD operations, input validation, and data persistence in Java.

## Project Overview

Dark Vault is a secure password manager built with Java, SQLite, JDBC, HTML, CSS, and JavaScript. It allows users to register, log in, and manage website credentials through a cyber-themed browser interface.

The system supports:

- user registration and login
- credential creation, viewing, updating, deletion, and search
- password strength checking
- password generation
- password masking and reveal
- encrypted password storage
- site summary statistics

## Assignment Alignment

This project satisfies the major assignment requirements:

- `Programming Language`: Java
- `Database`: SQLite
- `Database Connectivity`: JDBC
- `Architecture`: Presentation Layer, Business Logic Layer, DAO Layer, Database Layer
- `Core OOP Concepts`: classes, objects, encapsulation, inheritance, polymorphism, abstraction
- `CRUD Operations`: fully implemented for credential management
- `Validation and Exception Handling`: included across UI, service, and DAO layers

## Technology Stack

| Category | Technology |
| --- | --- |
| Language | Java 17 |
| Build Tool | Maven |
| Database | SQLite |
| JDBC Driver | `org.xerial:sqlite-jdbc:3.46.1.3` |
| Backend | Java built-in `HttpServer` |
| Frontend | HTML, CSS, JavaScript |
| Encryption | AES for credential encryption, SHA-256 for password hashing |

## Features

### User Management

- Register a new user
- Login with username and password

### Credential Management

- Add credential
- View all saved credentials
- Search credentials by site name or username
- Edit existing credential
- Delete credential

### Security and Usability Features

- Passwords stored in encrypted form in the database
- User account passwords stored as SHA-256 hashes
- Password strength checker: `Weak / Medium / Strong`
- Strong password generator
- Show/Hide password option
- Masked passwords in dashboard view

### Validation and Reliability

- Empty field validation
- Invalid input validation
- Minimum password length validation
- Database exception handling using `try-catch`

## System Architecture

The project follows a layered architecture:

### 1. Presentation Layer

Responsible for the browser interface.

- `src/main/resources/static/index.html`
- `src/main/resources/static/styles.css`
- `src/main/resources/static/app.js`

### 2. Business Logic Layer

Responsible for application rules and processing.

- `AuthService`
- `CredentialService`
- `AESEncryptionService`

### 3. Data Access Layer

Responsible for database interaction using JDBC.

- `UserDAO`
- `CredentialDAO`
- `UserDAOImpl`
- `CredentialDAOImpl`

### 4. Database Layer

Responsible for tables and persistent data.

- SQLite database file created at runtime
- SQL scripts in `database/`

## OOP Concepts Demonstrated

### Classes and Objects

The project uses multiple classes such as:

- `User`
- `StandardUser`
- `Credential`
- `AuthService`
- `CredentialService`
- `UserDAOImpl`
- `CredentialDAOImpl`
- `PasswordManagerWebServer`

### Encapsulation

Model classes use private fields with getters and setters.

Examples:

- `User`
- `Credential`

### Inheritance

`StandardUser` inherits from the abstract `User` class.

### Polymorphism

Method overriding is used through subclass behavior such as:

- `StandardUser#getDisplayRole()`

Interface-based polymorphism is also used through DAO and encryption abstractions.

### Abstraction

The project demonstrates abstraction through:

- abstract class: `User`
- interface: `EncryptionService`
- interfaces: `UserDAO`, `CredentialDAO`

### Constructors

Constructors are used in model classes and service classes to initialize application state and dependencies.

### Collections

The project uses Java collections such as:

- `List`
- `Map`

Examples:

- returning credential lists from DAO/service layers
- building site summary counts using `Map<String, Integer>`

## Database Design

The project uses a relational SQLite database with two main tables.

### `users`

| Column | Type | Description |
| --- | --- | --- |
| `user_id` | INTEGER | Primary key |
| `username` | TEXT | Unique username |
| `password_hash` | TEXT | SHA-256 hashed password |
| `role` | TEXT | User role |

### `credentials`

| Column | Type | Description |
| --- | --- | --- |
| `cred_id` | INTEGER | Primary key |
| `user_id` | INTEGER | Foreign key referencing `users.user_id` |
| `site_name` | TEXT | Website or platform name |
| `site_username` | TEXT | Username/email used for that site |
| `encrypted_password` | TEXT | AES-encrypted password |
| `notes` | TEXT | Optional notes |

### Relationship

- One user can have many credentials
- `credentials.user_id` references `users.user_id`

## Project Structure

```text
PasswordManager/
├── database/
│   ├── sample-data.sql
│   └── schema.sql
├── docs/
│   ├── class-diagram.md
│   └── project-report-template.md
├── src/
│   └── main/
│       ├── java/com/passwordmanager/
│       │   ├── config/
│       │   ├── dao/
│       │   ├── model/
│       │   ├── service/
│       │   ├── web/
│       │   └── MainApp.java
│       └── resources/static/
│           ├── app.js
│           ├── index.html
│           └── styles.css
├── pom.xml
└── README.md
```

## Main Java Files

| File | Purpose |
| --- | --- |
| `MainApp.java` | Application entry point |
| `DatabaseConfig.java` | Creates database connections |
| `DatabaseInitializer.java` | Creates database tables |
| `User.java` | Abstract base class for users |
| `StandardUser.java` | Concrete user implementation |
| `Credential.java` | Credential model |
| `AuthService.java` | Registration and login logic |
| `CredentialService.java` | Credential business logic |
| `AESEncryptionService.java` | Encrypts and decrypts credential passwords |
| `UserDAOImpl.java` | JDBC operations for users |
| `CredentialDAOImpl.java` | JDBC operations for credentials |
| `PasswordManagerWebServer.java` | Local web server and API routing |

## Frontend Interface

The project uses a local browser-based UI instead of a console UI. When the application runs, it starts a local server and opens through a browser URL such as:

```text
http://localhost:8080
```

The interface includes:

- login/register page
- dashboard with credential grid
- search bar
- add/edit credential modal
- password reveal buttons
- password strength meter
- vault summary section

## How to Run the Project

### Requirements

- Java 17 or higher
- Maven

### Run Command

```bash
mvn -Dmaven.repo.local=.m2 compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

Then open the printed URL in Chrome or any browser.

If port `8080` is already in use, the application automatically tries fallback ports like `8081`, `8082`, or `8083`.

## Database Files

The project includes:

- `database/schema.sql` for table creation
- `database/sample-data.sql` for sample records

The runtime database file is created automatically when the application starts.

## Validation and Exception Handling

This project includes both frontend and backend validation.

Examples:

- username cannot be empty
- password cannot be empty
- password minimum length validation
- invalid input detection

Database operations are protected with `try-catch` blocks using `SQLException`, which supports the assignment rubric for exception handling.

## Security Notes

This project demonstrates security concepts for academic purposes:

- account passwords are hashed with SHA-256
- saved site passwords are encrypted using AES
- session handling is managed on the local web server

This is an educational project and not intended as a production-grade password manager.

## Assignment Deliverables Covered

This repository includes or supports the following deliverables:

- Java source code
- SQL schema file
- sample database records
- browser-based user interface
- class diagram documentation
- project report template
- application screenshots can be captured after running the project

## Future Enhancements

Possible future improvements include:

- export/import credentials
- category-based filtering
- unit testing
- report generation
- stronger production-grade authentication
- Chrome extension version

## Author

Developed as an academic Java + JDBC project on Object-Oriented Programming, modular design, and relational database integration.
