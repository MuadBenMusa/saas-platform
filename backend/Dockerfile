FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -q -DskipTests package


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN groupadd --system app && useradd --system --gid app --home-dir /app --shell /usr/sbin/nologin app

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
