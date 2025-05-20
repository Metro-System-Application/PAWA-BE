FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /home/app
COPY . .

RUN mvn clean install -DskipTests
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /home/app/application/target/*.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
