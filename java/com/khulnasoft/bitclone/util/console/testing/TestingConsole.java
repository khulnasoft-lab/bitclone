/*
 * Copyright (C) 2016 KhulnaSoft Ltd..
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

package com.khulnasoft.bitclone.util.console.testing;

import static com.google.common.truth.Truth.assertAbout;
import static com.khulnasoft.bitclone.util.console.Message.MessageType.VERBOSE;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.util.console.AnsiColor;
import com.khulnasoft.bitclone.util.console.CapturingConsole;
import com.khulnasoft.bitclone.util.console.Console;
import com.khulnasoft.bitclone.util.console.LogConsole;
import com.khulnasoft.bitclone.util.console.Message.MessageType;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * A testing console that allows programming the user input and deletages on a
 * {@link CapturingConsole} to intercept all the messages.
 *
 * <p>It also writes the output to a {@link LogConsole} for debug.
 */
public final class TestingConsole extends CapturingConsole {

  private enum PromptResponse {
    YES, NO,
  }

  private final Console outputConsole =
      LogConsole.writeOnlyConsole(System.out, /*verbose=*/true);
  private final ArrayDeque<PromptResponse> programmedResponses = new ArrayDeque<>();
  private final ArrayDeque<String> programmedStringResponses = new ArrayDeque<>();

  public TestingConsole() {
    this(/*verbose*/true);
  }

  private static final ImmutableSet<MessageType> ALL_BUT_VERBOSE =
      ImmutableSet.copyOf(EnumSet.complementOf(EnumSet.of(VERBOSE)));

  public TestingConsole(boolean verbose) {
    super(LogConsole.writeOnlyConsole(System.out, verbose), verbose ? ALL_TYPES : ALL_BUT_VERBOSE);
  }

  public TestingConsole respondYes() {
    this.programmedResponses.addLast(PromptResponse.YES);
    return this;
  }

  public TestingConsole respondNo() {
    this.programmedResponses.addLast(PromptResponse.NO);
    return this;
  }

  public TestingConsole respondWithString(String response) {
    this.programmedStringResponses.addLast(response);
    return this;
  }

  /**
   * Returns a truth subject that provides fluent methods for assertions on this instance.
   *
   * <p>For example:
   *
   *     testConsole.assertThat()
   *       .matchesNext(...)
   *       .equalsNext(...)
   *       .containsNoMoreMessages();
   */
  public LogSubjects.LogSubject assertThat() {
    return assertAbout(LogSubjects.CONSOLE_SUBJECT_FACTORY)
        .that(this);
  }

  @Override
  public boolean promptConfirmation(String message) {
    Preconditions.checkState(!programmedResponses.isEmpty(), "No more programmed responses.");
    // Validate prompt messages with WARN level in tests
    warn(message);
    return programmedResponses.removeFirst() == PromptResponse.YES;
  }

  @Override
  public String ask(String msg, @Nullable String defaultAnswer, Predicate<String> validator)
      throws IOException {
    while (true) {
      Preconditions.checkState(!programmedStringResponses.isEmpty(),
          "No more programmed responses for msg: '%s', defaultAnswer: '%s'",
          msg, defaultAnswer);
      info(msg);
      infoFmt("Responding with %s", programmedStringResponses.peekFirst());
      String response = programmedStringResponses.removeFirst();
      if (Strings.isNullOrEmpty(response) && defaultAnswer != null) {
        return defaultAnswer;
      }
      if (validator.test(response)) {
        return response;
      } else {
        warnFmt("Invalid response: %s", response);
      }
    }
  }

  @Override
  public String colorize(AnsiColor ansiColor, String message) {
    return outputConsole.colorize(ansiColor, message);
  }

  /**
   * Clear messages
   */
  public void reset() {
    clearMessages();
  }
}
