/*
 * Copyright (C) 2018 KhulnaSoft Ltd.
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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.khulnasoft.bitclone.config.Config;
import com.khulnasoft.bitclone.config.ConfigValidator;
import com.khulnasoft.bitclone.config.ValidationResult;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.revision.Revision;
import com.khulnasoft.bitclone.testing.DummyOrigin;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.RecordsProcessCallDestination;
import com.khulnasoft.bitclone.testing.RecordsProcessCallDestination.DestinationInfoImpl;
import com.khulnasoft.bitclone.testing.SkylarkTestExecutor;
import com.khulnasoft.bitclone.util.console.Console;
import com.khulnasoft.bitclone.util.console.StarlarkMode;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReadConfigFromChangeWorkflowTest {

  private OptionsBuilder options;
  private DummyOrigin origin;
  private RecordsProcessCallDestination destination;
  private SkylarkTestExecutor skylark;

  @Before
  public void setup() {
    options = new OptionsBuilder();
    origin = new DummyOrigin();
    destination = new RecordsProcessCallDestination();
    options.testingOptions.origin = origin;
    options.testingOptions.destination = destination;
    options.general.starlarkMode = StarlarkMode.STRICT.name();
    skylark = new SkylarkTestExecutor(options);
  }

  /**
   * A test that check that we can mutate the glob in iterative mode
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testWriterStateMaintained() throws Exception {
    options.workflowOptions.lastRevision = "0";
    String configCode = mutatingWorkflow("*");
    Config cfg = skylark.loadConfig(configCode);
    ConfigLoader constantConfigLoader =
        new ConfigLoader(
            skylark.createModuleSet(),
            skylark.createConfigFile("bit.clone.sky", configCode),
            options.general.getStarlarkMode()) {
          @Override
          protected Config doLoadForRevision(Console console, Revision revision)
              throws ValidationException {
            try {
              return skylark.loadConfig(mutatingWorkflow(revision.asString()));
            } catch (IOException e) {
              throw new AssertionError("Should not fail", e);
            }
          }
        };
    ReadConfigFromChangeWorkflow<?, ?> wf = new ReadConfigFromChangeWorkflow<>(
        (Workflow) cfg.getMigration("default"),
        options.build(),
        constantConfigLoader, new ConfigValidator() {
      @Override
      public ValidationResult validate(Config config, String migrationName) {
        return ValidationResult.EMPTY;
      }
    });

    origin.singleFileChange(0, "base", "fileB", "b");
    origin.singleFileChange(1, "one", "file1", "b");
    origin.singleFileChange(2, "two", "file2", "b");
    origin.singleFileChange(3, "three", "file3", "b");

    wf.run(Files.createTempDirectory("workdir"), ImmutableList.of("3"));
    assertThat(destination.processed).hasSize(3);
    assertThat(destination.processed.get(0).getDestinationFiles().toString()).contains("file1");
    assertThat(destination.processed.get(0).getWorkdir()).containsExactly("file1", "b");
    assertThat(destination.processed.get(1).getDestinationFiles().toString()).contains("file2");
    assertThat(destination.processed.get(1).getWorkdir()).containsExactly("file2", "b");
    assertThat(destination.processed.get(2).getDestinationFiles().toString()).contains("file3");
    assertThat(destination.processed.get(2).getWorkdir()).containsExactly("file3", "b");

    DestinationInfoImpl destinationInfo1 =
        (DestinationInfoImpl) destination.processed.get(0).getDestinationInfo();
    assertThat((Iterable<?>) destinationInfo1.getValues("filename")).containsExactly("file1");
    DestinationInfoImpl destinationInfo2 =
        (DestinationInfoImpl) destination.processed.get(1).getDestinationInfo();
    assertThat((Iterable<?>) destinationInfo2.getValues("filename")).containsExactly("file2");
    DestinationInfoImpl destinationInfo3 =
        (DestinationInfoImpl) destination.processed.get(2).getDestinationInfo();
    assertThat((Iterable<?>) destinationInfo3.getValues("filename")).containsExactly("file3");
  }

  private String mutatingWorkflow(String suffix) {
    return "def _dynamicTransform(ctx):\n"
        + "    ctx.destination_info().add_value('filename', 'file" + suffix + "')\n"
        + "core.workflow("
        + "    name = 'default',"
        + "    origin = testing.origin(),"
        + "    mode = 'ITERATIVE',"
        + "    origin_files = glob(['file" + suffix + "']),"
        + "    destination_files = glob(['file" + suffix + "']),"
        + "    destination = testing.destination(),"
        + "    transformations = [core.dynamic_transform(_dynamicTransform)],"
        + "    authoring = authoring.pass_thru('foo <foo@foo.com>')"
        + ")";
  }
}
