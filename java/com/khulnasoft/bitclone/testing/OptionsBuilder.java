/*
 * Copyright (C) 2016 KhulnaSoft Ltd..
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

package com.khulnasoft.bitclone.testing;

import com.google.api.client.http.HttpTransport;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Jimfs;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.Option;
import com.khulnasoft.bitclone.Options;
import com.khulnasoft.bitclone.WorkflowOptions;
import com.khulnasoft.bitclone.buildozer.BuildozerOptions;
import com.khulnasoft.bitclone.folder.FolderDestinationOptions;
import com.khulnasoft.bitclone.folder.FolderOriginOptions;
import com.khulnasoft.bitclone.format.BuildifierOptions;
import com.khulnasoft.bitclone.git.GerritOptions;
import com.khulnasoft.bitclone.git.GitDestinationOptions;
import com.khulnasoft.bitclone.git.GitHubDestinationOptions;
import com.khulnasoft.bitclone.git.GitHubOptions;
import com.khulnasoft.bitclone.git.GitHubPrOriginOptions;
import com.khulnasoft.bitclone.git.GitMirrorOptions;
import com.khulnasoft.bitclone.git.GitOptions;
import com.khulnasoft.bitclone.git.GitOriginOptions;
import com.khulnasoft.bitclone.hg.HgOptions;
import com.khulnasoft.bitclone.hg.HgOriginOptions;
import com.khulnasoft.bitclone.http.HttpOptions;
import com.khulnasoft.bitclone.onboard.GeneratorOptions;
import com.khulnasoft.bitclone.regenerate.RegenerateOptions;
import com.khulnasoft.bitclone.remotefile.RemoteFileOptions;
import com.khulnasoft.bitclone.testing.TestingModule.TestingOptions;
import com.khulnasoft.bitclone.transform.debug.DebugOptions;
import com.khulnasoft.bitclone.transform.patch.PatchingOptions;
import com.khulnasoft.bitclone.util.console.Console;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;

/**
 * Allows building complete and sane {@link Options} instances succinctly.
 */
public class OptionsBuilder {

  public final GeneralOptions general =
      new GeneralOptions(
          System.getenv(),
          Jimfs.newFileSystem(),
          /*verbose=*/ true,
          new TestingConsole(),
          /* configRoot= */ null,
          /*outputRoot*/ null,
          /*reuseOutputDirs*/ true,
          /* disableReversibleCheck= */ false,
          /*force=*/ false, /*outputLimit*/ 0);

  public FolderDestinationOptions folderDestination = new FolderDestinationOptions();
  public FolderOriginOptions folderOrigin = new FolderOriginOptions();

  public GitOptions git = new GitOptions(general);
  public GitOriginOptions gitOrigin = new GitOriginOptions();
  public GitHubPrOriginOptions githubPrOrigin = new GitHubPrOriginOptions();
  public GitDestinationOptions gitDestination = new GitDestinationOptions(general, git);
  public PatchingOptions patch = new PatchingOptions(general);
  public DebugOptions debug = new DebugOptions(general);
  public RemoteFileOptions remoteFile = new RemoteFileOptions();
  public BuildifierOptions buildifier = new BuildifierOptions();
  public GeneratorOptions generator = new GeneratorOptions();

  public String buildozerBin = null;

  public GitHubOptions github = new GitHubOptions(general, git) {
    @Override
    protected HttpTransport newHttpTransport() {
      return Mockito.mock(HttpTransport.class);
    }
  };
  public GitHubDestinationOptions githubDestination = new GitHubDestinationOptions();
  public GitMirrorOptions gitMirrorOptions = new GitMirrorOptions();
  public GerritOptions gerrit = new GerritOptions(general, git);
  public WorkflowOptions workflowOptions =
      new WorkflowOptions(/*changeBaseline=*/null, /*lastRevision=*/ null,
          /*checkLastRevState=*/false);

  public HttpOptions http = new HttpOptions();

  public HgOptions hg = new HgOptions(general);
  public HgOriginOptions hgOrigin = new HgOriginOptions();

  public TestingOptions testingOptions = new TestingOptions();

  public RegenerateOptions regenerateOptions = new RegenerateOptions();

  public final OptionsBuilder setWorkdirToRealTempDir() {
    return setWorkdirToRealTempDir(StandardSystemProperty.USER_DIR.value());
  }

  public OptionsBuilder setWorkdirToRealTempDir(String cwd) {
    general.setFileSystemForTest(FileSystems.getDefault());
    general.setEnvironmentForTest(updateEnvironment(general.getEnvironment(), "PWD", cwd));
    return this;
  }

  public OptionsBuilder setEnvironment(Map<String, String> environment) {
    general.setEnvironmentForTest(environment);
    return this;
  }

  public OptionsBuilder setOutputRootToTmpDir() {
    // Using Files.createTempDirectory() generates paths > 255 in some tests and that causes
    // 'File name too long' exceptions in Linux
    general.setOutputRootPathForTest(
        FileSystems.getDefault().getPath(StandardSystemProperty.JAVA_IO_TMPDIR.value()));
    return this;
  }

  public final OptionsBuilder setConsole(Console newConsole) {
    general.setConsoleForTest(newConsole);
    return this;
  }

  public final OptionsBuilder setHomeDir(String homeDir) {
    general.setEnvironmentForTest(updateEnvironment(general.getEnvironment(), "HOME", homeDir));
    return this;
  }

  public final OptionsBuilder setForce(boolean force) {
    general.setForceForTest(force);
    return this;
  }

  public final OptionsBuilder setLabels(ImmutableMap<String, String> labels) {
    general.setCliLabelsForTest(labels);
    return this;
  }

  public final OptionsBuilder setLastRevision(String lastRevision) {
    workflowOptions = new WorkflowOptions(workflowOptions.getChangeBaseline(), lastRevision,
        workflowOptions.checkLastRevState);
    return this;
  }

  /**
   * Returns all options to include in the built {@link Options} instance. This can be overridden by
   * child classes, in which case it should also include the superclass' instances.
   */
  protected Iterable<Option> allOptions() {
    BuildozerOptions buildozer = new BuildozerOptions(general, buildifier, workflowOptions);

    if (buildozerBin != null) {
      buildozer.buildozerBin = buildozerBin;
    }
    return ImmutableList.of(
        general,
        folderDestination,
        folderOrigin,
        git,
        gitOrigin,
        githubPrOrigin,
        gitDestination,
        gitMirrorOptions,
        gerrit,
        github,
        githubDestination,
        hg,
        hgOrigin,
        workflowOptions,
        testingOptions,
        patch,
        debug,
        remoteFile,
        buildifier,
        buildozer,
        generator,
        http,
        regenerateOptions);
  }

  public final Options build() {
    return new Options(ImmutableList.copyOf(allOptions()));
  }

  private static ImmutableMap<String, String> updateEnvironment(
      Map<String, String> environment, String key, String value) {
    HashMap<String, String> updatedEnvironment = new HashMap<>(environment);
    updatedEnvironment.put(key, value);
    return ImmutableMap.copyOf(updatedEnvironment);
  }

}
