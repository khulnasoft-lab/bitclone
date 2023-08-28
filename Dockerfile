FROM gcr.io/bazel-public/bazel:6.0.0 AS build

USER root
COPY . .
RUN ./cloudbuild.sh build //java/com/khulnasoft/bitclone:bitclone_deploy.jar
RUN mkdir -p /tmp/bitclone && \
    cp bazel-bin/java/com/khulnasoft/bitclone/bitclone_deploy.jar /tmp/bitclone/

USER ubuntu
FROM golang:latest AS buildtools
RUN go install github.com/bazelbuild/buildtools/buildozer@latest
RUN go install github.com/bazelbuild/buildtools/buildifier@latest

FROM openjdk:11-jre-slim
WORKDIR /usr/src/app
ENV BITCLONE_CONFIG=copy.bara.sky \
    BITCLONE_SUBCOMMAND=migrate \
    BITCLONE_OPTIONS='' \
    BITCLONE_WORKFLOW=default \
    BITCLONE_SOURCEREF=''
COPY --from=build /tmp/bitclone/ /opt/bitclone/
COPY --from=buildtools /go/bin/buildozer /go/bin/buildifier /usr/bin/
COPY .docker/entrypoint.sh /usr/local/bin/bitclone
RUN chmod +x /usr/local/bin/bitclone
RUN apt-get update && \
    apt-get install -y git && \
    apt-get clean
