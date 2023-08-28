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

package com.khulnasoft.bitclone.testing;

import com.khulnasoft.bitclone.Endpoint;
import com.khulnasoft.bitclone.Trigger;
import net.starlark.java.annot.StarlarkBuiltin;

/**
 * A dummy trigger for feedback mechanism.
 *
 * <p>Extends {@link DummyEndpoint} just for convenience for the tests.
 */
@StarlarkBuiltin(name = "dummy_trigger", doc = "A dummy trigger for feedback mechanism")
public class DummyTrigger extends DummyEndpoint implements Trigger {

  @Override
  public Endpoint getEndpoint() {
    return this;
  }
}
