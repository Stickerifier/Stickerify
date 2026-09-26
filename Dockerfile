FROM eclipse-temurin:26-alpine AS builder

WORKDIR /app

# bump: FFmpeg /static-ffmpeg:([\d.]+)/ docker:mwader/static-ffmpeg|/\d+\./|*
COPY --from=mwader/static-ffmpeg:9.0.2 /ff* /usr/bin/

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew check installDist :otel-agent-extension:jar --no-daemon

FROM alpine:3.24.2

# bump: OpenTelemetry /v([\d.]+)/ git:https://github.com/open-telemetry/opentelemetry-java-instrumentation.git|*|sort
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.31.1/opentelemetry-javaagent.jar \
    /app/opentelemetry-agent.jar

COPY --from=builder /usr/bin/ff* /usr/bin/
COPY --from=builder /app/build/install/Stickerify/ .
COPY --from=builder /app/otel-agent-extension/build/libs/otel-agent-extension.jar /app/otel-agent-extension.jar

ENV OTEL_SDK_DISABLED=true
ENV JAVA_TOOL_OPTIONS=-javaagent:/app/opentelemetry-agent.jar
ENV OTEL_JAVAAGENT_EXTENSIONS=/app/otel-agent-extension.jar
ENV CONCURRENT_PROCESSES=5
CMD ["./bin/Stickerify"]
