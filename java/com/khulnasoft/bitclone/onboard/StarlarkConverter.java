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

import static com.khulnasoft.bitclone.util.console.StarlarkMode.STRICT;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableMap;
import com.khulnasoft.bitclone.ModuleSet;
import com.khulnasoft.bitclone.config.MapConfigFile;
import com.khulnasoft.bitclone.config.SkylarkParser;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.onboard.core.CannotConvertException;
import com.khulnasoft.bitclone.onboard.core.Converter;
import com.khulnasoft.bitclone.onboard.core.InputProviderResolver;
import com.khulnasoft.bitclone.util.console.Console;
import java.io.IOException;
import net.starlark.java.eval.Module;

/**
 * A converter that given an arbitrary string, transforms it to the equivalent Starlark/Java object.
 */
public class StarlarkConverter implements Converter<Object> {
  private final Console console;
  private final SkylarkParser skylarkParser;
  private final ModuleSet moduleSet;

  public StarlarkConverter(ModuleSet moduleSet, Console console) {
    this.console = console;
    skylarkParser = new SkylarkParser(moduleSet.getStaticModules(), STRICT);
    this.moduleSet = moduleSet;
  }

  @Override
  public Object convert(String value, InputProviderResolver resolver)
      throws CannotConvertException {
    MapConfigFile content =
        new MapConfigFile(
            ImmutableMap.of("bit.clone.sky", ("CONVERTED_VAR = " + value).getBytes(UTF_8)),
            "bit.clone.sky");
    try {
      Module module = skylarkParser.executeSkylark(content, moduleSet, console);
      return module.getGlobal("CONVERTED_VAR");
    } catch (InterruptedException | ValidationException | IOException e) {
      // Not ideal, but given the scope of the call (narrow, for a conversion), it is fine
      throw new CannotConvertException("Cannot convert value " + value + ": " + e.getMessage(), e);
    }
  }
}
