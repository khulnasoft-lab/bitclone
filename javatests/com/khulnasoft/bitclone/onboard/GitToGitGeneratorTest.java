/*
 * Copyright (C) 2022 KhulnaSoft Ltd.
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

import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.onboard.core.AskInputProvider.Mode;
import com.khulnasoft.bitclone.onboard.core.CannotProvideException;
import com.khulnasoft.bitclone.onboard.core.InputProviderResolverImpl;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GitToGitGeneratorTest {

  @Test
  public void testSimple() throws CannotProvideException, InterruptedException {
    GitToGitGenerator gitToGitGenerator = new GitToGitGenerator();
    TestingConsole console = new TestingConsole();

    console.respondWithString("http://example.com/origin");
    console.respondWithString("http://example.com/destination");
    console.respondWithString("author <author@example.com>");
    console.respondWithString("my_name");

    String config =
        gitToGitGenerator.generate(
            InputProviderResolverImpl.create(
                ImmutableSet.of(),
                (s, r) -> {
                  throw new IllegalStateException();
                },
                Mode.AUTO,
                console));
    assertThat(config).isEqualTo(""
        + "core.workflow(\n"
        + "    name = 'my_name',\n"
        + "    origin = git.origin(\n"
        + "        url = \"http://example.com/origin\",\n"
        + "    ), \n"
        + "    destination = git.destination(\n"
        + "        url = \"http://example.com/destination\",\n"
        + "    ),\n"
        + "    authoring = authoring.pass_thru(\"author <author@example.com>\"),\n"
        + "\n"
        + "    transformations = [\n"
        + "        # TODO: Insert your transformations here\n"
        + "    ],\n"
        + ")\n");
  }
}
