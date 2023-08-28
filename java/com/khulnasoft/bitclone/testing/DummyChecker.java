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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.checks.Checker;
import com.khulnasoft.bitclone.checks.CheckerException;
import com.khulnasoft.bitclone.checks.DescriptionChecker;
import com.khulnasoft.bitclone.util.console.Console;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.starlark.java.annot.StarlarkBuiltin;

/**
 * A dummy, not very efficient, checker for tests.
 *
 * <p>TODO(danielromero): Promote to a real transform that uses regex
 */
@StarlarkBuiltin(name = "dummy_checker", doc = "A dummy checker for tests")
public class DummyChecker implements Checker, DescriptionChecker {

  private final ImmutableSet<String> badWords;
  @Nullable private final String descriptionOnError;

  /**
   * Creates a new checker.
   *
   * @param badWords Case-insensitive set of bad words
   */
  public DummyChecker(ImmutableSet<String> badWords) {
    this(badWords, null);
  }

  public DummyChecker(ImmutableSet<String> badWords, @Nullable String descriptionOnError) {
    this.badWords =
        badWords.stream().map(String::toLowerCase).collect(ImmutableSet.toImmutableSet());
    this.descriptionOnError = descriptionOnError;
  }

  /**
   * Fails on first bad word found.
   */
  @Override
  public void doCheck(ImmutableMap<String, String> fields, Console console)
      throws CheckerException {
    for (Entry<String, String> entry : fields.entrySet()) {
      for (String badWord : badWords) {
        if (entry.getValue().toLowerCase().contains(badWord)) {
          throw new CheckerException(
              String.format("Bad word '%s' found: field '%s'", badWord, entry.getKey()));
        }
      }
    }
  }

  /**
   * Does a line by line check. Does not detect bad words if multi-line. Fails on first bad word
   * found.
   */
  @Override
  public void doCheck(Path target, Console console) throws CheckerException, IOException {
    AtomicReference<CheckerException> e = new AtomicReference<>();
    SimpleFileVisitor<Path> visitor =
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            try {
              checkFile(file);
            } catch (CheckerException ex) {
              e.set(ex);
              return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
          }
        };

    Files.walkFileTree(target, visitor);
    if (e.get() != null) {
      throw e.get();
    }
  }

  @Override
  public String processDescription(String description, Console console) throws CheckerException {
    String lowerCase = description.toLowerCase();
    for (String badWord : badWords) {
      if (!lowerCase.contains(badWord)) {
        continue;
      }
      if (descriptionOnError != null) {
        return descriptionOnError;
      }
      throw new CheckerException(String.format("Bad word '%s' found in description", badWord));
    }
    return description;
  }

  private void checkFile(Path target) throws IOException, CheckerException {
    int lineNum = 0;
    for (String line : Files.readAllLines(target)) {
      lineNum++;
      for (String badWord : badWords) {
        if (line.toLowerCase().contains(badWord)) {
          throw new CheckerException(
              String.format("Bad word '%s' found: %s:%d", badWord, target, lineNum));
        }
      }
    }
  }
}
