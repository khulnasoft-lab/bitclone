/*
 * Copyright (C) 2023 KhulnaSoft Ltd.
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

package com.khulnasoft.bitclone.onboard;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.khulnasoft.bitclone.testing.git.GitTestUtil.getGitEnv;
import static com.khulnasoft.bitclone.util.CommandRunner.DEFAULT_TIMEOUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.git.GitEnvironment;
import com.khulnasoft.bitclone.git.GitOptions;
import com.khulnasoft.bitclone.git.GitRepository;
import com.khulnasoft.bitclone.git.GitRevision;
import com.khulnasoft.bitclone.git.Refspec;
import com.khulnasoft.bitclone.onboard.core.CannotProvideException;
import com.khulnasoft.bitclone.onboard.core.Input;
import com.khulnasoft.bitclone.onboard.core.InputProviderResolver;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.util.Glob;
import com.khulnasoft.bitclone.util.console.Message;
import com.khulnasoft.bitclone.util.console.Message.MessageType;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConfigHeuristicsInputProviderTest {
  protected String url;
  protected Path workDir;
  protected Path repoGitDir;
  protected Path destination;
  protected GitRepository origin;
  protected GitOptions gitOptions;
  protected TestingConsole console;
  protected OptionsBuilder optionsBuilder;
  protected GeneralOptions generalOptions;

  @Before
  public void setup() throws Exception {
    console = new TestingConsole();
    repoGitDir = Files.createTempDirectory("GitDestinationTest-repoGitDir");
    workDir = Files.createTempDirectory("workdir");
    destination = Files.createTempDirectory("destination");
    
    optionsBuilder = getOptionsBuilder(console);
    String unused = git("init", "--bare", repoGitDir.toString());
    generalOptions = optionsBuilder.general;
    gitOptions = optionsBuilder.git;
    origin =
        GitRepository.newBareRepo(
            repoGitDir,
            new GitEnvironment(optionsBuilder.general.getEnvironment()),
            /*verbose*/ true,
            DEFAULT_TIMEOUT,
            false);
    url = "file:///" + origin.getGitDir();
    origin = repo().withWorkTree(workDir);
  }

  @Test
  public void doubleWildcardOriginGlobTest() throws Exception {
    InputProviderResolver resolver =
        new InputProviderResolver() {
          @Override
          public <T> T resolve(Input<T> input) throws CannotProvideException {
            try {
              if (input == Inputs.GIT_ORIGIN_URL) {
                return Inputs.GIT_ORIGIN_URL.asValue(new URL(url));
              }
              if (input == Inputs.CURRENT_VERSION) {
                return Inputs.CURRENT_VERSION.asValue("1.0.0");
              }
              if (input == Inputs.GENERATOR_FOLDER) {
                return Inputs.GENERATOR_FOLDER.asValue(destination);
              }
              if (input == Inputs.ORIGIN_GLOB) {
                return Inputs.ORIGIN_GLOB.asValue(Glob.ALL_FILES);
              }
            } catch (MalformedURLException e) {
              Assert.fail("Malformed url, shouldn't happen: " + e);
            }
            throw new CannotProvideException("Cannot provide " + input);
          }
        };

    GitOptions mockGitOptions = mock(GitOptions.class);
    GitRepository mockGitRepository = mock(GitRepository.class);
    ConfigHeuristicsInputProvider inputProvider =
        new ConfigHeuristicsInputProvider(
            mockGitOptions, generalOptions, ImmutableSet.of(), 30, console);
    GitRevision gitRevision =
        new GitRevision(
            mockGitRepository, "a".repeat(40), null, null, ImmutableListMultimap.of(), url);

    when(mockGitOptions.cachedBareRepoForUrl(anyString())).thenReturn(mockGitRepository);
    when(mockGitRepository.withWorkTree(any(Path.class))).thenReturn(mockGitRepository);
    when(mockGitRepository.createRefSpec(anyString()))
        .thenReturn(Refspec.create(getEnv(), Files.createTempDirectory("origin"), "refs/tags/*"));
    when(mockGitRepository.fetchSingleRef(
            anyString(), anyString(), anyBoolean(), eq(Optional.empty())))
        .thenReturn(gitRevision);

    Optional<Glob> glob = inputProvider.resolve(Inputs.ORIGIN_GLOB, resolver);

    // If the destination is a directory and no exception is thrown, we know that the heuristics was
    // computed
    assertThat(Files.isDirectory(destination)).isTrue();
    assertThat(glob).isEmpty();
  }

  @Test
  public void gitFuzzyLastRevTest() throws Exception {
    Files.writeString(workDir.resolve("foo.txt"), "hi");
    Files.writeString(workDir.resolve("bar.txt"), "bye");
    origin.add().files("foo.txt", "bar.txt").run();
    origin.simpleCommand("commit", "foo.txt", "-m", "message");
    origin.simpleCommand("commit", "bar.txt", "-m", "message");

    Files.writeString(destination.resolve("foo.txt"), "hi");

    origin.tag("v1.0.0").run();

    InputProviderResolver resolver =
        new InputProviderResolver() {
          @Override
          public <T> T resolve(Input<T> input) throws CannotProvideException {
            try {
              if (input == Inputs.GIT_ORIGIN_URL) {
                return Inputs.GIT_ORIGIN_URL.asValue(new URL(url));
              }
              if (input == Inputs.CURRENT_VERSION) {
                return Inputs.CURRENT_VERSION.asValue("1.0.0");
              }
              if (input == Inputs.GENERATOR_FOLDER) {
                return Inputs.GENERATOR_FOLDER.asValue(destination);
              }
              if (input == Inputs.ORIGIN_GLOB) {
                return Inputs.ORIGIN_GLOB.asValue(Glob.ALL_FILES);
              }
            } catch (MalformedURLException e) {
              Assert.fail("Malformed url, shouldn't happen: " + e);
            }
            throw new CannotProvideException("Cannot provide " + input);
          }
        };

    ConfigHeuristicsInputProvider inputProvider =
        new ConfigHeuristicsInputProvider(
            gitOptions, generalOptions, ImmutableSet.of(), 30, console);
    Glob expectedGlob = Glob.createGlob(ImmutableList.of("**"), ImmutableList.of("bar.txt"));
    Optional<Glob> glob = inputProvider.resolve(Inputs.ORIGIN_GLOB, resolver);

    // The glob was computed and the version was matched with the git tag
    assertThat(Files.isDirectory(workDir)).isTrue();
    assertThat(glob).hasValue(expectedGlob);
    assertThat(console.getMessages())
        .contains(
            new Message(MessageType.INFO, "Assuming version 1.0.0 references v1.0.0 (1.0.0)"));
  }

  public OptionsBuilder getOptionsBuilder(TestingConsole console) throws IOException {
    return new OptionsBuilder().setConsole(this.console).setOutputRootToTmpDir();
  }

  private String git(String... argv) throws RepoException {
    return repo().git(repoGitDir, argv).getStdout();
  }

  private GitRepository repo() {
    return repoForPath(repoGitDir);
  }

  private GitRepository repoForPath(Path path) {
    return GitRepository.newBareRepo(
        path, getEnv(), /* verbose= */ true, DEFAULT_TIMEOUT, /* noVerify= */ false);
  }

  public GitEnvironment getEnv() {
    Map<String, String> joinedEnv = Maps.newHashMap(optionsBuilder.general.getEnvironment());
    joinedEnv.putAll(getGitEnv().getEnvironment());
    return new GitEnvironment(joinedEnv);
  }
}
