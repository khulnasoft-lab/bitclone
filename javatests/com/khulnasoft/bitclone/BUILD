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

load(":test.bzl", "all_tests")
load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

licenses(["notice"])

LOCAL_TESTS = [
    "InfoTest.java",
    "MigrateCmdTest.java",
    "util/DiffUtilTest.java",
    "util/AutoPatchUtilTest.java",
    "WorkflowTest.java",
]

all_tests(
    name = "info_test",
    shard_count = 3,
    tags = ["local"],
    tests = ["InfoTest.java"],
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone:bitclone_lib",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/config:parser",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/testing",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:mockito",
        "//third_party:truth",
    ],
)

all_tests(
    name = "migrate_cmd_test",
    shard_count = 2,
    tags = ["local"],
    tests = ["MigrateCmdTest.java"],
    deps = [
        "//java/com/khulnasoft.bitclone:bitclone_lib",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/git",
        "//java/com/khulnasoft.bitclone/git:core",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/testing",
        "//java/com/khulnasoft.bitclone/testing/git",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:truth",
    ],
)

all_tests(
    name = "diff_util_test",
    shard_count = 5,
    tags = ["local"],
    tests = ["util/DiffUtilTest.java"],
    deps = [
        "//java/com/khulnasoft.bitclone/util",
        "//third_party:truth",
    ],
)

all_tests(
    name = "auto_patch_util_test",
    shard_count = 2,
    tags = ["local"],
    tests = ["util/AutoPatchUtilTest.java"],
    deps = [
        "//java/com/khulnasoft.bitclone/util",
        "//third_party:truth",
    ],
)

all_tests(
    name = "workflow_test",
    shard_count = 5,
    tags = ["local"],
    tests = ["WorkflowTest.java"],
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone:bitclone_lib",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/git:core",
        "//java/com/khulnasoft.bitclone/hg",
        "//java/com/khulnasoft.bitclone/monitor",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/testing",
        "//java/com/khulnasoft.bitclone/testing/git",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:jimfs",
        "//third_party:starlark",
        "//third_party:truth",
    ],
)

all_tests(
    name = "all_tests",
    tests = glob(
        ["**/*Test.java"],
        exclude = LOCAL_TESTS,
    ),
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone:bitclone_lib",
        "//java/com/khulnasoft.bitclone:bitclone_main",
        "//java/com/khulnasoft.bitclone:labels",
        "//java/com/khulnasoft.bitclone:starlark_date_time_module",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/checks",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/doc:annotations",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/monitor",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/testing",
        "//java/com/khulnasoft.bitclone/transform",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util:origin_util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/util/console/testing",
        "//third_party:google_code_gson",
        "//third_party:guava_testlib",
        "//third_party:jcommander",
        "//third_party:jimfs",
        "//third_party:mockito",
        "//third_party:re2j",
        "//third_party:shell",
        "//third_party:starlark",
        "//third_party:testparameterinjector",
        "//third_party:truth",
    ],
)

bzl_library(
    name = "test_bzl",
    srcs = ["test.bzl"],
    visibility = ["//visibility:private"],
)
