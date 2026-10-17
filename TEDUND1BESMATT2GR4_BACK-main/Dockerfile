# Back-End da Clinica Medica Academica (Java 21 + Spring Boot)
# Etapa 1: compila com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: so o Java e o .jar, rodando com usuario sem privilegios
FROM eclipse-temurin:21-jre
RUN useradd --system --create-home clinica
USER clinica
WORKDIR /app
COPY --from=build /app/target/clinica-backend-0.0.1-SNAPSHOT.jar app.jar
# Dentro da rede interna do Docker o Back-End escuta em todas as interfaces do container,
# mas o docker-compose NAO publica esta porta para fora (secao 6.4).
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
