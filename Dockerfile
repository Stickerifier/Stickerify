FROM eclipse-temurin:26-alpine AS builder

WORKDIR /app

# bump: FFmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|/\d+\./|*
COPY --from=mwader/static-ffmpeg:9.0.2 /ff* /usr/bin/

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew check installDist --no-daemon

FROM alpine:3.24.2

COPY --from=builder /usr/bin/ff* /usr/bin/
COPY --from=builder /app/build/install/Stickerify/ .

ENV OTEL_SDK_DISABLED=true
ENV OTEL_METRICS_EXPORTER=none
ENV OTEL_LOGS_EXPORTER=none
ENV OTEL_JAVAAGENT_LOGGING=none
ENV CONCURRENT_PROCESSES=5
CMD ["./bin/Stickerify"]
