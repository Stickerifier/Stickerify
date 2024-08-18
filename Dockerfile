ARG LIBWEBP=libwebp-1.4.0-linux-x86-64

FROM eclipse-temurin AS builder
ARG LIBWEBP
RUN curl -LO https://storage.googleapis.com/downloads.webmproject.org/releases/webp/$LIBWEBP.tar.gz && \
    tar -xvzf $LIBWEBP.tar.gz -C /tmp
WORKDIR /app
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew runtime --no-daemon

FROM debian:trixie-slim AS bot
ARG LIBWEBP
COPY --from=builder /app/build/jre ./jre
COPY --from=builder /app/build/libs/Stickerify-shadow.jar .
COPY --from=builder /tmp/$LIBWEBP/bin/cwebp /usr/local/bin/
COPY --from=builder /tmp/$LIBWEBP/bin/dwebp /usr/local/bin/
COPY --from=mwader/static-ffmpeg /ffmpeg /usr/local/bin/
CMD ["jre/bin/java", "-Dcom.sksamuel.scrimage.webp.binary.dir=/usr/local/bin/", "-jar", "Stickerify-shadow.jar"]
