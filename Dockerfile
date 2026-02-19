FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .

# build only one module (MODULE_NAME passed as build arg)
ARG MODULE_NAME
RUN mvn -DskipTests clean package -pl ${MODULE_NAME} -am

FROM eclipse-temurin:17-jre
WORKDIR /app

ARG MODULE_NAME
COPY --from=build /workspace/${MODULE_NAME}/target/*.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java","-jar","app.jar"]
