FROM ubuntu:22.04 AS build

ARG GRAALVM_VERSION=22.3.0
ARG JAVA_VERSION=19
ARG GRADLE_VERSION=7.6-rc-3

RUN apt-get update -y && apt-get upgrade -y && apt-get install -y wget unzip build-essential zlib1g-dev upx && apt-get autoremove --purge -y \
 && wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAALVM_VERSION}/graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAALVM_VERSION}.tar.gz -P /tmp \
 && tar zxvf /tmp/graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAALVM_VERSION}.tar.gz -C /opt \
 && wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp \
 && unzip -d /opt /tmp/gradle-${GRADLE_VERSION}-bin.zip

ARG MUSL_VERSION=10.2.1
ARG ZLIB_VERSION=1.2.13

RUN wget http://more.musl.cc/${MUSL_VERSION}/x86_64-linux-musl/x86_64-linux-musl-native.tgz -P /tmp \
 && mkdir /opt/musl-${MUSL_VERSION} \
 && tar -zxvf /tmp/x86_64-linux-musl-native.tgz -C /opt/musl-${MUSL_VERSION}/ \
 && wget https://zlib.net/zlib-${ZLIB_VERSION}.tar.gz -P /tmp \
 && tar -zxvf /tmp/zlib-${ZLIB_VERSION}.tar.gz -C /tmp

ENV TOOLCHAIN_DIR=/opt/musl-${MUSL_VERSION}/x86_64-linux-musl-native

ENV PATH=${TOOLCHAIN_DIR}/bin:${PATH}
ENV CC=${TOOLCHAIN_DIR}/bin/gcc

WORKDIR /tmp/zlib-${ZLIB_VERSION}
RUN ./configure --prefix=${TOOLCHAIN_DIR} --static && make && make install

ENV GRADLE_HOME=/opt/gradle-${GRADLE_VERSION}
ENV GRAALVM_HOME=/opt/graalvm-ce-java${JAVA_VERSION}-${GRAALVM_VERSION}
ENV JAVA_HOME=${GRAALVM_HOME}
ENV PATH=${GRAALVM_HOME}/bin:${GRADLE_HOME}/bin:${PATH}

RUN gu install native-image

RUN rm -rf /tmp/*

WORKDIR /app
COPY . ./

RUN gradle nativeCompile --no-daemon

RUN upx --lzma --best -o stickerify-upx build/native/nativeCompile/Stickerify

FROM scratch
ARG STICKERIFY_TOKEN
ENV STICKERIFY_TOKEN $STICKERIFY_TOKEN
COPY --from=build /app/stickerify-upx /
ENTRYPOINT ["/stickerify-upx", "-Djava.io.tmpdir=/"]
