/*
 * Copyright (C) 2020 KhulnaSoft Ltd..
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

package com.khulnasoft.bitclone.remotefile;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Suppliers;
import com.khulnasoft.bitclone.Option;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.jcommander.DurationConverter;
import java.time.Duration;
import java.util.function.Supplier;

/** Options for loading files from a source other than the origin. Use with caution. */
@Parameters(separators = "=")
public class RemoteFileOptions implements Option {

  @Parameter(
      names = "--remote-http-files-connection-timeout",
      description = "Timeout for the fetch operation, e.g. 30s.",
      converter = DurationConverter.class)
  protected Duration connectionTimeout = Duration.ofMinutes(2);

  public Supplier<HttpStreamFactory> transport =
      Suppliers.memoize(() -> new GclientHttpStreamFactory(connectionTimeout));

  public HttpStreamFactory getTransport() throws ValidationException {
    return transport.get();
  }

}
