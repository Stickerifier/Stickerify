FROM eclipse-temurin AS builder

# bump: libwebp /LIBWEBP_VERSION=([\d.]+)/ git:https://chromium.googlesource.com/webm/libwebp.git|*
# bump: libwebp after ./hashupdate Dockerfile LIBWEBP $LATEST
# bump: libwebp link "NEWS" https://github.com/webmproject/libwebp/blob/$LATEST/NEWS
ARG LIBWEBP_VERSION=1.4.0
ARG LIBWEBP_URL="https://storage.googleapis.com/downloads.webmproject.org/releases/webp/libwebp-$LIBWEBP_VERSION-linux-x86-64.tar.gz"
ARG LIBWEBP_SHA256=94ac053be5f8cb47a493d7a56b2b1b7328bab9cff24ecb89fa642284330d8dff

WORKDIR /app
RUN curl "$LIBWEBP_URL" -o libwebp.tar.gz
RUN echo "$LIBWEBP_SHA256  libwebp.tar.gz" | sha256sum -c -
RUN tar -xzf libwebp.tar.gz --one-top-level=libwebp --strip-components=1
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew runtime --no-daemon

FROM gcr.io/distroless/base-nossl:nonroot AS bot
COPY --from=builder /app/build/jre ./jre
COPY --from=builder /app/build/libs/Stickerify-shadow.jar .
COPY --from=builder /app/libwebp/bin/cwebp /usr/local/bin/
COPY --from=builder /app/libwebp/bin/dwebp /usr/local/bin/
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg
CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/local/bin/", "-jar", "Stickerify-shadow.jar"]
