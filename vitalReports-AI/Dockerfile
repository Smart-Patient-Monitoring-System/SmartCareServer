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

# Install Python3 + pip + required packages for ECG ML prediction
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    python3-numpy \
    && pip3 install --no-cache-dir tensorflow wfdb scipy \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Create symlink so 'python' resolves to python3
RUN ln -sf /usr/bin/python3 /usr/bin/python

COPY --from=build /app/vitalReports-AI/target/*.jar app.jar

# Copy ECG Python script and Keras model into working directory
COPY vitalReports-AI/ecg_predict.py ecg_predict.py
COPY vitalReports-AI/ecg_cnn_mitbih.keras ecg_cnn_mitbih.keras

EXPOSE 8083
ENV ECG_PYTHON_PATH=python
ENV ECG_SCRIPT_PATH=ecg_predict.py
ENTRYPOINT ["java","-jar","app.jar"]
