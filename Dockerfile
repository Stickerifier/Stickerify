FROM eclipse-temurin:24-alpine AS builder

WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew jlink shadowJar

# bump: alpine /FROM alpine:([\d.]+)/ docker:alpine|^3
# bump: alpine link "Release notes" https://alpinelinux.org/posts/Alpine-$LATEST-released.html
FROM alpine:3.22.1 AS bot

RUN apk --no-cache add libwebp-tools

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|~7.0
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/bin/
ENV FFMPEG_PATH=/usr/bin/ffmpeg

COPY --from=builder /app/build/jlink/jre jre
COPY --from=builder /app/build/libs/Stickerify-1.0-all.jar Stickerify.jar

CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/bin/", "-jar", "Stickerify.jar"]
