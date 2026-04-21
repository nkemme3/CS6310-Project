# syntax=docker/dockerfile:1.6
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY backend/pom.xml ./pom.xml
RUN mvn -B -q -Dmaven.test.skip=true dependency:go-offline
COPY backend/src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /build/target/powergrid.jar /app/powergrid.jar
EXPOSE 8080
ENV POWERGRID_DATA_DIR=/app/data
ENTRYPOINT ["java","-jar","/app/powergrid.jar"]
