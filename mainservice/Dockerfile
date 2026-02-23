#FROM maven:3.9-eclipse-temurin-17 AS build
#WORKDIR /app
#COPY . .
#RUN mvn -pl mainservice -am clean package -DskipTests

FROM eclipse-temurin:17-jre

# Install Tesseract OCR
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/* \

WORKDIR /app
COPY mainservice/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

