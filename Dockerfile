FROM eclipse-temurin:21 AS base

FROM base AS builder
WORKDIR /app
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
  ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew shadowJar --no-daemon

FROM base AS bot
WORKDIR /app
COPY --from=builder /app/build/libs/Stickerify-shadow.jar .
COPY --from=mwader/static-ffmpeg:6.0 /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg
CMD ["java", "-jar", "Stickerify-shadow.jar"]
