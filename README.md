# 📚 Courses

A Spring Boot application for managing language course scheduling, supporting teachers, students, and lessons with full CRUD operations and business logic enforcement.

---

## 🧱 Architecture

The project follows a clean **3-layer architecture**:

- **Controller (REST)** – receives HTTP requests and returns responses.
- **Service** – contains business logic.
- **Repository** – communicates with the database (JPA).

Each entity has its own 3-layer structure.

### 📦 Domain Entities

- **Teacher** – `id`, `firstName`, `lastName`, `List<Language>`
- **Student** – `id`, `firstName`, `lastName`, `Language`
- **Lesson** – `id`, `Student`, `Teacher`, `date`

---

## ✨ Functionalities

### 👩‍🏫 Teacher
- Get all teachers
- Get teacher by ID
- Add a teacher
- Partial update (languages only)
- Soft delete

### 👨‍🎓 Student
- Get all students
- Get student by ID
- Add a student  
  ⚠️ Only teachers who teach the student's language can be assigned
- Partial update (language only)
- Soft delete

### 📅 Lesson
- Get all lessons
- Get lesson by ID
- Add a lesson  
  ⚠️ Cannot schedule in the past  
  ⚠️ Cannot overlap with another lesson of the same teacher
- Update timetable  
  ⚠️ Only for future lessons  
  ⚠️ Cannot overlap with another lesson of the same teacher  
  ⚠️ Cannot schedule in the past
- Delete a lesson  
  ⚠️ Only for lessons that haven't started yet

---

## 🔧 Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- Spring Validation
- Lombok
- Liquibase
- MySQL / H2 (test scope)

---

## 🚀 Getting Started

Coming soon...
