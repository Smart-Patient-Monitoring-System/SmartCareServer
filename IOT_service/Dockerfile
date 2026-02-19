#FROM maven:3.9-eclipse-temurin-17 AS build
#WORKDIR /app
#COPY . .
#RUN

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY IOT_service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]

