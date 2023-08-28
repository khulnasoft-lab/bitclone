load("@bazel_tools//tools/build_defs/repo:utils.bzl", "maybe")
load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies", "go_repository")

def bitclone_go_repositories():
    go_rules_dependencies()

    go_register_toolchains(version = "1.19.5")

    gazelle_dependencies()

    # LICENSE: The Apache Software License, Version 2.0
    maybe(
        go_repository,
        name = "skylark_syntax",
        importpath = "go.starlark.net",
        sum = "h1:Qoe+9POtDT51UBQ8XEnS9QKeHDQzEl2QRh3eok9R4aw=",
        version = "v0.0.0-20200203144150-6677ee5c7211",
    )
