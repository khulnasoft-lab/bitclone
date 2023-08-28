licenses(["notice"])

package(default_visibility = ["//visibility:public"])

java_plugin(
    name = "document",
    srcs = [
        "AnnotationProcessor.java",
    ] + glob([
        "annotations/**/*.java",
    ]),
    javacopts = [
        "-Xlint:unchecked",
    ],
    processor_class = "com.khulnasoft.bitclone.doc.AnnotationProcessor",
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//third_party:autocommon",
        "//third_party:guava",
        "//third_party:starlark",
    ],
)

java_library(
    name = "generator-lib",
    srcs = [
        "DocBase.java",
        "Generator.java",
        "MarkdownRenderer.java",
        "ModuleLoader.java",
    ],
    deps = [
        ":annotations",
        "//java/com/khulnasoft.bitclone/jcommander:converters",
        "//third_party:guava",
        "//third_party:jcommander",
        "//third_party:jsr305",
        "//third_party:re2j",
        "//third_party:starlark",
    ],
)

java_library(
    name = "annotations",
    srcs = glob(["annotations/**/*.java"]),
    exported_plugins = [":document"],
    javacopts = [
        "-Xlint:unchecked",
    ],
    tags = ["keep_dep"],
    visibility = [
        "//visibility:public",
    ],
)