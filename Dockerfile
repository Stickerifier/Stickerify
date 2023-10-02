FROM openjdk:21 AS builder
WORKDIR /app
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
RUN \
  --mount=type=cache,target=/var/cache/dnf \
  microdnf install findutils
RUN \
  --mount=type=cache,target=/home/gradle/.gradle/caches \
  ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:21 AS bot
WORKDIR /app
COPY --from=mwader/static-ffmpeg:6.0 /ffmpeg /usr/local/bin/
COPY --from=builder /app/build/libs .
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg
CMD ["java", "--enable-preview", "-jar", "Stickerify-shadow.jar"]
