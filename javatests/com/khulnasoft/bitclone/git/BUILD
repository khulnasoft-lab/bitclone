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

# Used for extending the tests internally. Not a public API.
package(default_visibility = ["//visibility:public"])

licenses(["notice"])

all_tests(
    name = "all_tests",
    env = {"LC_CTYPE": "en_US.UTF-8"},
    tags = ["local"],
    tests = glob(
        ["**/*.java"],
        exclude = [
            "GitDestinationTest.java",
            "GitTestUtil.java",
        ],
    ),
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone:bitclone_lib",
        "//java/com/khulnasoft.bitclone:general_options",
        "//java/com/khulnasoft.bitclone:labels",
        "//java/com/khulnasoft.bitclone/approval",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/checks",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/git",
        "//java/com/khulnasoft.bitclone/git:core",
        "//java/com/khulnasoft.bitclone/git:environment",
        "//java/com/khulnasoft.bitclone/git/github:api",
        "//java/com/khulnasoft.bitclone/git/github:util",
        "//java/com/khulnasoft.bitclone/git/github/api/testing:abstract_gitapi_test",
        "//java/com/khulnasoft.bitclone/git/testing",
        "//java/com/khulnasoft.bitclone/profiler",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/testing",
        "//java/com/khulnasoft.bitclone/testing/git",
        "//java/com/khulnasoft.bitclone/testing/profiler",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:google_http_client",
        "//third_party:google_http_client_test",
        "//third_party:guava_testlib",
        "//third_party:jcommander",
        "//third_party:jimfs",
        "//third_party:mockito",
        "//third_party:starlark",
        "//third_party:testparameterinjector",
        "//third_party:truth",
    ],
)

all_tests(
    name = "destination_test",
    shard_count = 5,
    tags = ["local"],
    tests = ["GitDestinationTest.java"],
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/checks",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/git",
        "//java/com/khulnasoft.bitclone/git:core",
        "//java/com/khulnasoft.bitclone/git:environment",
        "//java/com/khulnasoft.bitclone/git/testing",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/testing",
        "//java/com/khulnasoft.bitclone/testing/git",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:truth",
    ],
)
