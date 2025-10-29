# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia apenas os arquivos de configuração do Maven primeiro para aproveitar o cache
COPY pom.xml .

# Baixa as dependências (cache mais eficiente)
RUN mvn dependency:go-offline

# Copia o restante do código-fonte
COPY src ./src

# Compila e empacota a aplicação
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Expondo a porta da aplicação
EXPOSE 8080

# Copia o JAR gerado do stage build
COPY --from=build /app/target/Benucci-App-0.0.1-SNAPSHOT.jar app.jar

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
