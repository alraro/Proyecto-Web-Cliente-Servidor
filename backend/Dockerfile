# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiamos el pom primero para cachear la descarga de dependencias en una capa separada.
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# El código fuente invalida solo la última capa, no toda la resolución de dependencias.
COPY src ./src
RUN mvn -B -DskipTests package

# Etapa 2: Ejecución (Contenedor final ligero)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Extraer el .war de la etapa anterior
COPY --from=build /app/target/*.war app.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]