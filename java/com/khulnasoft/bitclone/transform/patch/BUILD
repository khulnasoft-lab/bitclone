# Copyright 2018 KhulnaSoft Ltd..
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
    name = "patch",
    srcs = [
        "PatchModule.java",
        "PatchTransformation.java",
        "PatchingOptions.java",
        "QuiltTransformation.java",
    ],
    deps = [
        "//java/com/khulnasoft.bitclone:base",
        "//java/com/khulnasoft.bitclone:general_options",
        "//java/com/khulnasoft.bitclone/config:base",
        "//java/com/khulnasoft.bitclone/doc:annotations",  # Keep it, needed for doc generation
        "//java/com/khulnasoft.bitclone/exception",
        "//java/com/khulnasoft.bitclone/git:environment",
        "//java/com/khulnasoft.bitclone/util",
        "//java/com/khulnasoft.bitclone/util/console",
        "//third_party:guava",
        "//third_party:jcommander",
        "//third_party:jsr305",
        "//third_party:shell",
        "//third_party:starlark",
    ],
)
