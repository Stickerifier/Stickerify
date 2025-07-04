FROM alpine
COPY --from=mwader/static-ffmpeg:7.0.2 /ff* /
ENTRYPOINT cp /ff* /out
