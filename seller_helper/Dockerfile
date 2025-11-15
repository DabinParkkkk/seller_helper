# -----------------------------
# Stage 1: Build
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# 실제 폴더명: seller_helper
COPY seller_helper/pom.xml ./seller_helper/
COPY seller_helper/src ./seller_helper/src

RUN mvn -f seller_helper/pom.xml clean package -DskipTests

# -----------------------------
# Stage 2: Run
# -----------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/seller_helper/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
