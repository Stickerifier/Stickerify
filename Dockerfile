FROM eclipse-temurin:24-alpine AS builder

# bump: libwebp /LIBWEBP_VERSION=([\d.]+)/ git:https://chromium.googlesource.com/webm/libwebp.git|^1
# bump: libwebp after ./hashupdate Dockerfile LIBWEBP $LATEST
ARG LIBWEBP_VERSION=1.6.0
ARG LIBWEBP_SHA256=1c5ffab71efecefa0e3c23516c3a3a1dccb45cc310ae1095c6f14ae268e38067
ARG LIBWEBP_URL="https://storage.googleapis.com/downloads.webmproject.org/releases/webp/libwebp-$LIBWEBP_VERSION-linux-x86-64.tar.gz"

WORKDIR /app
RUN apk --no-cache add binutils curl tar
RUN curl -L --fail --retry 3 --retry-delay 5 "$LIBWEBP_URL" -o /tmp/libwebp.tar.gz && \
    echo "$LIBWEBP_SHA256  /tmp/libwebp.tar.gz" | sha256sum -c - && \
    tar -xzf /tmp/libwebp.tar.gz --one-top-level=libwebp --strip-components=1 && \
    rm /tmp/libwebp.tar.gz

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew jlink shadowJar

# bump: alpine /FROM alpine:([\d.]+)/ docker:alpine|^3
# bump: alpine link "Release notes" https://alpinelinux.org/posts/Alpine-$LATEST-released.html
FROM alpine:3.22.1 AS bot

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|~7.0
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg

COPY --from=builder /app/libwebp/bin/cwebp /usr/local/bin/
COPY --from=builder /app/libwebp/bin/dwebp /usr/local/bin/

COPY --from=builder /app/build/jlink/jre jre
COPY --from=builder /app/build/libs/Stickerify-1.0-all.jar Stickerify.jar

CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/local/bin/", "-jar", "Stickerify.jar"]
