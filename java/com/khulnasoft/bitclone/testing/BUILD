# Copyright 2016 KhulnaSoft Ltd..
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

licenses(["notice"])

package(default_visibility = ["//visibility:public"])

java_library(
    name = "testing",
    testonly = 1,
    srcs = glob(["**/*.java"]),
    javacopts = [
        "-Xlint:unchecked",
    ],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone:bitclone_lib",
        "//java/com/khulnasoft.bitclone:general_options",
        "//java/com/khulnasoft.bitclone:labels",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/buildozer:buildozer_options",
        "//java/com/khulnasoft.bitclone/checks",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/config:parser",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/format:buildifier_options",
        "//java/com/khulnasoft.bitclone/git",
        "//java/com/khulnasoft.bitclone/hg",
        "//java/com/khulnasoft.bitclone/http",
        "//java/com/khulnasoft.bitclone/monitor",
        "//java/com/khulnasoft.bitclone/onboard:options",
        "//java/com/khulnasoft.bitclone/remotefile",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/transform/debug",
        "//java/com/khulnasoft.bitclone/transform/patch",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:error_prone",
        "//third_party:google_http_client",
        "//third_party:guava",
        "//third_party:jimfs",
        "//third_party:jsr305",
        "//third_party:mockito",
        "//third_party:starlark",
        "//third_party:truth",
    ],
)
