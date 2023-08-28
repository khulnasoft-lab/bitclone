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

package com.khulnasoft.bitclone.git;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.ChangeVisitable.ChangesVisitor;
import com.khulnasoft.bitclone.ChangeVisitable.VisitResult;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.exception.CannotResolveRevisionException;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.profiler.Profiler.ProfilerTask;
import com.khulnasoft.bitclone.revision.Change;

/** Utility methods for visiting Git repos. */
public class GitVisitorUtil {

  private GitVisitorUtil() {}

  /** Visits */
  static void visitChanges(
      GitRevision start,
      ChangesVisitor visitor,
      ChangeReader.Builder queryChanges,
      GeneralOptions generalOptions,
      String type,
      int visitChangePageSize)
      throws RepoException, ValidationException {
    Preconditions.checkNotNull(start);
    int skip = 0;
    boolean finished = false;
    try (ProfilerTask ignore = generalOptions.profiler().start(type + "/visit_changes")) {
      while (!finished) {
        ImmutableList<Change<GitRevision>> result;
        try (ProfilerTask ignore2 =
            generalOptions.profiler().start("git_log_" + skip + "_" + visitChangePageSize)) {
          result =
              queryChanges
                  .setSkip(skip)
                  .setLimit(visitChangePageSize)
                  .build()
                  .run(start.getSha1())
                  .reverse();
        }
        if (result.isEmpty()) {
          break;
        }
        skip += result.size();
        for (Change<GitRevision> current : result) {
          if (visitor.visit(current) == VisitResult.TERMINATE) {
            finished = true;
            break;
          }
        }
      }
    }
    if (skip == 0) {
      throw new CannotResolveRevisionException("Cannot resolve reference " + start.getSha1());
    }
  }
}
