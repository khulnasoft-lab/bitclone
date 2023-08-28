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

package com.khulnasoft.bitclone.config;

import com.khulnasoft.bitclone.ActionMigration;
import com.khulnasoft.bitclone.Workflow;
import com.khulnasoft.bitclone.git.Mirror;

/**
 * Validates Bitclone {@link Migration}s and returns a {@link ValidationResult}.
 *
 * <p>Implementations of this interface should not throw exceptions for validation errors.
 */
public abstract class MigrationValidator {

  public final ValidationResult validate(Migration migration) {
    if (migration instanceof Workflow) {
      return validateWorkflow(migration.getName(), (Workflow<?, ?>) migration);
    }
    if (migration instanceof Mirror) {
      return validateMirror(migration.getName(), (Mirror) migration);
    }
    if (migration instanceof ActionMigration) {
      return validateActionMigration(migration.getName(), (ActionMigration) migration);
    }
    throw new IllegalStateException(String.format("Validation missing for %s", migration));
  }

  /** Performs specific validation of a {@link Workflow} migration. */
  protected abstract ValidationResult validateWorkflow(String name, Workflow<?, ?> workflow);

  /** Performs specific validation of a {@link Mirror} migration. */
  protected abstract ValidationResult validateMirror(String name, Mirror mirror);

  /** Performs specific validation of a {@link ActionMigration} migration. */
  protected abstract ValidationResult validateActionMigration(
      String name, ActionMigration actionMigration);
}
