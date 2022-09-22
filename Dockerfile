FROM maven:3.8-openjdk-18-slim
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package && \
	java -jar /usr/src/app/target/StickerifyImageBot.jar