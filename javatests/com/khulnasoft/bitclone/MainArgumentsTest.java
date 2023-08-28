/*
 * Copyright (C) 2016 KhulnaSoft Ltd.
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

package com.khulnasoft.bitclone;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Jimfs;
import com.khulnasoft.bitclone.MainArguments.CommandWithArgs;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.util.ExitCode;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MainArgumentsTest {

  private MainArguments mainArguments;
  private FileSystem fs;
  private OptionsBuilder options;

  @Before
  public void setup() {
    mainArguments = new MainArguments(ImmutableList.of());
    fs = Jimfs.newFileSystem();
    options = new OptionsBuilder();
  }

  @Test
  public void getWorkdirNormalized() throws Exception {
    mainArguments.baseWorkdir = "/some/../path/..";
    Path baseWorkdir = mainArguments.getBaseWorkdir(options.general, fs);
    assertThat(baseWorkdir.toString()).isEqualTo("/");
  }

  @Test
  public void getWorkdirIsNotDirectory() throws Exception {
    Files.write(fs.getPath("file"), "hello".getBytes(UTF_8));

    mainArguments.baseWorkdir = "file";
    IOException thrown =
        assertThrows(IOException.class, () -> mainArguments.getBaseWorkdir(options.general, fs));
    assertThat(thrown).hasMessageThat().contains("'file' exists and is not a directory");
  }

  @Test
  public void colonSyntaxParses() throws Exception {
    BitcloneCmd cmd = new BitcloneCmd() {

      @Override
      public ExitCode run(CommandEnv commandEnv) {
        return null;
      }

      @Override
      public String name() {
        return "cmd";
      }
    };
    mainArguments.unnamed = new ArrayList<>(ImmutableList.of("path/bit.clone.sky:workflow", "ref"));

    CommandWithArgs res = mainArguments.parseCommand(ImmutableMap.of("cmd", cmd), cmd);
    assertThat(res.getArgs()).containsExactly("path/bit.clone.sky", "workflow", "ref");
  }

  @Test
  public void colonSyntaxParsesWithCmd() throws Exception {
    BitcloneCmd cmd = new BitcloneCmd() {

      @Override
      public ExitCode run(CommandEnv commandEnv) {
        return null;
      }

      @Override
      public String name() {
        return "cmd";
      }
    };
    mainArguments.unnamed =
        new ArrayList<>(ImmutableList.of("cmd", "path/bit.clone.sky:workflow", "ref"));

    CommandWithArgs res = mainArguments.parseCommand(ImmutableMap.of("cmd", cmd), cmd);
    assertThat(res.getArgs()).containsExactly("path/bit.clone.sky", "workflow", "ref");
    assertThat(res.getSubcommand()).isSameInstanceAs(cmd);
  }

  @Test
  public void noFile() throws Exception {
    BitcloneCmd cmd = new BitcloneCmd() {

      @Override
      public ExitCode run(CommandEnv commandEnv) {
        return null;
      }

      @Override
      public String name() {
        return "cmd";
      }
    };
    mainArguments.unnamed =
        new ArrayList<>(ImmutableList.of("cmd"));

    CommandWithArgs res = mainArguments.parseCommand(ImmutableMap.of("cmd", cmd), cmd);
    assertThat(res.getArgs()).isEmpty();
    assertThat(res.getSubcommand()).isSameInstanceAs(cmd);
  }
}
