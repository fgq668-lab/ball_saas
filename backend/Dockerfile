FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/ball-saas-backend-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 18200
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

