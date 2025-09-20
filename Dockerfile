FROM eclipse-temurin:24-alpine AS builder

WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew installDist

# bump: alpine /FROM alpine:([\d.]+)/ docker:alpine|^3
# bump: alpine link "Release notes" https://alpinelinux.org/posts/Alpine-$LATEST-released.html
FROM alpine:3.22.1 AS bot

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|~7.0
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/bin/
COPY --from=builder /app/build/install/Stickerify/ .

ENV CONCURRENT_PROCESSES=5
ENV FFMPEG_PATH=/usr/bin/ffmpeg
CMD ["./bin/Stickerify"]
