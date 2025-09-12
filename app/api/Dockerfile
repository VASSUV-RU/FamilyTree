# syntax=docker/dockerfile:1

FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /workspace

# Leverage build cache by copying build scripts first
COPY settings.gradle.kts build.gradle.kts ./
COPY api/build.gradle.kts api/build.gradle.kts
COPY service/build.gradle.kts service/build.gradle.kts
COPY data/build.gradle.kts data/build.gradle.kts
COPY bot-telegram/build.gradle.kts bot-telegram/build.gradle.kts

RUN --mount=type=cache,target=/home/gradle/.gradle gradle help

# Copy sources
COPY . .

# Build only the API boot jar (pulling in subprojects)
RUN --mount=type=cache,target=/home/gradle/.gradle gradle :api:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /workspace/api/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

