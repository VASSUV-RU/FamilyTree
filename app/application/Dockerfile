# syntax=docker/dockerfile:1

FROM gradle:8.8-jdk24-alpine AS build
WORKDIR /workspace

# Leverage build cache by copying build scripts first
COPY settings.gradle.kts build.gradle.kts ./
COPY app/api/build.gradle.kts app/api/build.gradle.kts
COPY app/service/build.gradle.kts app/service/build.gradle.kts
COPY app/data/build.gradle.kts app/data/build.gradle.kts
COPY app/bot-telegram/build.gradle.kts app/bot-telegram/build.gradle.kts
COPY app/application/build.gradle.kts app/application/build.gradle.kts

RUN --mount=type=cache,target=/home/gradle/.gradle gradle help

# Copy sources
COPY . .

# Build the application boot jar (pulling in subprojects)
RUN --mount=type=cache,target=/home/gradle/.gradle gradle :app:application:bootJar --no-daemon

FROM eclipse-temurin:24-jre
WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /workspace/app/application/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
