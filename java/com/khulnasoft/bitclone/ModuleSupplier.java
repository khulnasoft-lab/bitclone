/*
 * Copyright (C) 2016 KhulnaSoft Ltd.
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

package com.khulnasoft.bitclone;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.archive.ArchiveModule;
import com.khulnasoft.bitclone.authoring.Authoring;
import com.khulnasoft.bitclone.buildozer.BuildozerModule;
import com.khulnasoft.bitclone.buildozer.BuildozerOptions;
import com.khulnasoft.bitclone.compression.CompressionModule;
import com.khulnasoft.bitclone.folder.FolderDestinationOptions;
import com.khulnasoft.bitclone.folder.FolderModule;
import com.khulnasoft.bitclone.folder.FolderOriginOptions;
import com.khulnasoft.bitclone.format.BuildifierOptions;
import com.khulnasoft.bitclone.format.FormatModule;
import com.khulnasoft.bitclone.git.GerritOptions;
import com.khulnasoft.bitclone.git.GitDestinationOptions;
import com.khulnasoft.bitclone.git.GitHubDestinationOptions;
import com.khulnasoft.bitclone.git.GitHubOptions;
import com.khulnasoft.bitclone.git.GitHubPrOriginOptions;
import com.khulnasoft.bitclone.git.GitMirrorOptions;
import com.khulnasoft.bitclone.git.GitModule;
import com.khulnasoft.bitclone.git.GitOptions;
import com.khulnasoft.bitclone.git.GitOriginOptions;
import com.khulnasoft.bitclone.go.GoModule;
import com.khulnasoft.bitclone.hashing.HashingModule;
import com.khulnasoft.bitclone.hg.HgModule;
import com.khulnasoft.bitclone.hg.HgOptions;
import com.khulnasoft.bitclone.hg.HgOriginOptions;
import com.khulnasoft.bitclone.http.HttpModule;
import com.khulnasoft.bitclone.http.HttpOptions;
import com.khulnasoft.bitclone.onboard.GeneratorOptions;
import com.khulnasoft.bitclone.python.PythonModule;
import com.khulnasoft.bitclone.re2.Re2Module;
import com.khulnasoft.bitclone.regenerate.RegenerateOptions;
import com.khulnasoft.bitclone.remotefile.RemoteFileModule;
import com.khulnasoft.bitclone.remotefile.RemoteFileOptions;
import com.khulnasoft.bitclone.rust.RustModule;
import com.khulnasoft.bitclone.toml.TomlModule;
import com.khulnasoft.bitclone.transform.debug.DebugOptions;
import com.khulnasoft.bitclone.transform.metadata.MetadataModule;
import com.khulnasoft.bitclone.transform.patch.PatchModule;
import com.khulnasoft.bitclone.transform.patch.PatchingOptions;
import com.khulnasoft.bitclone.util.console.Console;
import com.khulnasoft.bitclone.xml.XmlModule;
import java.nio.file.FileSystem;
import java.util.Map;
import java.util.function.Function;
import net.starlark.java.annot.StarlarkBuiltin;
import net.starlark.java.lib.json.Json;

/**
 * A supplier of modules and {@link Option}s for Bitclone.
 */
public class ModuleSupplier {

  private static final ImmutableSet<Class<?>> BASIC_MODULES = ImmutableSet.of(
      CoreGlobal.class);
  private final Map<String, String> environment;
  private final FileSystem fileSystem;
  private final Console console;

  public ModuleSupplier(Map<String, String> environment, FileSystem fileSystem,
      Console console) {
    this.environment = Preconditions.checkNotNull(environment);
    this.fileSystem = Preconditions.checkNotNull(fileSystem);
    this.console = Preconditions.checkNotNull(console);
  }

  /**
   * Returns the {@code set} of modules available.
   * TODO(malcon): Remove once no more static modules exist.
   */
  protected ImmutableSet<Class<?>> getStaticModules() {
    return BASIC_MODULES;
  }

  /**
   * Get non-static modules available
   */
  public ImmutableSet<Object> getModules(Options options) {
    GeneralOptions general = options.get(GeneralOptions.class);
    FolderModule folderModule = new FolderModule(
        options.get(FolderOriginOptions.class),
        options.get(FolderDestinationOptions.class),
        general);
    return ImmutableSet.of(
        new Core(
            general,
            options.get(WorkflowOptions.class),
            options.get(DebugOptions.class),
            folderModule),
        new GitModule(options),
        new HgModule(options),
        folderModule,
        new FormatModule(
            options.get(WorkflowOptions.class), options.get(BuildifierOptions.class), general),
        new BuildozerModule(
            options.get(WorkflowOptions.class), options.get(BuildozerOptions.class)),
        new PatchModule(options.get(PatchingOptions.class)),
        new MetadataModule(),
        new Authoring.Module(),
        new RemoteFileModule(options),
        new ArchiveModule(),
        new Re2Module(),
        new TomlModule(),
        new XmlModule(),
        new StructModule(),
        new StarlarkDateTimeModule(),
        new GoModule(options.get(RemoteFileOptions.class)),
        new RustModule(options.get(RemoteFileOptions.class)),
        new HashingModule(),
        new HttpModule(console, options.get(HttpOptions.class)),
        new PythonModule(),
        new CompressionModule(),
        Json.INSTANCE);
  }

  /** Returns a new list of {@link Option}s. */
  protected Options newOptions() {
    GeneralOptions generalOptions = new GeneralOptions(environment, fileSystem, console);
    GitOptions gitOptions = new GitOptions(generalOptions);
    GitDestinationOptions gitDestinationOptions =
        new GitDestinationOptions(generalOptions, gitOptions);
    BuildifierOptions buildifierOptions = new BuildifierOptions();
    WorkflowOptions workflowOptions = new WorkflowOptions();
    return new Options(
        ImmutableList.of(
            generalOptions,
            buildifierOptions,
            new BuildozerOptions(generalOptions, buildifierOptions, workflowOptions),
            new FolderDestinationOptions(),
            new FolderOriginOptions(),
            gitOptions,
            new GitOriginOptions(),
            new GitHubPrOriginOptions(),
            gitDestinationOptions,
            new GitHubOptions(generalOptions, gitOptions),
            new GitHubDestinationOptions(),
            new GerritOptions(generalOptions, gitOptions),
            new GitMirrorOptions(),
            new HgOptions(generalOptions),
            new HgOriginOptions(),
            new PatchingOptions(generalOptions),
            workflowOptions,
            new RemoteFileOptions(),
            new DebugOptions(generalOptions),
            new GeneratorOptions(),
            new HttpOptions(),
            new RegenerateOptions()));
  }

  /**
   * A ModuleSet contains the collection of modules and flags for one Skylark bit.clone.sky
   * evaluation/execution.
   */
  public final ModuleSet create() {
    Options options = newOptions();
    return createWithOptions(options);
  }

  public final ModuleSet createWithOptions(Options options) {
    return new ModuleSet(options, getStaticModules(), modulesToVariableMap(options));
  }

  private ImmutableMap<String, Object> modulesToVariableMap(Options options) {
    return getModules(options).stream()
        .collect(ImmutableMap.toImmutableMap(
            this::findClosestStarlarkBuiltinName,
            Function.identity()));
  }

  private String findClosestStarlarkBuiltinName(Object o) {
    Class<?> cls = o.getClass();
    while (cls != null && cls != Object.class) {
      StarlarkBuiltin annotation = cls.getAnnotation(StarlarkBuiltin.class);
      if (annotation != null) {
        return annotation.name();
      }
      cls = cls.getSuperclass();
    }
    throw new IllegalStateException("Cannot find @StarlarkBuiltin for " + o.getClass());
  }
}
