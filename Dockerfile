# ---- build stage: compile the jar with Maven + JDK 25 ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
# Copy pom.xml first so Docker can cache the downloaded libraries between builds
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- run stage: small JRE image, runs as a normal user (not root) ----
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 appuser
COPY --from=build /app/target/mepmep.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
