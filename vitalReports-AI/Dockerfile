FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY IOT_service/pom.xml IOT_service/
COPY mainservice/pom.xml mainservice/
COPY apigateway/pom.xml apigateway/
COPY chatbotbackend/pom.xml chatbotbackend/
COPY discoveryserver/pom.xml discoveryserver/
COPY vitalReports-AI/pom.xml vitalReports-AI/
RUN mvn dependency:go-offline -pl vitalReports-AI -am -q 2>/dev/null || true
COPY vitalReports-AI/src vitalReports-AI/src
RUN mvn -pl vitalReports-AI -am clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/vitalReports-AI/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
