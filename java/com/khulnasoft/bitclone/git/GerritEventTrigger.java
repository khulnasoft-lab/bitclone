/*
 * Copyright (C) 2022 KhulnaSoft Ltd..
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

package com.khulnasoft.bitclone.git;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.git.gerritapi.GerritEventType;

/**
 * A simple pair to express Gerrit Events with arbitrary subtypes (Labels)
 */
@AutoValue
public abstract class GerritEventTrigger {
  public abstract GerritEventType type();

  public abstract ImmutableSet<String> subtypes();


  public static GerritEventTrigger create(GerritEventType type, Iterable<String> subtypes) {
    return new AutoValue_GerritEventTrigger(type, ImmutableSet.copyOf(subtypes));
  }
}
