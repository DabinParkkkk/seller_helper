FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY src ./src

RUN mvn -e -X -DskipTests package


FROM eclipse-temurin:21-jdk AS run
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
