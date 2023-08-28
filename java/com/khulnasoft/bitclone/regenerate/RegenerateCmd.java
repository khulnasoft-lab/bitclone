/*
 * Copyright (C) 2023 KhulnaSoft Ltd..
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
package com.khulnasoft.bitclone.regenerate;

import static com.khulnasoft.bitclone.exception.ValidationException.checkCondition;

import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.CommandEnv;
import com.khulnasoft.bitclone.ConfigFileArgs;
import com.khulnasoft.bitclone.ConfigLoaderProvider;
import com.khulnasoft.bitclone.BitcloneCmd;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.Workflow;
import com.khulnasoft.bitclone.WorkflowOptions;
import com.khulnasoft.bitclone.config.Migration;
import com.khulnasoft.bitclone.config.SkylarkParser.ConfigWithDependencies;
import com.khulnasoft.bitclone.exception.CommandLineException;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.revision.Revision;
import com.khulnasoft.bitclone.util.ExitCode;
import com.khulnasoft.bitclone.util.console.Console;
import java.io.IOException;
import javax.annotation.Nullable;

/**
 * RegenerateCmd is used to recreate the patch representing the destination-only changes after
 * manual edits are made to a destination change.
 */
public class RegenerateCmd implements BitcloneCmd {
  private final ConfigLoaderProvider configLoaderProvider;

  public RegenerateCmd(ConfigLoaderProvider configLoaderProvider) {
    this.configLoaderProvider = configLoaderProvider;
  }

  @Override
  public ExitCode run(CommandEnv commandEnv)
      throws ValidationException, IOException, RepoException {
    ConfigFileArgs configFileArgs = commandEnv.parseConfigFileArgs(this, /*useSourceRef*/ true);
    ImmutableList<String> sourceRefs = configFileArgs.getSourceRefs();
    if (sourceRefs.size() > 1) {
      throw new CommandLineException(
          String.format(
              "Workflow does not support multiple source_ref arguments yet: %s",
              ImmutableList.copyOf(sourceRefs)));
    }
    @Nullable String sourceRef = sourceRefs.size() == 1 ? sourceRefs.get(0) : null;

    GeneralOptions options = commandEnv.getOptions().get(GeneralOptions.class);
    WorkflowOptions workflowOptions = commandEnv.getOptions().get(WorkflowOptions.class);
    RegenerateOptions regenerateOptions = commandEnv.getOptions().get(RegenerateOptions.class);
    Console console = options.console();

    ConfigWithDependencies config =
        configLoaderProvider
            .newLoader(configFileArgs.getConfigPath(), configFileArgs.getSourceRef())
            .loadWithDependencies(console);

    String workflowName = configFileArgs.getWorkflowName();
    console.infoFmt("Running regenerate for workflow %s", workflowName);

    Migration migration = config.getConfig().getMigration(workflowName);
    checkCondition(
        migration instanceof Workflow,
        "regenerate patch files is only supported for workflow migrations");

    Workflow<? extends Revision, ? extends Revision> workflow =
        (Workflow<? extends Revision, ? extends Revision>) migration;

    Regenerate<? extends Revision, ? extends Revision> regenerate =
        Regenerate.newRegenerate(
            workflow,
            commandEnv.getWorkdir(),
            options,
            workflowOptions,
            regenerateOptions,
            sourceRef);
    regenerate.regenerate();

    return ExitCode.SUCCESS;
  }

  @Override
  public String name() {
    return "regenerate";
  }
}
