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
package com.khulnasoft.bitclone.http.json;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.http.HttpTransport;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.http.HttpOptions;
import com.khulnasoft.bitclone.http.testing.MockHttpTester;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.SkylarkTestExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class HttpEndpointJsonContentTest {
  private SkylarkTestExecutor starlark;
  private MockHttpTester http;
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setUp() {
    http = new MockHttpTester();
    OptionsBuilder optionsBuilder = new OptionsBuilder();
    optionsBuilder.http =
        new HttpOptions() {
          @Override
          public HttpTransport getTransport() {
            return http.getTransport();
          }
        };
    starlark = new SkylarkTestExecutor(optionsBuilder);
  }

  @Test
  public void testContentType() throws ValidationException {
    http.mockHttp(
        (method, url, req, resp) -> assertThat(req.getContentType()).contains("application/json"));
    var unused =
        starlark.eval(
            "resp",
            "endpoint = testing.get_endpoint(\n"
                + "  http.endpoint(host = \"foo.com\")\n"
                + "  )\n"
                + "resp = endpoint.post(\n"
                + "  url = \"http://foo.com\",\n"
                + "  content = http.json({})\n"
                + ")");
  }

  @Test
  public void testFormContentsAreEncoded() throws ValidationException {
    http.mockHttp(
        (method, url, req, resp) -> {
          String content = req.getContentAsString();
          String expectedContent = "{\"testfield\":\"http://foo.com\",\"foofield\":\"@special#\"}";
          assertThat(content).isEqualTo(expectedContent);
        });
    starlark.eval(
        "resp",
        "endpoint = testing.get_endpoint(\n"
            + "  http.endpoint(host = \"foo.com\")\n"
            + "  )\n"
            + "resp = endpoint.post(\n"
            + "  url = \"http://foo.com\",\n"
            + "  content = http.json({\n"
            + "    \"testfield\": \"http://foo.com\",\n"
            + "    \"foofield\": \"@special#\",\n"
            + "  })\n"
            + ")");
  }
}
