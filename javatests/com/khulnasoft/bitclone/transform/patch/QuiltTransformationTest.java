/*
 * Copyright (C) 2021 KhulnaSoft Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.khulnasoft.bitclone.transform.patch;

import static com.google.common.truth.Truth.assertThat;
import static com.khulnasoft.bitclone.testing.FileSubjects.assertThatPath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.config.ConfigFile;
import com.khulnasoft.bitclone.config.MapConfigFile;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.SkylarkTestExecutor;
import com.khulnasoft.bitclone.testing.TransformWorks;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import com.khulnasoft.bitclone.shell.Command;
import com.khulnasoft.bitclone.shell.CommandException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.starlark.java.syntax.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class QuiltTransformationTest {

  private static final String OLDDIFF =
      ""
          + "--- a/file1.txt\n"
          + "+++ b/file1.txt\n"
          + "@@ -1,3 +1,3 @@\n"
          + " line1\n"
          + "-foo\n"
          + "+bar\n"
          + " line3\n"
          + "\\ No newline at end of file\n"
          + "--- a/file2.txt\n"
          + "+++ b/file2.txt\n"
          + "@@ -1 +1 @@\n"
          + "-bar\n"
          + "+new bar\n";

  private static final String SERIES =
      ""
          + "# Comment line\n"
          + "diff.patch";

  private OptionsBuilder options;
  private PatchingOptions patchingOptions;
  private Path checkoutDir;
  private TestingConsole console;
  private SkylarkTestExecutor skylark;
  private ConfigFile patchFile;
  private ConfigFile seriesFile;

  @Before
  public void setUp() throws IOException {
    checkoutDir =  Files.createTempDirectory("workdir");
    Files.createDirectories(checkoutDir);
    console = new TestingConsole();
    // QuiltTransformation needs to write and read from to real temp directory.
    options = new OptionsBuilder().setWorkdirToRealTempDir();
    // GeneralOptions.getDirFactory() requires $HOME to be set. We set --output-root instead.
    options.setOutputRootToTmpDir();
    patchingOptions = options.build().get(PatchingOptions.class);
    patchingOptions.quiltBin = setUpQuiltBin(patchingOptions.getGeneralOptions());
    skylark = new SkylarkTestExecutor(options);
    ImmutableMap<String, byte[]> configFiles =
        ImmutableMap.of(
            "patches/diff.patch", OLDDIFF.getBytes(UTF_8),
            "patches/series", SERIES.getBytes(UTF_8));
    patchFile = new MapConfigFile(configFiles , "patches/diff.patch");
    seriesFile = new MapConfigFile(configFiles, "patches/series");
  }

  // Returns "quilt" if the "quilt" command can execute successfully. Otherwise, returns the path
  // to the quilt binary from build-time data dependency.
  private static String setUpQuiltBin(GeneralOptions options) {
    // Try executing "quilt --version" to see if "quilt" command can run.
    Command cmd =
          new Command(new String[] {"quilt", "--version"}, options.getEnvironment(), null);
    boolean success = false;
    try {
      options.newCommandRunner(cmd).execute();
      success = true;
    } catch (CommandException e) {
      // "success" remains false.
    }
    if (success) {
      return "quilt";
    }
    Path runtime = Paths.get(System.getenv("TEST_SRCDIR"))
        .resolve(System.getenv("TEST_WORKSPACE"))
        .resolve("third_party/quilt");
    return runtime.resolve("quilt").toAbsolutePath().toString();
  }

  @Test
  public void transformationZeroChangeTest() throws Exception {
    Files.write(checkoutDir.resolve("file1.txt"), "line1\nfoo\nline3".getBytes(UTF_8));
    Files.write(checkoutDir.resolve("file2.txt"), "bar\n".getBytes(UTF_8));
    QuiltTransformation transform =
        new QuiltTransformation(seriesFile, ImmutableList.of(patchFile), patchingOptions,
            /*reverse=*/ false, Location.BUILTIN);
    transform.transform(TransformWorks.of(checkoutDir, "testmsg", console));
    assertThatPath(checkoutDir)
        .containsFile("file1.txt", "line1\nbar\nline3")
        .containsFile("file2.txt", "new bar\n")
        .containsFile("patches/diff.patch", OLDDIFF)
        .containsFile("patches/series", SERIES)
        .containsNoMoreFiles();
  }

  @Test
  public void transformationUpdatePatchTest() throws Exception {
    String expectedNewDiff =
        ""
            + "--- a/file1.txt\n"
            + "+++ b/file1.txt\n"
            + "@@ -1,5 +1,5 @@\n"
            + " new line\n"
            + " \n"
            + " line1\n"
            + "-foo\n"
            + "+bar\n"
            + " line3\n"
            + "\\ No newline at end of file\n"
            + "--- a/file2.txt\n"
            + "+++ b/file2.txt\n"
            + "@@ -1 +1 @@\n"
            + "-bar\n"
            + "+new bar\n";
    Files.write(checkoutDir.resolve("file1.txt"), "new line\n\nline1\nfoo\nline3".getBytes(UTF_8));
    Files.write(checkoutDir.resolve("file2.txt"), "bar\n".getBytes(UTF_8));
    QuiltTransformation transform =
        new QuiltTransformation(seriesFile, ImmutableList.of(patchFile), patchingOptions,
            /*reverse=*/ false, Location.BUILTIN);
    transform.transform(TransformWorks.of(checkoutDir, "testmsg", console));
    assertThatPath(checkoutDir)
        .containsFile("file1.txt", "new line\n\nline1\nbar\nline3")
        .containsFile("file2.txt", "new bar\n")
        .containsFile("patches/diff.patch", expectedNewDiff)
        .containsFile("patches/series", SERIES)
        .containsNoMoreFiles();
  }

  @Test
  public void parseSkylarkTest() throws Exception {
    Files.write(checkoutDir.resolve("file1.txt"), "line1\nfoo\nline3".getBytes(UTF_8));
    Files.write(checkoutDir.resolve("file2.txt"), "bar\n".getBytes(UTF_8));
    skylark.addConfigFile("patches/diff.patch", patchFile.readContent());
    skylark.addConfigFile("patches/series", seriesFile.readContent());
    QuiltTransformation transformation =
        skylark.eval("r",
            "r = patch.quilt_apply(\n"
                + "  series = 'patches/series',\n"
                + ")\n");
    transformation.transform(TransformWorks.of(checkoutDir, "testmsg", console));
    assertThatPath(checkoutDir)
        .containsFile("file1.txt", "line1\nbar\nline3")
        .containsFile("file2.txt", "new bar\n")
        .containsFile("patches/diff.patch", OLDDIFF)
        .containsFile("patches/series", SERIES)
        .containsNoMoreFiles();
  }

    @Test
  public void parseSkylarkTest_emptySeries() throws Exception {
    Files.write(checkoutDir.resolve("file1.txt"), "line1\nfoo\nline3".getBytes(UTF_8));
    Files.write(checkoutDir.resolve("file2.txt"), "bar\n".getBytes(UTF_8));
    skylark.addConfigFile("patches/series", "");
    assertThrows(ValidationException.class, 
        () -> skylark.eval("r",
            "r = patch.quilt_apply(\n"
                + "  series = 'patches/series',\n"
                + ")\n"));
  }


  @Test
  public void describeTest() {
    QuiltTransformation transform = new QuiltTransformation(seriesFile, ImmutableList.of(patchFile),
        patchingOptions, /*reverse=*/ false, Location.BUILTIN);
    assertThat(transform.describe()).isEqualTo(
        "Patch.quilt_apply: using quilt to apply and update patches: patches/diff.patch");
  }
}
