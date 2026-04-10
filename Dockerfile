FROM eclipse-temurin:26-alpine AS builder

WORKDIR /app

# bump: FFmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|/\d+\./|*
COPY --from=mwader/static-ffmpeg:8.1 /ff* /usr/bin/

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew check installDist --no-daemon

FROM alpine:3.23.3

COPY --from=builder /usr/bin/ff* /usr/bin/
COPY --from=builder /app/build/install/Stickerify/ .

ENV CONCURRENT_PROCESSES=5
CMD ["./bin/Stickerify"]
