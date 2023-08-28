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

package com.khulnasoft.bitclone.onboard.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InputTest {

  @SuppressWarnings("unused")
  @Test
  public void testNoTwoWithSameName() {
    Input<String> unused = Input.create("testNoTwoWithSameName", "doesn't matter",
        null, String.class, (s, resolver) -> s);

    assertThat(assertThrows(
        IllegalStateException.class, () ->
            Input.create("testNoTwoWithSameName", "still doesn't matter",
                null, String.class, (s, resolver) -> s)
    )).hasMessageThat().contains("Two calls for the same Input name 'testNoTwoWithSameName'");
  }
}
