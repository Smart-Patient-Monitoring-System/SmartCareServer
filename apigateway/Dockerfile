#FROM maven:3.9-eclipse-temurin-17 AS build
#WORKDIR /app
#COPY . .
#RUN mvn -pl apigateway -am clean package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY apigateway/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","app.jar"]

