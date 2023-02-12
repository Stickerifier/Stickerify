FROM gradle:7.6-jdk19 AS builder
COPY . /app
WORKDIR /app
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:19
ARG STICKERIFY_TOKEN
ENV STICKERIFY_TOKEN $STICKERIFY_TOKEN
COPY --from=builder /app/build/libs /app
WORKDIR /app
RUN apt-get -y update && apt-get -y upgrade && apt-get install -y --no-install-recommends ffmpeg
CMD ["java", "-jar", "Stickerify-shadow.jar"]
