FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --uid 1001 hms
COPY --from=build --chown=hms:hms /app/target/hms-*.jar app.jar

USER hms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
