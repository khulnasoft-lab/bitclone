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

load(":docs.bzl", "bitclone_reference")
load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

licenses(["notice"])

package(default_visibility = ["//visibility:public"])

exports_files(
    [
        "doc_skylark.sh",
        "docs.bzl",
    ],
    visibility = ["//visibility:public"],
)

JAVACOPTS = [
    "-Xlint:unchecked",
]

java_binary(
    name = "bitclone",
    javacopts = JAVACOPTS,
    main_class = "com.khulnasoft.bitclone.Main",
    runtime_deps = [
        ":bitclone_main",
    ],
)

java_library(
    name = "bitclone_main",
    srcs = ["Main.java"],
    javacopts = JAVACOPTS,
    deps = [
        ":base",
        ":bitclone_lib",
        ":general_options",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/jcommander:converters",
        "//java/com/khulnasoft.bitclone/onboard",
        "//java/com/khulnasoft.bitclone/profiler",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//third_party:flogger",
        "//third_party:guava",
        "//third_party:jcommander",
        "//third_party:jsr305",
        "//third_party:starlark",
    ],
)

bitclone_reference(
    name = "generate_reference",
    out = "reference.md",
    libraries = ["//java/com/khulnasoft.bitclone:bitclone_main"],
)

BASE_SRCS = [
    "BaselinesWithoutLabelVisitor.java",
    "ChangeMessage.java",
    "ChangeVisitable.java",
    "CheckoutPath.java",
    "CheckoutFileSystem.java",
    "CheckoutPathAttributes.java",
    "ConfigItemDescription.java",
    "Destination.java",
    "DestinationInfo.java",
    "DestinationReader.java",
    "DestinationStatusVisitor.java",
    "Endpoint.java",
    "EndpointProvider.java",
    "Info.java",
    "LazyResourceLoader.java",
    "Metadata.java",
    "MigrationInfo.java",
    "Option.java",
    "Options.java",
    "Origin.java",
    "SkylarkContext.java",
    "Transformation.java",
    "TransformResult.java",
    "TransformationStatus.java",
    "TransformWork.java",
    "Trigger.java",
    "treestate/TreeState.java",
    "treestate/TreeStateUtil.java",
    "WorkflowOptions.java",
    "WriterContext.java",
]

java_library(
    name = "labels",
    srcs = [
        "LabelFinder.java",
    ],
    javacopts = JAVACOPTS,
    deps = [
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/templatetoken",
        "//third_party:guava",
        "//third_party:re2j",
    ],
)

java_library(
    name = "options",
    srcs = [
        "Option.java",
        "Options.java",
    ],
    javacopts = JAVACOPTS,
    deps = ["//third_party:guava"],
)

java_library(
    name = "moduleset",
    srcs = ["ModuleSet.java"],
    javacopts = JAVACOPTS,
    deps = [
        ":options",
        "//third_party:guava",
    ],
)

java_library(
    name = "base",
    srcs = BASE_SRCS,
    javacopts = JAVACOPTS,
    deps = [
        ":labels",
        ":local_parallelizer",
        "//java/com/khulnasoft.bitclone/approval",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/doc:annotations",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/jcommander:converters",
        "//java/com/khulnasoft.bitclone/jcommander:validators",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//third_party:autovalue",
        "//third_party:error_prone",
        "//third_party:flogger",
        "//third_party:guava",
        "//third_party:jcommander",
        "//third_party:jsr305",
        "//third_party:starlark",
    ],
)

java_library(
    name = "general_options",
    srcs = ["GeneralOptions.java"],
    javacopts = JAVACOPTS,
    deps = [
        ":base",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/jcommander:converters",
        "//java/com/khulnasoft.bitclone/monitor",
        "//java/com/khulnasoft.bitclone/profiler",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//third_party:flogger",
        "//third_party:guava",
        "//third_party:jcommander",
        "//third_party:jsr305",
        "//third_party:shell",
    ],
)

java_library(
    name = "local_parallelizer",
    srcs = ["LocalParallelizer.java"],
    javacopts = JAVACOPTS,
    deps = [
        "//java/com/khulnasoft.bitclone/exception",
        "//third_party:guava",
    ],
)

java_library(
    name = "starlark_date_time_module",
    srcs = ["StarlarkDateTimeModule.java"],
    javacopts = JAVACOPTS,
    deps = [
        "//java/com/khulnasoft.bitclone/exception",
        "//third_party:guava",
        "//third_party:starlark",
    ],
)

java_library(
    name = "bitclone_lib",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "Main.java",
            "GeneralOptions.java",
            "StarlarkDateTimeModule.java",
            "LabelFinder.java",
        ] + BASE_SRCS,
    ),
    javacopts = JAVACOPTS,
    deps = [
        ":base",
        ":general_options",
        ":labels",
        ":starlark_date_time_module",
        "//java/com/khulnasoft.bitclone/action",
        "//java/com/khulnasoft.bitclone/archive",
        "//java/com/khulnasoft.bitclone/authoring",
        "//java/com/khulnasoft.bitclone/buildozer",
        "//java/com/khulnasoft.bitclone/buildozer:buildozer_options",
        "//java/com/khulnasoft.bitclone/compression",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/config:global_migrations",
        "//java/com/khulnasoft.bitclone/config:parser",
        "//java/com/khulnasoft.bitclone/doc:annotations",
        "//java/com/khulnasoft.bitclone/effect",
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/format",
        "//java/com/khulnasoft.bitclone/git",
        "//java/com/khulnasoft.bitclone/go",
        "//java/com/khulnasoft.bitclone/hashing",
        "//java/com/khulnasoft.bitclone/hg",
        "//java/com/khulnasoft.bitclone/http",
        "//java/com/khulnasoft.bitclone/monitor",
        "//java/com/khulnasoft.bitclone/onboard:options",
        "//java/com/khulnasoft.bitclone/profiler",
        "//java/com/khulnasoft.bitclone/python",
        "//java/com/khulnasoft.bitclone/re2",
        "//java/com/khulnasoft.bitclone/remotefile",
        "//java/com/khulnasoft.bitclone/revision",
        "//java/com/khulnasoft.bitclone/rust",
        "//java/com/khulnasoft.bitclone/templatetoken",
        "//java/com/khulnasoft.bitclone/toml",
        "//java/com/khulnasoft.bitclone/transform",
        "//java/com/khulnasoft.bitclone/transform/debug",
        "//java/com/khulnasoft.bitclone/transform/patch",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//java/com/khulnasoft.bitclone/version",
        "//java/com/khulnasoft.bitclone/xml",
        "//third_party:autovalue",
        "//third_party:error_prone",
        "//third_party:flogger",
        "//third_party:guava",
        "//third_party:jcommander",
        "//third_party:jsr305",
        "//third_party:re2j",
        "//third_party:starlark",
    ],
)

bzl_library(
    name = "docs_bzl",
    srcs = ["docs.bzl"],
    visibility = ["//visibility:private"],
)
