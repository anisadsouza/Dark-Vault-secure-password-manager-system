# Project Report Template

## Title Page

**Project Title:** Secure Password Manager System  
**Course:** Java Programming / OOP with JDBC  
**Student Name:**  
**Student ID:**  
**Submission Date:** 15 April 2026

## Problem Statement

The aim of this project is to design and implement a secure password manager system using Java and JDBC. The application allows users to store, retrieve, update, delete, and search account credentials in a relational database while demonstrating key object-oriented programming concepts and layered software architecture.

## Objectives

- To build a real-world Java application using OOP concepts
- To implement CRUD operations with JDBC
- To design a maintainable layered architecture
- To apply encapsulation, inheritance, abstraction, and polymorphism
- To manage persistent application data using a relational database

## System Design

### Architecture

- Presentation Layer: Console UI
- Business Logic Layer: AuthService and CredentialService
- Data Access Layer: DAO interfaces and implementations
- Database Layer: SQLite database

### Main Modules

- User Registration and Login
- Credential Management
- Admin User Management
- Search and Reporting

## Class Diagram

Paste the diagram from `docs/class-diagram.md` or export it as an image.

## Database Schema

Attach the schema from `database/schema.sql`.

## Explanation of OOP Concepts Used

### Encapsulation

All model classes use private fields with public getters and setters.

### Inheritance

`AdminUser` and `StandardUser` inherit from the abstract `User` class.

### Polymorphism

Child classes override abstract methods such as `canManageUsers()` and `getDisplayRole()`.

### Abstraction

The project uses an abstract `User` class and the `EncryptionService` interface.

### Method Overloading

The `CredentialService` class overloads methods for adding and searching credentials.

## Screenshots of Application

Add screenshots for:

- Main menu
- Registration screen
- Login screen
- Add credential
- View credentials
- Update credential
- Search credential
- Admin user list

## Conclusion

The Secure Password Manager System successfully demonstrates object-oriented programming, database connectivity using JDBC, modular design, and CRUD functionality. It also introduces basic security techniques such as hashing and encryption, making it a practical and industry-relevant Java project.
