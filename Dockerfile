FROM azul/zulu-openjdk-alpine:24 AS builder

# bump: libwebp /LIBWEBP_VERSION=([\d.]+)/ git:https://chromium.googlesource.com/webm/libwebp.git|^1
# bump: libwebp after ./hashupdate Dockerfile LIBWEBP $LATEST
ARG LIBWEBP_VERSION=1.6.0
ARG LIBWEBP_SHA256=1c5ffab71efecefa0e3c23516c3a3a1dccb45cc310ae1095c6f14ae268e38067
ARG LIBWEBP_FILE="libwebp-$LIBWEBP_VERSION-linux-x86-64.tar.gz"
ARG LIBWEBP_URL="https://storage.googleapis.com/downloads.webmproject.org/releases/webp/$LIBWEBP_FILE"

WORKDIR /app
RUN apk --no-cache add binutils curl tar
RUN curl -L --fail --retry 3 --retry-delay 5 "$LIBWEBP_URL" -O && \
    echo "$LIBWEBP_SHA256 $LIBWEBP_FILE" | sha256sum -c - && \
    tar -xzf "$LIBWEBP_FILE" --one-top-level=libwebp --strip-components=1 && \
    rm "$LIBWEBP_FILE"

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"
COPY *.gradle gradle.* gradlew ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies
COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew jre shadowJar

FROM alpine AS bot

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|~7.0
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg

COPY --from=builder /app/libwebp/bin/cwebp /usr/local/bin/
COPY --from=builder /app/libwebp/bin/dwebp /usr/local/bin/

COPY --from=builder /app/build/jre jre
COPY --from=builder /app/build/libs/Stickerify-1.0-all.jar Stickerify.jar

CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/local/bin/", "-jar", "Stickerify.jar"]
