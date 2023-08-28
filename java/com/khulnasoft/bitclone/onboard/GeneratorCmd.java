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

package com.khulnasoft.bitclone.onboard;


import com.beust.jcommander.Parameters;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.CommandEnv;
import com.khulnasoft.bitclone.BitcloneCmd;
import com.khulnasoft.bitclone.GeneralOptions;
import com.khulnasoft.bitclone.ModuleSet;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.format.BuildifierOptions;
import com.khulnasoft.bitclone.git.GitOptions;
import com.khulnasoft.bitclone.onboard.core.CannotConvertException;
import com.khulnasoft.bitclone.onboard.core.CannotProvideException;
import com.khulnasoft.bitclone.onboard.core.ConstantProvider;
import com.khulnasoft.bitclone.onboard.core.InputProvider;
import com.khulnasoft.bitclone.onboard.core.InputProviderResolver;
import com.khulnasoft.bitclone.onboard.core.InputProviderResolverImpl;
import com.khulnasoft.bitclone.onboard.core.MapBasedInputProvider;
import com.khulnasoft.bitclone.onboard.core.template.ConfigGenerator;
import com.khulnasoft.bitclone.util.CommandOutputWithStatus;
import com.khulnasoft.bitclone.util.ExitCode;
import com.khulnasoft.bitclone.util.console.Console;
import com.khulnasoft.bitclone.shell.Command;
import com.khulnasoft.bitclone.shell.CommandException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A command that generates a config file based on user and inferred inputs.
 *
 * <p>TODO(malcon, joshgoldman): Rename to 'GenerateCmd' once we remove old version
 */
@Parameters(
    separators = "=",
    commandDescription = "Generates a config file by asking/inferring field information")
public class GeneratorCmd implements BitcloneCmd {

  protected static final int PERCENTAGE_SIMILAR = 30;

  private final ModuleSet moduleSet;

  public GeneratorCmd(ModuleSet moduleSet) {
    this.moduleSet = moduleSet;
  }

  @Override
  public ExitCode run(CommandEnv commandEnv)
      throws ValidationException, IOException, RepoException {
    GeneratorOptions genOpts = commandEnv.getOptions().get(GeneratorOptions.class);
    Console console = commandEnv.getOptions().get(GeneralOptions.class).console();

    ImmutableList<ConfigGenerator> generators = generators();
    Inputs.maybeSetTemplates(generators);
    for (ConfigGenerator generator : generators) {
      // force the generator to initialize its Inputs so tha they are declared in the registry
      var unused = generator.consumes();
    }

    try {
      InputProviderResolver resolver =
          InputProviderResolverImpl.create(
              inputProviders(genOpts, commandEnv, console),
              new StarlarkConverter(moduleSet, console),
              genOpts.askMode,
              console);
      Optional<Path> path = resolver.resolveOptional(Inputs.GENERATOR_FOLDER);
      if (path.isEmpty()) {
        console.error("Cannot infer a path to place the generated config");
        return ExitCode.COMMAND_LINE_ERROR;
      }
      ConfigGenerator template;
      try {
        template = selectGenerator(resolver, genOpts.template, console);
      } catch (CannotConvertException e) {
        console.error("Cannot infer a template for generating a config. Use --template flag.");
        return ExitCode.COMMAND_LINE_ERROR;
      }
      String config = template.generate(resolver);

      Path configDestination = path.get().resolve("bit.clone.sky");
      Files.write(configDestination, config.getBytes(StandardCharsets.UTF_8));

      format(commandEnv, configDestination);

      console.infoFmt("%s created", configDestination);

    } catch (InterruptedException e) {
      console.error("Interrupted: " + e.getMessage());
      return ExitCode.INTERRUPTED;
    } catch (CannotProvideException e) {
      console.error("Cannot resolve input field: " + e.getMessage());
      return ExitCode.COMMAND_LINE_ERROR;
    }
    return ExitCode.SUCCESS;
  }

  private void format(CommandEnv commandEnv, Path config) throws CannotProvideException {
    GeneralOptions generalOptions = commandEnv.getOptions().get(GeneralOptions.class);
    BuildifierOptions buildifierOptions = commandEnv.getOptions().get(BuildifierOptions.class);
    Command cmd =
        new Command(
            new String[] {
              buildifierOptions.buildifierBin, "-type=bzl", config.toAbsolutePath().toString()
            },
            /* environmentVariables= */ null,
            config.getParent().toFile());
    try {
      CommandOutputWithStatus unused =
          generalOptions.newCommandRunner(cmd).withVerbose(generalOptions.isVerbose()).execute();
    } catch (CommandException e) {
      throw new CannotProvideException("Cannot format generated config " + config, e);
    }
  }

  protected ImmutableList<InputProvider> inputProviders(
      GeneratorOptions genOpts, CommandEnv commandEnv, Console console)
      throws CannotProvideException {

    ImmutableMap<String, String> inputs = genOpts.inputs;

    return ImmutableList.of(
        new ConstantProvider<>(
            Inputs.GENERATOR_FOLDER, commandEnv.getOptions().get(GeneralOptions.class).getCwd()),
        new ConfigHeuristicsInputProvider(
            commandEnv.getOptions().get(GitOptions.class),
            commandEnv.getOptions().get(GeneralOptions.class),
            ImmutableSet.of(),
            PERCENTAGE_SIMILAR,
            console),
        new MapBasedInputProvider(inputs, InputProvider.COMMAND_LINE_PRIORITY));
  }

  private ConfigGenerator selectGenerator(
      InputProviderResolver resolver, @Nullable String cliTemplate, Console console)
      throws CannotConvertException, CannotProvideException, InterruptedException {
    ImmutableList<ConfigGenerator> generators = generators();
    if (cliTemplate != null) {
      return Inputs.templateInput().convert(cliTemplate, resolver);
    }
    for (ConfigGenerator generator : generators) {
      if (generator.isGenerator(resolver)) {
        console.info("Using '" + generator.name() + "' template");
        return generator;
      }
    }
    return resolver.resolve(Inputs.templateInput());
  }


  /**
   * A priority ordered lists of templates that can be useds
   */
  protected ImmutableList<ConfigGenerator> generators() {
    return ImmutableList.of(
        new GitToGitGenerator()
    );
  }

  @Override
  public String name() {
    // TODO(malcon, joshgoldman): Rename to 'generate' once we remove old version
    return "generator";
  }
}
