FROM maven:3.9-eclipse-temurin-21 AS base

WORKDIR /workspace

# Pre-fetch dependencies to speed up iterative builds
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy source
COPY . .

RUN mvn -q -DskipTests package

EXPOSE 8080

# Run packaged JAR (stable in containers)
CMD ["sh", "-c", "java -jar target/*-SNAPSHOT.jar"]
