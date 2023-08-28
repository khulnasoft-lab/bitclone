/*
 * Copyright (C) 2020 KhulnaSoft Ltd..
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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.git.version.RefspecVersionList.TagVersionList;
import com.khulnasoft.bitclone.git.version.RequestedShaVersionSelector;
import com.khulnasoft.bitclone.go.PseudoVersionSelector;
import com.khulnasoft.bitclone.util.console.Console;
import com.khulnasoft.bitclone.version.CorrectorVersionSelector;
import com.khulnasoft.bitclone.version.OrderedVersionSelector;
import com.khulnasoft.bitclone.version.RequestedExactMatchSelector;
import com.khulnasoft.bitclone.version.RequestedVersionSelector;
import javax.annotation.Nullable;

/**
 * A VersionSelector that heuristically tries to match a version to a git tag. This is best effort
 * and only recommended for testing.
 */
public class FuzzyClosestVersionSelector {

  public String selectVersion(@Nullable String requestedRef, GitRepository repo, String url,
      Console console) throws ValidationException {
    // Move this check where it is used
    ValidationException.checkCondition(!Strings.isNullOrEmpty(requestedRef),
        "Fuzzy version finding requires a ref to be explicitly specified");

    OrderedVersionSelector selector =
        new OrderedVersionSelector(
            ImmutableList.of(
                new PseudoVersionSelector(),
                new RequestedShaVersionSelector(),
                new RequestedExactMatchSelector(),
                new CorrectorVersionSelector(console),
                new RequestedVersionSelector()));
    try {
      return selector.select(new TagVersionList(repo, url), requestedRef, console).get();
    } catch (RepoException e) {
      // Technically this could be a real RepoException, but the current interface
      //
      console.warnFmt("Unable to obtain tags for %s. %s", url, e);
      return requestedRef;
    }
    // TODO(malcon): I think the old implementation returns requestedRef if cannot find a version.
    // check what we do in the diff and match the logic.
  }
}
