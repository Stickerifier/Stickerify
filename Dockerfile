FROM gradle:7.6-jdk19 AS builder
COPY . /app
WORKDIR /app
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:19
ARG STICKERIFY_TOKEN
ENV STICKERIFY_TOKEN $STICKERIFY_TOKEN
COPY --from=builder /app/build/libs /app
WORKDIR /app
CMD ["java", "-jar", "Stickerify-shadow.jar"]
