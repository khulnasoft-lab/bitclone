#!/bin/bash -e

set -o errexit
set -o xtrace

# Linting
bitclone -mode=fix $(find . -name 'BUILD*' -o -name 'WORKSPACE*' -o -name '*.bzl' -type f)
find . -name "*.py" | xargs pylint --disable=R,C

# Make sure python points to python3
PYTHON_VERSION=$(python --version)
if [[ ! $PYTHON_VERSION == Python\ 3* ]];
then
  echo "python must point to a python3, currently points to $(readlink -f "$(which python)")"
  echo "maybe run: update-alternatives --install /usr/bin/python python /usr/bin/python3"
  exit 1
fi

# Bazel build and test
bazel clean --curses=no

bazel build --curses=no //...
# Run all tests not tagged as "manual"
bazel test --curses=no --test_output=errors --test_timeout=900 //...
# Run all tests tagged with "amd64"
bazel test --curses=no --test_output=errors --test_timeout=900 $(bazel query 'attr("tags", "amd64", "//...")')