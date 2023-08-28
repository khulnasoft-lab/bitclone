"""Bazel rule to generate bitclone reference docs."""

def _doc_generator_impl(ctx):
    jars = []
    for target in ctx.attr.targets:
        for jar in target[JavaInfo].transitive_source_jars.to_list():
            jars.append(jar)

    ctx.actions.run(
        inputs = jars + ctx.files.template_file,
        outputs = [ctx.outputs.out],
        executable = ctx.executable.generator,
        arguments = [
            ",".join([j.path for j in jars]),
            ctx.outputs.out.path,
        ] + [f.path for f in ctx.files.template_file],
    )

# Generates documentation by scanning the transitive set of dependencies of a Java binary.
doc_generator = rule(
    attrs = {
        "targets": attr.label_list(allow_rules = [
            "java_binary",
            "java_library",
        ]),
        "generator": attr.label(
            executable = True,
            cfg = "exec",
            mandatory = True,
        ),
        "template_file": attr.label(mandatory = False, allow_single_file = True),
        "out": attr.output(mandatory = True),
    },
    implementation = _doc_generator_impl,
    output_to_genfiles = True,
)

def bitclone_reference(name, *, out, libraries, template_file = None):
    """
    Auto-generate reference documentation for a target containing bitclone libraries.

    out: Name of the output file to generate.
    libraries: List of libraries for which to generate reference documentation.
    template_file: Optional template file in which to insert the generated reference.
    """
    native.java_binary(
        name = "generator",
        main_class = "com.khulnasoft.bitclone.doc.Generator",
        runtime_deps = ["//java/com/khulnasoft/bitclone/doc:generator-lib"] + libraries,
    )

    doc_generator(
        name = name,
        out = out,
        generator = ":generator",
        targets = libraries,
        template_file = template_file,
    )
