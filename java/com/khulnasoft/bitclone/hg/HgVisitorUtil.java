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

package com.khulnasoft.bitclone.hg;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.ChangeVisitable.ChangesVisitor;
import com.khulnasoft.bitclone.ChangeVisitable.VisitResult;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.profiler.Profiler.ProfilerTask;
import com.khulnasoft.bitclone.revision.Change;

/**
 * Utility methods for visiting Mercurial (Hg) repositories.
 */
public class HgVisitorUtil {

  private HgVisitorUtil() {}

  /**
   * Visits Hg changes, up to the termination point specified by the visitor.
   */
  static void visitChanges(
      HgRevision start,
      ChangesVisitor visitor,
      ChangeReader.Builder queryChanges,
      GeneralOptions generalOptions,
      String type,
      int visitChangePageSize)
      throws RepoException {
    Preconditions.checkNotNull(start);
    int offset = 0;
    boolean finished = false;

    try (ProfilerTask ignore = generalOptions.profiler().start(type + "/visit_changes")) {
      while (!finished) {
        ImmutableList<Change<HgRevision>> result;
        try (ProfilerTask ignore2 =
            generalOptions.profiler().start(
                String.format("hg_log_%d_%d", offset, visitChangePageSize))) {
          try {
            result =
                queryChanges
                    .setSkip(offset)
                    .setLimit(visitChangePageSize)
                    .build()
                    .run(start.getGlobalId())
                    .reverse();
          } catch (ValidationException e) {
            throw new RepoException(
                String.format("Error querying changes: %s", e.getMessage()), e.getCause());
          }
        }

        if (result.isEmpty()) {
          break;
        }

        offset += result.size();
        for (Change<HgRevision> current : result) {
          if (visitor.visit(current) == VisitResult.TERMINATE) {
            finished = true;
            break;
          }
        }
      }
    }
  }
}
