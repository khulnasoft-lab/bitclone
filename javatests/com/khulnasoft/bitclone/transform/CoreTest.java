/*
 * Copyright (C) 2019 KhulnaSoft Ltd.
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

package com.khulnasoft.bitclone.transform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.khulnasoft.bitclone.action.StarlarkAction;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.SkylarkTestExecutor;
import com.khulnasoft.bitclone.util.console.Message.MessageType;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import net.starlark.java.eval.StarlarkCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CoreTest {

  private OptionsBuilder options;
  private SkylarkTestExecutor skylark;
  private TestingConsole console;

  @Before
  public void setup() {
    console = new TestingConsole();
    options = new OptionsBuilder();
    options.setConsole(console);
    skylark = new SkylarkTestExecutor(options);
  }

  @Test
  public void testGetConfig() throws Exception {
    assertThat(
            skylark.<String>evalWithConfigFilePath(
                "p", "p = core.main_config_path", "some/random/path/bit.clone.sky"))
        .isEqualTo("some/random/path/bit.clone.sky");
  }

  @Test
  public void testFormat() throws Exception {
    assertThat(skylark.<String>eval("f", "f = core.format('%-10s %d', ['foo', 1234])"))
        .isEqualTo("foo        1234");
  }

  @Test
  public void testLambdaName_top_level() throws Exception {
    assertThat(skylark.<StarlarkAction>eval("some", ""
        + "some = core.action(impl = lambda x: x*2)").getName())
        .isEqualTo("lambda");
  }

  @Test
  public void testLambdaName_caller_name() throws Exception {
    assertThat(skylark.<StarlarkAction>eval("some", ""
        + "def create_f(n):\n"
        + "    return core.action(impl = lambda x: x*n)\n"
        + "some = create_f(3)").getName())
        .isEqualTo("create_f");
  }

  @Test
  public void testLambdaName_no_rename_raw_lambda() throws Exception {
    assertThat(skylark.<StarlarkCallable>eval("some", ""
        + "def create_f(n):\n"
        + "    return lambda x: x*n\n"
        + "some = create_f(3)").getName())
        .isEqualTo("lambda");
  }

  @Test
  public void testInvalidFormat() {
    ValidationException expected =
        assertThrows(
            ValidationException.class,
            () -> skylark.eval("f", "f = core.format('%-10s %d', ['foo', '1234'])"));
    assertThat(expected)
        .hasMessageThat()
        .contains("Invalid format: %-10s %d: d != java.lang.String");
  }

  @Test
  public void testInvalidReverse_hasLocation() {
    ValidationException expected =
        assertThrows(ValidationException.class, () -> skylark.eval("f", 
            "f = core.reverse([core.replace("
                + "before = '${x}', after = '#', regex_groups = {'x': '.*'},),])"));
    assertThat(expected)
        .hasMessageThat()
        .contains("at bit.clone.sky:1:31");
  }

  @Test
  public void testConsole() throws Exception {
    skylark.<String>eval("f", "f = core.console.info('Hello World')");
    console.assertThat().logContains(MessageType.INFO, "Hello World");
  }
}
