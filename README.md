# SwiftCodes Application

A Spring Boot REST API service for managing SWIFT/BIC codes. This application provides endpoints for querying, adding, and managing bank SWIFT codes.

## Functionality

- CSV data parsing and import
- CRUD operations for SWIFT codes
- CLI import/server mode


## Prerequisites

- Java 22
- Maven 3.12+
- Docker and Docker Compose
- PostgreSQL 16 (if running locally)

## Project Structure

```
src/main/java/com/tgasper/swiftcodes/
├── SwiftCodesApplication.java
├── controller/         # REST controllers
├── service/           # Business logic
├── repository/        # Data access layer
├── model/            # Domain entities
├── dto/              # Data transfer objects
├── exception/        # Custom exceptions
└── config/           # Application configuration
```

## Design Choices

- **Simplified Address Storage:**  
  Since the API doesn't require town-level filtering and doesn't accept detailed address information in the POST request, the address is stored as a single string. This choice simplifies data entry and processing without impacting core functionality.

- **Efficient Swift Code Representation:**  
  The full 11-character SWIFT code is stored alongside its base (first 8 characters) to improve query performance and simplify the entity model. Stricter normalization is possible by storing only the branch codes in the *swift_codes* table.

- **Consistent Bank Naming Assumption:**  
  All SWIFT codes sharing the same first 8 characters are assumed to belong to the same bank. This minimizes redundancy by ensuring that any bank name change needs to be updated in only one place.

Database schema:
![Schema](https://github.com/tomgasper/swift-codes/blob/main/example/diagram.png)

## Local Development Setup

1. Clone the repository:
```
git clone https://github.com/tomgasper/swift-codes.git
cd swift-codes
```

2. Download Maven Wrapper:
```
mvn wrapper:wrapper
```
This will create the necessary Maven wrapper files (mvnw, mvnw.cmd, and .mvn directory)

3. Setup the database:
   - The application uses PostgreSQL
   - Default configuration can be found in `application.properties`
   - For local development, ensure PostgreSQL is running on port 5500

```
docker compose up -d db
```

3. Build the project:
```
./mvnw clean install
```

## Running the Application

### Using Docker (Recommended)

1. Build and start the containers:
```
docker compose up -d
```

The application will be available at `http://localhost:8080`

#### Local Development

1. Start PostgreSQL:
```
docker compose up -d db
```

2. Run the application:
```
./mvnw spring-boot:run
```


## Data Import

The application supports importing SWIFT codes from CSV files. You can run the import in two ways:

1. Default import (uses bundled data):
```
./mvnw spring-boot:run
```

2. Custom import file:
```
./mvnw spring-boot:run -Dspring-boot.run.arguments="--import=/path/to/your/file.csv"
```

3. Server-only mode (no import):
```
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server"
```

## Testing

### Running Tests

Run all tests:
```
./mvnw test
```

Run specific test class:
```
./mvnw test -Dtest=SwiftCodeServiceTest
```

### Test Coverage

The project includes:
- Unit tests for services and utilities
- Integration tests for controllers
- CSV parsing tests

## Configuration

### Application Properties

Key configuration files:
- `application.properties`: Default configuration
- `application-docker.yml`: Docker environment configuration
- `(test directory) application.properties`: Test configuration

### Environment Variables

When running with Docker, you can configure:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `LOGGING_LEVEL_SPRING`