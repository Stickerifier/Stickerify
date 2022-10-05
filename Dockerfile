FROM maven:3.8-eclipse-temurin-19-alpine
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
ARG STICKERIFY_TOKEN
ENV STICKERIFY_TOKEN $STICKERIFY_TOKEN
RUN mvn -f /usr/src/app/pom.xml clean package
CMD ["java","-jar","/usr/src/app/target/Stickerify.jar"]
