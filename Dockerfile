# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the jar file
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a slim Java Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render will use this to route traffic)
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]