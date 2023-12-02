# Bitclone

*A tool for transforming and moving code between repositories.*

Bitclone is a tool used internally at KhulnaSoft. It transforms and moves code between repositories.

Often, source code needs to exist in multiple repositories, and Bitclone allows you to transform
and move source code between these repositories. A common case is a project that involves
maintaining a confidential repository and a public repository in sync.

Bitclone requires you to choose one of the repositories to be the authoritative repository, so that
there is always one source of truth. However, the tool allows contributions to any repository, and
any repository can be used to cut a release.

The most common use case involves repetitive movement of code from one repository to another.
Bitclone can also be used for moving code once to a new repository.

Examples uses of Bitclone include:

  - Importing sections of code from a confidential repository to a public repository.

  - Importing code from a public repository to a confidential repository.

  - Importing a change from a non-authoritative repository into the authoritative repository. When
    a change is made in the non-authoritative repository (for example, a contributor in the public
    repository), Bitclone transforms and moves that change into the appropriate place in the
    authoritative repository. Any merge conflicts are dealt with in the same way as an out-of-date
    change within the authoritative repository.

One of the main features of Bitclone is that it is stateless, or more specifically, that it stores
the state in the destination repository (As a label in the commit message). This allows several
users (or a service) to use Bitclone for the same config/repositories and get the same result.

Currently, the only supported type of repository is Git. Bitclone is also able
to read from Mercurial repositories, but the feature is still experimental.
The extensible architecture allows adding bespoke origins and destinations
for almost any use case.
Official support for other repositories types will be added in the future.

## Example

```python
core.workflow(
    name = "default",
    origin = git.github_origin(
      url = "https://github.com/khulnasoft-lab/bitclone.git",
      ref = "master",
    ),
    destination = git.destination(
        url = "file:///tmp/foo",
    ),

    # Copy everything but don't remove a README_INTERNAL.txt file if it exists.
    destination_files = glob(["third_party/bitclone/**"], exclude = ["README_INTERNAL.txt"]),

    authoring = authoring.pass_thru("Default email <default@default.com>"),
    transformations = [
        core.replace(
                before = "//third_party/bazel/bashunit",
                after = "//another/path:bashunit",
                paths = glob(["**/BUILD"])),
        core.move("", "third_party/bitclone")
    ],
)
```

Run:

```shell
$ (mkdir /tmp/foo ; cd /tmp/foo ; git init --bare)
$ bitclone copy.bara.sky
```

## Getting Started using Bitclone

Bitclone doesn't have a release process yet, so you need to compile from HEAD.
In order to do that, you need to do the following:

  * [Install JDK 11](https://www.oracle.com/java/technologies/downloads/#java11).
  * [Install Bazel](https://bazel.build/install).
  * Clone the bitclone source locally:
      * `git clone https://github.com/khulnasoft-lab/bitclone.git`
  * Build:
      * `bazel build //java/com/khulnasoft-lab/bitclone`
      * `bazel build //java/com/khulnasoft-lab/bitclone:bitclone_deploy.jar` to create an executable uberjar.
  * Tests: `bazel test //...` if you want to ensure you are not using a broken version. Note that
    certain tests require the underlying tool to be installed(e.g. Mercurial, Quilt, etc.). It is
    fine to skip those tests if your Pull Request is unrelated to those modules (And our CI will
    run all the tests anyway).

### System packages

These packages can be installed using the appropriate package manager for your
system.

#### Arch Linux

  * [`aur/bitclone-git`][install/archlinux/aur-git]

[install/archlinux/aur-git]: https://aur.archlinux.org/packages/bitclone-git "Bitclone on the AUR"

### Using Intellij with Bazel plugin

If you use Intellij and the Bazel plugin, use this project configuration:

```
directories:
  bitclone/integration
  java/com/khulnasoft-lab/bitclone
  javatests/com/khulnasoft-lab/bitclone
  third_party

targets:
  //bitclone/integration/...
  //java/com/khulnasoft-lab/bitclone/...
  //javatests/com/khulnasoft-lab/bitclone/...
  //third_party/...
```

Note: configuration files can be stored in any place, even in a local folder.
We recommend using a VCS (like git) to store them; treat them as source code.

### Building Bitclone in an external Bazel workspace

There are convenience macros defined for all of Bitclone's dependencies. Add the
following code to your `WORKSPACE` file, replacing `{{ sha256sum }}` and
`{{ commit }}` as necessary.

```bzl
http_archive(
  name = "com_github_khulnasoft_bitclone",
  sha256 = "{{ sha256sum }}",
  strip_prefix = "bitclone-{{ commit }}",
  url = "https://github.com/khulnasoft-lab/bitclone/archive/{{ commit }}.zip",
)

load("@com_github_khulnasoft_bitclone//:repositories.bzl", "bitclone_repositories")

bitclone_repositories()

load("@com_github_khulnasoft_bitclone//:repositories.maven.bzl", "bitclone_maven_repositories")

bitclone_maven_repositories()

load("@com_github_khulnasoft_bitclone//:repositories.go.bzl", "bitclone_go_repositories")

bitclone_go_repositories()
```

You can then build and run the Bitclone tool from within your workspace:

```sh
bazel run @com_github_khulnasoft_bitclone//java/com/khulnasoft/bitclone -- <args...>
```

### Using Docker to build and run Bitclone

*NOTE: Docker use is currently experimental, and we encourage feedback or contributions.*

You can build bitclone using Docker like so

```sh
docker build --rm -t bitclone .
```

Once this has finished building, you can run the image like so from the root of
the code you are trying to use Bitclone on:

```sh
docker run -it -v "$(pwd)":/usr/src/app bitclone help
```

#### Environment variables

In addition to passing cmd args to the container, you can also set the following
environment variables as an alternative:
* `BITCLONE_SUBCOMMAND=migrate`
  * allows you to change the command run, defaults to `migrate`
* `BITCLONE_CONFIG=copy.bara.sky`
  * allows you to specify a path to a config file, defaults to root `copy.bara.sky`
* `BITCLONE_WORKFLOW=default`
  * allows you to specify the workflow to run, defaults to `default`
* `BITCLONE_SOURCEREF=''`
  * allows you to specify the sourceref, defaults to none
* `BITCLONE_OPTIONS=''`
  * allows you to specify options for bitclone, defaults to none

```sh
docker run \
    -e BITCLONE_SUBCOMMAND='validate' \
    -e BITCLONE_CONFIG='other.config.sky' \
    -v "$(pwd)":/usr/src/app \
    -it bitclone
```

#### Git Config and Credentials

There are a number of ways by which to share your git config and ssh credentials
with the Docker container, an example is below:

```sh
docker run \
    -v ~/.gitconfig:/root/.gitconfig:ro \
    -v ~/.ssh:/root/.ssh \
    -v ${SSH_AUTH_SOCK}:${SSH_AUTH_SOCK} -e SSH_AUTH_SOCK
    -v "$(pwd)":/usr/src/app \
    -it bitclone
```