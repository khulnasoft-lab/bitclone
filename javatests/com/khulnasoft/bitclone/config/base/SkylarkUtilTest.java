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

package com.khulnasoft.bitclone.config.base;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.khulnasoft.bitclone.config.SkylarkUtil;
import net.starlark.java.eval.EvalException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SkylarkUtilTest {
  @Test
  public void testCheckNotEmpty_null() throws Exception {
    EvalException thrown =
        assertThrows(EvalException.class, () -> SkylarkUtil.checkNotEmpty(null, "foo"));
    assertThat(thrown).hasMessageThat().contains("Invalid empty field 'foo'");
  }

  @Test
  public void testCheckNotEmpty_empty() throws Exception {
    EvalException thrown =
        assertThrows(EvalException.class, () -> SkylarkUtil.checkNotEmpty("", "foo"));
    assertThat(thrown).hasMessageThat().contains("Invalid empty field 'foo'");
  }

  @Test
  public void testCheckNotEmpty_nonEmpty() throws Exception {
    assertThat(SkylarkUtil.checkNotEmpty("test", "foo")).isEqualTo("test");
  }
}
