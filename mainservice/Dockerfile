FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY IOT_service/pom.xml IOT_service/
COPY mainservice/pom.xml mainservice/
COPY apigateway/pom.xml apigateway/
COPY chatbotbackend/pom.xml chatbotbackend/
COPY discoveryserver/pom.xml discoveryserver/
COPY vitalReports-AI/pom.xml vitalReports-AI/
RUN mvn dependency:go-offline -pl mainservice -am -q 2>/dev/null || true
COPY mainservice/src mainservice/src
RUN mvn -pl mainservice -am clean package -DskipTests -q

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/mainservice/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
