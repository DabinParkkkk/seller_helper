# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 현재 폴더에 pom.xml, src가 바로 있음
COPY pom.xml .
COPY src ./src

RUN mvn -e -X -DskipTests package

# ---- Run Stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
