package com.khulnasoft.bitclone.buildozer;

import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.buildozer.BuildozerOptions.BuildozerCommand;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.util.console.Console;
import java.nio.file.Path;

/** A class that can run a 'buildozer print' command. */
public class BuildozerPrintExecutor {
  private final BuildozerOptions options;
  private final Console console;

  private BuildozerPrintExecutor(BuildozerOptions options, Console console) {
    this.options = options;
    this.console = console;
  }

  public static BuildozerPrintExecutor create(BuildozerOptions options, Console console) {
    return new BuildozerPrintExecutor(options, console);
  }

  /**
   * Runs a Buildozer print command.
   *
   * @param attr The attribute from the target rule to print.
   * @param target The target to print from.
   * @return A string with the buildozer print output.
   * @throws ValidationException If there is an issue running buildozer print.
   */
  public String run(Path checkoutDir, String attr, String target) throws ValidationException {
    try {
      BuildozerCommand command = new BuildozerCommand(target, String.format("print %s", attr));
      return options.runCaptureOutput(console, checkoutDir, ImmutableList.of(command));
    } catch (TargetNotFoundException e) {
      throw new ValidationException("Buildozer could not find the specified target", e);
    }
  }
}
