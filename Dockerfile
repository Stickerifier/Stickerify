FROM eclipse-temurin AS builder

# bump: libwebp /LIBWEBP_VERSION=([\d.]+)/ https://github.com/webmproject/libwebp.git|^1
# bump: libwebp after ./hashupdate Dockerfile LIBWEBP $LATEST
# bump: libwebp link "Release notes" https://github.com/webmproject/libwebp/releases/tag/v$LATEST
# bump: libwebp link "Source diff $CURRENT..$LATEST" https://github.com/webmproject/libwebp/compare/v$CURRENT..v$LATEST
ARG LIBWEBP_VERSION=1.4.0
ARG LIBWEBP_URL="https://github.com/webmproject/libwebp/archive/v$$LIBWEBP_VERSION.tar.gz"
ARG LIBWEBP_SHA256=12af50c45530f0a292d39a88d952637e43fb2d4ab1883c44ae729840f7273381

WORKDIR /app
RUN curl "$LIBWEBP_URL" -o libwebp.tar.gz && \
    echo "$LIBWEBP_SHA256 libwebp.tar.gz" | sha256sum -c - && \
    tar -xzf libwebp.tar.gz --one-top-level=libwebp --strip-components=1
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew runtime --no-daemon

FROM gcr.io/distroless/base-nossl:nonroot AS bot

# bump: ffmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|~7.0
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg

COPY --from=builder /app/build/jre ./jre
COPY --from=builder /app/build/libs/Stickerify-shadow.jar .
COPY --from=builder /app/libwebp/bin/cwebp /usr/local/bin/
COPY --from=builder /app/libwebp/bin/dwebp /usr/local/bin/

CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/local/bin/", "-jar", "Stickerify-shadow.jar"]
