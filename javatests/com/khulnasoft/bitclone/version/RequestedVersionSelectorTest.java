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

package com.khulnasoft.bitclone.version;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RequestedVersionSelectorTest {

  @Test
  public void testVersionSelector() throws Exception {
    RequestedVersionSelector selector = new RequestedVersionSelector();
    assertThat(selector.select(() -> ImmutableSet.of("hello"), "REQUEST",
        new TestingConsole()))
        .isEqualTo(Optional.of("REQUEST"));
  }
}
