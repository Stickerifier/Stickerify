FROM eclipse-temurin AS builder
RUN apt-get update && apt-get install -y webp
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
COPY --from=builder /usr/bin/cwebp /usr/local/bin/cwebp
COPY --from=builder /usr/bin/dwebp /usr/local/bin/dwebp
COPY --from=builder /lib/x86_64-linux-gnu/libwebpdemux.so.2 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libwebp.so.7 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libsharpyuv.so.0 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libpng16.so.16 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libjpeg.so.8 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libtiff.so.6 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libc.so.6 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libm.so.6 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libz.so.1 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libzstd.so.1 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/liblzma.so.5 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libLerc.so.4 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libjbig.so.0 /usr/lib/
COPY --from=builder "/lib/x86_64-linux-gnu/libstdc++.so.6" /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libgcc_s.so.1 /usr/lib/
COPY --from=builder /lib/x86_64-linux-gnu/libdeflate.so.0 /usr/lib/
COPY --from=mwader/static-ffmpeg /ffmpeg /usr/local/bin/
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg
ENV LOG_LEVEL=debug
CMD ["jre/bin/java", "-jar", "Stickerify-shadow.jar"]
