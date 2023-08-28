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

package com.khulnasoft.bitclone.git.github.api;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.khulnasoft.bitclone.checks.ApiChecker;
import com.khulnasoft.bitclone.checks.CheckerException;
import com.khulnasoft.bitclone.exception.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class GitHubApiTransportWithCheckerTest {

  private GitHubApiTransport delegate;
  private ApiChecker checker;

  private GitHubApiTransport transport;

  @Before
  public void setup() throws Exception {
    delegate = Mockito.mock(GitHubApiTransport.class);
    checker = Mockito.mock(ApiChecker.class);
    transport = new GitHubApiTransportWithChecker(delegate, checker);
  }

  @Test
  public void testGet() throws Exception {
    String unused = transport.get(String.class, "path/foo");
    verify(checker).check("path", "path/foo", "response_type", String.class);
    verify(delegate).get(eq("path/foo"), eq(String.class), any(), any());
  }

  @Test
  public void testGetThrowsException() throws Exception {
    doThrow(new CheckerException("Error!")).when(checker).check(any(), any(), any(), any());
    assertThrows(ValidationException.class, () -> transport.get(String.class, "path/foo"));
    verifyNoMoreInteractions(delegate);
  }

  @Test
  public void testPost() throws Exception {
    String unused = transport.post("request_content", String.class, "path/foo");
    verify(checker)
        .check(
            "path", "path/foo",
            "request", "request_content",
            "response_type", String.class);
    verify(delegate).post(eq("path/foo"), eq("request_content"), eq(String.class), any());
  }

  @Test
  public void testPostThrowsException() throws Exception {
    doThrow(new CheckerException("Error!"))
        .when(checker)
        .check(any(), any(), any(), any(), any(), any());
    assertThrows(
        ValidationException.class,
        () -> transport.post("request_content", String.class, "path/foo"));
    verifyNoMoreInteractions(delegate);
  }
}
