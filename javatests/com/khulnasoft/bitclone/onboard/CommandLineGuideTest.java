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

package com.khulnasoft.bitclone.onboard;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.CommandEnv;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.SkylarkTestExecutor;
import com.khulnasoft.bitclone.testing.TestingEventMonitor;
import com.khulnasoft.bitclone.util.console.Message;
import com.khulnasoft.bitclone.util.console.Message.MessageType;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CommandLineGuideTest {

  private final OptionsBuilder optionsBuilder = new OptionsBuilder();
  private final TestingEventMonitor eventMonitor = new TestingEventMonitor();
  private Path temp;
  private SkylarkTestExecutor skylark;

  @Before
  public void setUp() throws Exception {
    optionsBuilder.general.enableEventMonitor("just testing", eventMonitor);
    temp = Files.createTempDirectory("temp");
    skylark = new SkylarkTestExecutor(optionsBuilder);
  }

  @Test
  public void testGitToGitTemplate() {
    TestingConsole console = new TestingConsole();
    String urlPrefix = "https://github.com/khulnasoft-lab/";
    console
        .respondWithString(urlPrefix.concat("origin"))
        .respondWithString(urlPrefix.concat("destination"))
        .respondWithString("Bitclone <bit.clone.com>");
    optionsBuilder.setConsole(console);

    CommandLineGuide.runForCommandLine(
        new CommandEnv(
            temp, skylark.createModuleSet().getOptions(), ImmutableList.of("bit.clone.sky")));

    ConfigBuilder expectedConfig = new ConfigBuilder(new GitToGitTemplate());
    expectedConfig.setNamedStringParameter("origin_url", urlPrefix.concat("origin"));
    expectedConfig.setNamedStringParameter("destination_url", urlPrefix.concat("destination"));
    expectedConfig.setNamedStringParameter("email", "Bitclone <bit.clone.com>");
    assertThat(
            Joiner.on('\n')
                .join(
                    console.getMessages().stream()
                        .map(Message::getText)
                        .collect(Collectors.toList())))
        .contains(expectedConfig.build());
  }

  @Test
  public void testGitToGitTemplatePredicateNotSatisfied() {
    String urlPrefix = "https://github.com/khulnasoft-lab/";

    TestingConsole console = new TestingConsole();
    console
        .respondWithString(urlPrefix.concat("origin"))
        .respondWithString(urlPrefix.concat("destination"))
        .respondWithString("not a valid email")
        .respondWithString("Foo <good.email@example.com>");
    optionsBuilder.setConsole(console);

    CommandLineGuide.runForCommandLine(
        new CommandEnv(
            temp, skylark.createModuleSet().getOptions(), ImmutableList.of("bit.clone.sky")));
    console.assertThat().timesInLog(2, MessageType.INFO,
        "What should be the value for field email");
    console.assertThat().onceInLog(MessageType.WARNING,
        "Invalid response: not a valid email");
  }
}
