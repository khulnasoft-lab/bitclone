/*
 * Copyright (C) 2018 KhulnaSoft Ltd.
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

package com.khulnasoft.bitclone.util;

import static com.google.common.truth.Truth.assertThat;
import static com.khulnasoft.bitclone.testing.FileSubjects.assertThatPath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.util.OriginUtil.CheckoutHook;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OriginUtilTest {

  private Path workDir;
  private OptionsBuilder options;

  @Before
  public void setup() throws Exception {
    workDir = Files.createTempDirectory("workDir");
    options = new OptionsBuilder();
  }

  @Test
  public void testCheckoutHookWithExitError() throws Exception {
    Path script = Files.createTempFile("script", "script");
    Files.write(script, "exit 42".getBytes(UTF_8));

    Files.setPosixFilePermissions(script, ImmutableSet.<PosixFilePermission>builder()
        .addAll(Files.getPosixFilePermissions(script))
        .add(PosixFilePermission.OWNER_EXECUTE).build());

    CheckoutHook checkoutHook =
        new CheckoutHook(script.toString(), options.general, "some.origin");
    RepoException expected = assertThrows(RepoException.class, () -> checkoutHook.run(workDir));
    assertThat(expected).hasMessageThat().contains("Error executing the checkout hook");
    assertThat(Throwables.getRootCause(expected))
        .hasMessageThat()
        .isEqualTo("Process exited with status 42");
  }

  @Test
  public void testCheckoutHook() throws Exception {
    Path script = Files.createTempFile("script", "script");
    Files.write(script, "touch hook.txt".getBytes(UTF_8));

    Files.setPosixFilePermissions(script, ImmutableSet.<PosixFilePermission>builder()
        .addAll(Files.getPosixFilePermissions(script))
        .add(PosixFilePermission.OWNER_EXECUTE).build());

    CheckoutHook checkoutHook =
        new CheckoutHook(script.toString(), options.general, "some.origin");
    checkoutHook.run(workDir);

    assertThatPath(workDir).containsFile("hook.txt", "");
  }

}
