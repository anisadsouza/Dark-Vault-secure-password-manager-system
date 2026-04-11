# Dark Vault: Secure Digital Vault System

Dark Vault is a Java-based secure vault application with the **password manager as the main feature** and a secondary secure document vault for storing important files such as PDFs, Word documents, CSV files, and images.

The project was built for a Java + JDBC software engineering assignment. It demonstrates Object-Oriented Programming, layered architecture, relational database design, CRUD operations, input validation, exception handling, and persistent storage using SQLite.

## Main Purpose

The main goal of Dark Vault is to provide a secure browser-based dashboard where users can manage website credentials. The document vault is added as an extra feature to make the system more practical and unique, but the password manager remains the primary module.

## What Makes This Project Different

Most basic academic projects use common domains such as student management or library management. Dark Vault is different because it is based on a cybersecurity-style real-world use case.

It includes:

- secure credential management as the main feature
- encrypted storage of saved passwords
- password strength checking
- strong password generation
- show/hide password controls
- a secondary secure document vault
- SQLite database persistence through JDBC
- local browser dashboard powered by a Java backend

## Technology Stack

| Category | Technology Used |
| --- | --- |
| Programming Language | Java 17 |
| Build Tool | Maven |
| Database | SQLite |
| Database Connectivity | JDBC |
| JDBC Driver | `org.xerial:sqlite-jdbc:3.46.1.3` |
| Backend Server | Java built-in `HttpServer` |
| Frontend | HTML, CSS, JavaScript |
| Credential Encryption | AES |
| User Password Hashing | SHA-256 |
| File Storage | Local `vault-files/` folder |

## Core Features

### 1. Password Manager Module

This is the main module of the project.

- Register user
- Login user
- Add website credential
- View saved credentials
- Search credentials by site name or username
- Edit credential
- Delete credential
- Copy password
- Show/Hide password
- Password strength checker: `Weak`, `Medium`, `Strong`
- Strong password generator
- Password masking in dashboard

### 2. Secure Document Vault Module

This is a secondary feature below the password manager.

- Upload document/file records
- Store actual files locally in `vault-files/`
- Store file metadata in SQLite
- Search documents by title, file name, or category
- Download stored files
- Delete stored files

Supported file examples:

- PDF
- Word documents
- CSV files
- images
- text files
- other common local files

## Important Storage Explanation

Dark Vault uses two types of storage:

| Data Type | Where It Is Stored |
| --- | --- |
| User account data | SQLite database |
| Website credential records | SQLite database |
| Encrypted credential passwords | SQLite database |
| Document metadata | SQLite database |
| Actual uploaded files | Local `vault-files/` folder |

This design keeps SQLite as the main relational database while avoiding large file BLOB storage inside the database. Instead, SQLite stores the file path and metadata, and the actual file is saved locally.

## System Architecture

The project follows a layered architecture.

### Presentation Layer

Responsible for the browser-based user interface.

- `src/main/resources/static/index.html`
- `src/main/resources/static/styles.css`
- `src/main/resources/static/app.js`

### Business Logic Layer

Responsible for validation, processing, encryption, and application rules.

- `AuthService`
- `CredentialService`
- `DocumentService`
- `AESEncryptionService`

### Data Access Layer

Responsible for database interaction using JDBC.

- `UserDAO`
- `CredentialDAO`
- `DocumentDAO`
- `UserDAOImpl`
- `CredentialDAOImpl`
- `DocumentDAOImpl`

### Database Layer

Responsible for persistent relational data.

- SQLite database file created at runtime
- SQL schema in `database/schema.sql`
- file metadata stored in SQLite
- actual files stored in `vault-files/`

## OOP Concepts Demonstrated

### Classes and Objects

The project contains multiple classes that represent models, services, DAO objects, and the web server.

Examples:

- `User`
- `StandardUser`
- `Credential`
- `SecureDocument`
- `AuthService`
- `CredentialService`
- `DocumentService`
- `PasswordManagerWebServer`

### Encapsulation

Model classes use private fields with public getters and setters.

Examples:

- `User`
- `Credential`
- `SecureDocument`

### Inheritance

`StandardUser` inherits from the abstract `User` class.

### Polymorphism

Polymorphism is demonstrated through method overriding and interface-based design.

Examples:

- `StandardUser#getDisplayRole()`
- `EncryptionService` implemented by `AESEncryptionService`
- `CredentialDAO` implemented by `CredentialDAOImpl`
- `DocumentDAO` implemented by `DocumentDAOImpl`

### Abstraction

The project uses:

- abstract class: `User`
- interface: `EncryptionService`
- interface: `UserDAO`
- interface: `CredentialDAO`
- interface: `DocumentDAO`

### Constructors

Constructors are used in model and service classes to initialize fields and dependencies.

### Collections

The project uses Java collections such as:

- `List`
- `Map`

Examples:

- returning credential and document lists
- building site summary statistics using `Map<String, Integer>`

## Database Design

The project uses SQLite with three main relational tables.

### `users`

| Column | Type | Description |
| --- | --- | --- |
| `user_id` | INTEGER | Primary key |
| `username` | TEXT | Unique username |
| `password_hash` | TEXT | SHA-256 hashed user password |
| `role` | TEXT | Standard user role |

### `credentials`

| Column | Type | Description |
| --- | --- | --- |
| `cred_id` | INTEGER | Primary key |
| `user_id` | INTEGER | Foreign key referencing `users.user_id` |
| `site_name` | TEXT | Website/platform name |
| `site_username` | TEXT | Username or email for the site |
| `encrypted_password` | TEXT | AES-encrypted password |
| `notes` | TEXT | Optional notes |

### `secure_documents`

| Column | Type | Description |
| --- | --- | --- |
| `document_id` | INTEGER | Primary key |
| `user_id` | INTEGER | Foreign key referencing `users.user_id` |
| `title` | TEXT | Document display title |
| `original_file_name` | TEXT | Original uploaded file name |
| `stored_file_path` | TEXT | Path to file inside `vault-files/` |
| `mime_type` | TEXT | File MIME type |
| `category` | TEXT | Optional document category |
| `notes` | TEXT | Optional notes |
| `file_size_bytes` | INTEGER | File size in bytes |
| `date_added` | TEXT | Date/time document was added |

### Relationships

- One user can have many credentials.
- One user can have many secure documents.
- `credentials.user_id` references `users.user_id`.
- `secure_documents.user_id` references `users.user_id`.

## CRUD Operations

### Credential CRUD

| Operation | Feature |
| --- | --- |
| Create | Add credential |
| Read | View credential dashboard |
| Update | Edit credential |
| Delete | Delete credential |
| Search | Search by site name or username |

### Document CRUD

| Operation | Feature |
| --- | --- |
| Create | Upload/save document |
| Read | View document list |
| Delete | Delete document |
| Search | Search by title, file name, or category |
| Download | Download stored file |

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
│       │   │   └── impl/
│       │   ├── model/
│       │   ├── service/
│       │   ├── web/
│       │   └── MainApp.java
│       └── resources/static/
│           ├── app.js
│           ├── index.html
│           └── styles.css
├── vault-files/
├── pom.xml
└── README.md
```

Note: `vault-files/` is created locally for uploaded files and is ignored by Git.

## Important Files

| File | Purpose |
| --- | --- |
| `MainApp.java` | Application entry point |
| `DatabaseConfig.java` | SQLite connection configuration |
| `DatabaseInitializer.java` | Creates required database tables |
| `User.java` | Abstract user model |
| `StandardUser.java` | Standard user implementation |
| `Credential.java` | Credential model |
| `SecureDocument.java` | Document/file metadata model |
| `AuthService.java` | Register/login logic |
| `CredentialService.java` | Credential business logic |
| `DocumentService.java` | Document upload/download/delete logic |
| `AESEncryptionService.java` | AES encryption for saved passwords |
| `UserDAOImpl.java` | JDBC operations for users |
| `CredentialDAOImpl.java` | JDBC operations for credentials |
| `DocumentDAOImpl.java` | JDBC operations for secure documents |
| `PasswordManagerWebServer.java` | Web server and API routes |
| `index.html` | Browser UI structure |
| `styles.css` | Cyber-themed UI styling |
| `app.js` | Browser UI behavior and API calls |

## Web API Overview

The browser UI communicates with the Java backend using local API routes.

### Auth APIs

- `POST /api/register`
- `POST /api/login`
- `POST /api/logout`
- `GET /api/session`

### Credential APIs

- `GET /api/credentials`
- `POST /api/credentials`
- `PUT /api/credentials`
- `DELETE /api/credentials?id=...`
- `GET /api/summary`

### Document APIs

- `GET /api/documents`
- `POST /api/documents`
- `DELETE /api/documents?id=...`
- `GET /api/documents/download?id=...`

## How to Run

### Requirements

- Java 17 or higher
- Maven
- Chrome or any modern browser

### Run Command

From the project folder, run:

```bash
mvn -Dmaven.repo.local=.m2 compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

The terminal will print a URL such as:

```text
Dark Vault is running at http://localhost:8080
```

Open that URL in Chrome.

If port `8080` is busy, the application automatically tries:

- `8081`
- `8082`
- `8083`

Always use the exact URL printed in the terminal.

## How to Use

1. Register a new user.
2. Login with the registered account.
3. Use the main password vault first:
   - add credentials
   - search credentials
   - reveal/copy passwords
   - edit or delete credentials
4. Scroll below the password vault to use the secondary document vault:
   - choose a file
   - enter title/category/notes
   - save the document
   - download or delete the document later

## File Storage Behavior

Uploaded files are copied into:

```text
vault-files/
```

The SQLite database stores:

- document title
- original file name
- local stored path
- MIME type
- category
- notes
- file size
- date added

The actual file content is stored locally, not inside Git.

## Git Ignore Notes

The following files/folders are intentionally ignored:

- `target/`
- `.m2/`
- `password_manager.db`
- `vault-files/`
- `.DS_Store`
- compiled `.class` files
- log files

This prevents generated files, local dependencies, database files, and user-uploaded documents from being committed.

## Validation and Exception Handling

Validation is implemented on both frontend and backend.

Examples:

- username cannot be empty
- password cannot be empty
- password must be at least 8 characters
- site name cannot be empty
- file title cannot be empty
- file must be selected before saving
- invalid characters such as `<` and `>` are rejected
- uploaded files are limited to 10 MB

Database operations use `try-catch` blocks with `SQLException`, and user-friendly error messages are returned to the browser.

## Security Notes

This project demonstrates security concepts for academic purposes.

Implemented security features:

- user passwords are hashed using SHA-256
- saved credential passwords are encrypted using AES
- password fields are masked by default
- reveal buttons are explicit user actions
- local session cookie is used for the browser session
- uploaded files are stored outside Git in `vault-files/`

Important academic note:

This project is suitable for demonstrating secure programming concepts, but it is not intended to replace a production-grade commercial password manager.

## Assignment Checklist

| Requirement | Status |
| --- | --- |
| Java application | Implemented |
| JDBC database connectivity | Implemented |
| Relational database | SQLite |
| CRUD operations | Implemented |
| Minimum 5 classes | Implemented |
| Encapsulation | Implemented |
| Inheritance | Implemented |
| Polymorphism | Implemented |
| Abstraction/interface | Implemented |
| Collections | Implemented |
| Exception handling | Implemented |
| Validation | Implemented |
| Browser/GUI interface | Implemented |
| SQL schema file | Included |
| Class diagram support | Included in `docs/` |
| Documentation | Included |

## Bonus Feature

The repository may also include a separate Chrome extension bonus feature in:

```text
chrome-extension/
```

This extension is separate from the Java + SQLite application. The main assignment project remains the Java browser app with SQLite and JDBC.

## Future Enhancements

Possible improvements:

- edit/update secure document metadata
- file preview for images/PDFs
- export password records
- category filters
- unit testing
- stronger authentication
- encryption for uploaded documents
- REST API version

## Author

Developed as an academic Java + JDBC project demonstrating Object-Oriented Programming, layered architecture, secure credential management, and relational database persistence.
