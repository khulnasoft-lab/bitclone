licenses(["notice"])

package(default_visibility = ["//visibility:public"])

JAVACOPTS = [
    "-Xlint:unchecked",
]

java_library(
    name = "action",
    srcs = [
        "Action.java",
        "ActionContext.java",
        "ActionResult.java",
        "StarlarkAction.java",
    ],
    javacopts = JAVACOPTS,
    deps = [
        "//java/com/khulnasoft/bitclone:base",
        "//java/com/khulnasoft/bitclone/config:base",
        "//java/com/khulnasoft/bitclone/effect",
        "//java/com/khulnasoft/bitclone/exception",
        "//java/com/khulnasoft/bitclone/revision",
        "//java/com/khulnasoft/bitclone/transform",
        "//java/com/khulnasoft/bitclone/util/console",
        "//third_party:guava",
        "//third_party:jsr305",
        "//third_party:starlark",
    ],
)
