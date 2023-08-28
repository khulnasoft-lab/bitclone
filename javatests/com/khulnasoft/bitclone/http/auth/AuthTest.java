/*
 * Copyright (C) 2023 KhulnaSoft Ltd.
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

package com.khulnasoft.bitclone.http.auth;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Base64;
import com.google.common.io.MoreFiles;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.http.HttpOptions;
import com.khulnasoft.bitclone.http.testing.MockHttpTester;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.SkylarkTestExecutor;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AuthTest {
  private SkylarkTestExecutor starlark;
  private MockHttpTester http;
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  private final HttpOptions httpOptions =
      new HttpOptions() {
        @Override
        public HttpTransport getTransport() {
          return http.getTransport();
        }
      };

  @Before
  public void setUp() {
    http = new MockHttpTester();
    OptionsBuilder optionsBuilder = new OptionsBuilder();
    optionsBuilder.http = httpOptions;
    starlark = new SkylarkTestExecutor(optionsBuilder);
  }

  private String basicAuth(String username, String password) {
    return String.format(
        "Basic %s",
        Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes(UTF_8)));
  }

  @Test
  public void testBasicAuth() throws ValidationException {
    String username = "testuser";
    String password = "testpassword";

    String expectedAuthString = basicAuth(username, password);
    http.mockHttp(
        (method, url, req, resp) -> {
          assertThat(req.getHeaders().get("authorization")).containsExactly(expectedAuthString);
        });
    starlark.eval(
        "resp",
        String.format(""
            + "endpoint = testing.get_endpoint(\n"
            + "  http.endpoint(host = \"foo.com\")\n"
            + ")\n"
            + "resp = endpoint.post(\n"
            + "  url = \"http://foo.com\",\n"
            + "  auth = http.auth(\"%s\", \"%s\")\n"
            + ")", username, password));
  }

  @Test
  public void testTomlDataSource() throws ValidationException, IOException {
    String username = "testuser";
    String password = "testpassword";

    Path configPath = tempFolder.getRoot().toPath().resolve("credentials.toml");
    httpOptions.credentialFile = configPath;

    MoreFiles.asByteSink(configPath)
        .asCharSink(UTF_8)
        .write(String.format("" + "[bitclone.test]\n" + "username = \"%s\"\n", username));
    String tomlKeyPath = "bitclone.test.username";

    String expectedAuthString = basicAuth(username, password);

    http.mockHttp(
        (method, url, req, resp) -> {
          assertThat(req.getHeaders().get("authorization")).containsExactly(expectedAuthString);
        });

    starlark.eval(
        "resp",
        String.format(
            ""
                + "endpoint = testing.get_endpoint(\n"
                + "  http.endpoint(host = \"foo.com\")\n"
                + ")\n"
                + "resp = endpoint.post(\n"
                + "  url = \"http://foo.com\",\n"
                + "  auth = http.auth(\n"
                + "    http.toml_key_source(\"%s\"),\n"
                + "    \"%s\"\n"
                + "  )"
                + ")",
            tomlKeyPath, password));
  }
}
