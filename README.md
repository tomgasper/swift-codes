# SwiftCodes Application

This Spring Boot application provides REST APIs for managing Swift codes. It uses PostgreSQL as the database and includes CSV parsing capabilities.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (for PostgreSQL container)
- Docker Compose

## Project Structure

```
src/main/java/com/example/swiftcodes/
├── SwiftCodesApplication.java
├── controller/
├── service/
├── repository/
├── model/
└── config/
```

## Building the Project

To build the project, run:

```bash
mvn clean install
```

## Running the Application

1. Start the PostgreSQL database:
```bash
docker-compose up -d
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## Running Tests

To run the tests:

```bash
mvn test
```

## API Documentation

API documentation will be available at `http://localhost:8080/swagger-ui.html` once implemented.