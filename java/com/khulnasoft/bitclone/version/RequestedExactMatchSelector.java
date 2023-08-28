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

package com.khulnasoft.bitclone.version;

import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.util.console.Console;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Given a requested reference, it returns that reference if it is an exact
 * match with one of the versions from VersionList.
 */
public class RequestedExactMatchSelector implements VersionSelector {

  @Override
  public Optional<String> select(VersionList versionList, @Nullable String requestedRef,
      Console console)
      throws ValidationException, RepoException {
    if (requestedRef != null && versionList.list().contains(requestedRef)) {
      return Optional.of(requestedRef);
    }
    return Optional.empty();
  }
}
