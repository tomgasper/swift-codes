FROM eclipse-temurin:22-jdk AS builder

WORKDIR /app
COPY . .
RUN ./mvnw clean package

FROM eclipse-temurin:22-jre

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]