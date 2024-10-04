FROM eclipse-temurin AS builder
ARG LIBWEBP=libwebp-1.4.0-linux-x86-64
RUN curl -s -L https://storage.googleapis.com/downloads.webmproject.org/releases/webp/${LIBWEBP}.tar.gz | \
    tar -xvzf - -C /tmp --one-top-level=libwebp --strip-components=1
WORKDIR /app
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew runtime --no-daemon

FROM gcr.io/distroless/base-nossl:nonroot AS bot
COPY --from=builder /app/build/jre ./jre
COPY --from=builder /app/build/libs/Stickerify-shadow.jar .
COPY --from=builder /tmp/libwebp/bin/cwebp /usr/local/bin/
COPY --from=builder /tmp/libwebp/bin/dwebp /usr/local/bin/
COPY --from=mwader/static-ffmpeg:7.0.2 /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg
CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/local/bin/", "-jar", "Stickerify-shadow.jar"]
