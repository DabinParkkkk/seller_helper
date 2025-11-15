FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY seller_helper/pom.xml pom.xml
COPY seller_helper/src src

RUN ./mvnw -e -X -DskipTests package

# ---- Run Stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
