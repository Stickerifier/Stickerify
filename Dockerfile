FROM gradle:8.1-jdk17 AS builder
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY gradle/libs.versions.toml ./gradle/
RUN \
  --mount=type=cache,target=/home/gradle/.gradle/caches \
  gradle dependencies --no-daemon
COPY . .
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:20 AS bot
WORKDIR /app
RUN \
  --mount=type=cache,target=/var/cache/apt \
  apt-get -y update && apt-get -y upgrade && \
  apt-get install -y --no-install-recommends ffmpeg
COPY --from=builder /app/build/libs .
CMD ["java", "-jar", "Stickerify-shadow.jar"]
