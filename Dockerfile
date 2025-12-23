FROM eclipse-temurin:25-alpine AS builder

WORKDIR /app

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|/\d+\./|*
COPY --from=mwader/static-ffmpeg:8.0.1 /ff* /usr/bin/

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew check installDist --no-daemon

FROM alpine:3.23.2

COPY --from=builder /usr/bin/ff* /usr/bin/
COPY --from=builder /app/build/install/Stickerify/ .

ENV CONCURRENT_PROCESSES=5
CMD ["./bin/Stickerify"]
