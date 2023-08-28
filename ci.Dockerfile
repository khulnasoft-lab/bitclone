FROM gcr.io/bazel-public/bazel${BAZEL_VERSION} AS build
COPY . .
USER root
RUN ./cloudbuild_setup.sh
USER ubuntu
