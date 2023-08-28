licenses(["notice"])

sh_test(
    name = "reference_doc_test",
    srcs = ["reference_doc_test.sh"],
    data = [
        "//docs:reference.md",
        "//java/com/khulnasoft/bitclone:reference.md",
        "//third_party/bazel/bashunit",
    ],
    visibility = ["//visibility:public"],
)

sh_test(
    name = "tool_test",
    srcs = ["tool_test.sh"],
    data = [
        "//java/com/khulnasoft/bitclone",
        "//third_party/bazel/bashunit",
    ],
    shard_count = 30,
    tags = ["local"],
    visibility = ["//visibility:public"],
)
