FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY IOT_service/pom.xml IOT_service/
COPY mainservice/pom.xml mainservice/
COPY apigateway/pom.xml apigateway/
COPY chatbotbackend/pom.xml chatbotbackend/
COPY discoveryserver/pom.xml discoveryserver/
COPY vitalReports-AI/pom.xml vitalReports-AI/
RUN mvn dependency:go-offline -pl apigateway -am -q 2>/dev/null || true
COPY apigateway/src apigateway/src
RUN mvn -pl apigateway -am clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/apigateway/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","app.jar"]
