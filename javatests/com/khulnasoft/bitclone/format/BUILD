# Copyright 2016 KhulnaSoft Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

load("//javatests/com/khulnasoft.bitclone:test.bzl", "all_tests")

licenses(["notice"])

# This is required because golang binaries have the build architecture in their
# path.
genrule(
    name = "normalised_buildifier",
    testonly = 1,
    srcs = ["@buildtools//buildifier"],
    outs = ["buildifier"],
    cmd = "cp $(SRCS) $@",
)

all_tests(
    name = "all_tests",
    data = [":normalised_buildifier"],
    tests = glob(["**/*Test.java"]),
    deps = [
        "//java/com/khulnasoft/bitclone:base",
        "//java/com/khulnasoft/bitclone/exception",
        "//java/com/khulnasoft/bitclone/format",
        "//java/com/khulnasoft/bitclone/testing",
        "//java/com/khulnasoft/bitclone/util/console/testing",
        "//third_party:truth",
    ],
)
