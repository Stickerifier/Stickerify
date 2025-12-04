FROM eclipse-temurin:25-alpine AS builder

WORKDIR /app

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|~7.0
COPY --from=mwader/static-ffmpeg:7.0.2 /ff* /usr/bin/

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew check installDist --no-daemon

# bump: alpine /FROM alpine:([\d.]+)/ docker:alpine|^3
# bump: alpine link "Release notes" https://alpinelinux.org/posts/Alpine-$LATEST-released.html
FROM alpine:3.23.0

COPY --from=builder /usr/bin/ff* /usr/bin/
COPY --from=builder /app/build/install/Stickerify/ .

ENV CONCURRENT_PROCESSES=5
CMD ["./bin/Stickerify"]
