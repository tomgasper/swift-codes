FROM maven:3.9.6-eclipse-temurin-22-jammy AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:22-jre

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]