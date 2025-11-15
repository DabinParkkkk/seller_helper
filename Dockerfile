FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY seller_helper/pom.xml ./pom.xml
COPY seller_helper/src ./src

RUN mvn -e -X -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
