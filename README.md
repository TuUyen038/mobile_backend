# 📱 Mobile Backend (`mobile_be`)

A backend service for a mobile application, built with **Spring Boot**, using **MongoDB** for data storage.

---

## 📦 Project Overview

- **Framework**: Spring Boot `3.4.4`
- **Language**: Java 17
- **Database**: MongoDB (`mydb`)
- **Port**: `8081`

---

## 📁 Folder Structure

mobile_be/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── mobile_be/
│   │   │               ├── MobileBeApplication.java     
│   │   │
│   │   │               ├── config/                      # Cấu hình ứng dụng (MongoDB)
│   │   │               │   └── WebConfig.java
│   │   │
│   │   │               ├── controllers/                  # REST API endpoints
│   │   │               │   └── UserController.java
│   │   │
│   │   │               ├── service/                     # Logic xử lý nghiệp vụ
│   │   │               │   └── UserService.java
│   │   │                        ...
│   │   │               ├── repository/                  # Giao tiếp với MongoDB
│   │   │               │   └── UserRepository.java
│   │   │                        ...
│   │   │               └── models/                       # Các class đại diện cho dữ liệu
│   │   │                   └── User.java
│   │   │                        ...
│   │   └── resources/
│   │       ├── application.properties                   # File cấu hình Spring Boot
│   │       └── static/                                  # Tài nguyên tĩnh (nếu cần)
│   │
│   └── test/                                           
│       └── java/com/example/mobilebe/
│           └──...
│
├── build.gradle                                          # File cấu hình build (Gradle)
├── .gitignore                                           
....

## Run the application
./gradlew bootRun

## Run the application

