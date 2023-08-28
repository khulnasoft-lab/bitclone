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

package com.khulnasoft.bitclone.buildozer;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.TransformWork;
import com.khulnasoft.bitclone.Transformation;
import com.khulnasoft.bitclone.TransformationStatus;
import com.khulnasoft.bitclone.WorkflowOptions;
import com.khulnasoft.bitclone.buildozer.BuildozerOptions.BuildozerCommand;
import com.khulnasoft.bitclone.exception.NonReversibleValidationException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.util.console.Console;
import java.util.ArrayList;
import java.util.List;

/**
 * A transformation which runs one or more commands against a single target.
 */
public final class BuildozerModify implements BuildozerTransformation {

  private final BuildozerOptions options;
  private final WorkflowOptions workflowOptions;
  private final List<Target> targets;
  private final ImmutableList<Command> commands;

  BuildozerModify(
      BuildozerOptions options,
      WorkflowOptions workflowOptions,
      List<Target> targets,
      Iterable<Command> commands) {
    this.options = checkNotNull(options, "options");
    this.workflowOptions = checkNotNull(workflowOptions, "workflowOptions");
    this.targets = checkNotNull(targets, "target");
    this.commands = ImmutableList.copyOf(commands);
  }

  @Override
  public TransformationStatus transform(TransformWork work)
      throws ValidationException {
    Console console = work.getConsole();
    try {
      options.run(console, work.getCheckoutDir(), getCommands());
      return TransformationStatus.success();
    } catch (TargetNotFoundException e) {
      return TransformationStatus.noop(e.getMessage());
    }
  }

  @Override
  public BuildozerModify reverse() throws NonReversibleValidationException {
    List<Command> reverseCommands = new ArrayList<>();
    for (Command command : commands.reverse()) {
      reverseCommands.add(command.reverse());
    }
    return new BuildozerModify(options, workflowOptions, targets, reverseCommands);
  }

  @Override
  public Iterable<BuildozerCommand> getCommands() {
    List<BuildozerCommand> result = new ArrayList<>();
    for (Command command : commands) {
      result.add(new BuildozerCommand(Target.asStringList(targets), command.toString()));
    }
    return result;
  }

  @Override
  public boolean canJoin(Transformation transformation) {
    return BuildozerBatch.isBuildozer(transformation);
  }

  @Override
  public Transformation join(Transformation next) {
    return BuildozerBatch.join(options, workflowOptions, this, (BuildozerTransformation) next);
  }

  @Override
  public String describe() {
    return "buildozer.modify " + targets;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("target", targets)
        .add("commands", commands)
        .toString();
  }
}
