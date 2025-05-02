# Этап 1: Сборка приложения
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app

# Копируем исходный код и настройки Maven
COPY pom.xml .
COPY src ./src

# Собираем JAR-файл
RUN mvn clean package -DskipTests

# Этап 2: Создание итогового образа
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app

# Копируем JAR из этапа сборки
COPY --from=build /app/target/*.jar app.jar

# Настраиваем параметры запуска
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]