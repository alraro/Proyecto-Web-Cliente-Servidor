# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copiar el pom.xml y descargar dependencias (optimiza la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copiar el código fuente y empaquetar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiar el WAR generado en la etapa anterior
COPY --from=build /app/target/*.war app.war
EXPOSE 8080
# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.war"]