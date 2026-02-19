#FROM maven:3.9-eclipse-temurin-17 AS build
#WORKDIR /app
#COPY . .
#RUN mvn -pl mainservice -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY mainservice/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

