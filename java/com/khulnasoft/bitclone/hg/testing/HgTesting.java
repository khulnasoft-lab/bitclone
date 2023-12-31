/*
 * Copyright (C) 2018 KhulnaSoft Ltd..
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

package com.khulnasoft.bitclone.hg.testing;

import com.khulnasoft.bitclone.testing.OptionsBuilder;

/**
 * Testing utilities for Hg.
 */
public class HgTesting {

  private HgTesting() {}

  /**
   * Makes the Hg binary available with the given options.
   */
  public static void enableBinary(OptionsBuilder options) {
    // TODO(jlliu): Implement in order to make OSS tests hermetic
  }
}
