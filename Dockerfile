FROM gradle:8.0-jdk19 AS builder
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY gradle/libs.versions.toml ./gradle/
RUN \
  --mount=type=cache,target=/home/gradle/.gradle/caches \
  gradle dependencies --no-daemon
COPY . .
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:19 AS bot
RUN \
  --mount=type=cache,target=/var/cache/apt \
  apt-get -y update && apt-get -y upgrade && \
  apt-get install -y --no-install-recommends ffmpeg
ARG STICKERIFY_TOKEN
ENV STICKERIFY_TOKEN $STICKERIFY_TOKEN
WORKDIR /app
COPY --from=builder /app/build/libs .
CMD ["java", "-jar", "Stickerify-shadow.jar"]
