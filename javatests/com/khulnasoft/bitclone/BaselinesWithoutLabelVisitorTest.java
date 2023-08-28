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
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.khulnasoft.bitclone.ChangeVisitable.VisitResult;
import com.khulnasoft.bitclone.authoring.Author;
import com.khulnasoft.bitclone.revision.Change;
import com.khulnasoft.bitclone.testing.DummyRevision;
import com.khulnasoft.bitclone.util.Glob;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BaselinesWithoutLabelVisitorTest {

  @Test
  public void testResults() {
    BaselinesWithoutLabelVisitor<DummyRevision> visitor =
        new BaselinesWithoutLabelVisitor<>(
            Glob.createGlob(ImmutableList.of("foo/**"), ImmutableList.of("foo/bar")),
            10,
            Optional.empty(),
            false);

    visit(visitor, "one", ImmutableSet.of("foo/aa"));
    visit(visitor, "two", ImmutableSet.of("excluded/aaa"));
    visit(visitor, "three", ImmutableSet.of("foo/bar"));

    assertThat(
            visitor.getResult().stream().map(DummyRevision::asString).collect(Collectors.toList()))
        .containsExactly("one", "three")
        .inOrder();
  }

  @Test
  public void testSkipStart() {
    BaselinesWithoutLabelVisitor<DummyRevision> visitor =
        new BaselinesWithoutLabelVisitor<>(
            Glob.createGlob(ImmutableList.of("foo/**"), ImmutableList.of("foo/bar")),
            10,
            /* toSkip= */ Optional.of(new DummyRevision("one")),
            false);

    visit(visitor, "one", ImmutableSet.of("foo/aa"));
    visit(visitor, "two", ImmutableSet.of("excluded/aaa"));
    visit(visitor, "three", ImmutableSet.of("foo/bar"));

    assertThat(
            visitor.getResult().stream().map(DummyRevision::asString).collect(Collectors.toList()))
        .containsExactly("three");
  }

  @Test
  public void testSkipFirst() {
    BaselinesWithoutLabelVisitor<DummyRevision> visitor =
        new BaselinesWithoutLabelVisitor<>(
            Glob.createGlob(ImmutableList.of("foo/**"), ImmutableList.of("foo/bar")),
            10,
            /* toSkip= */ Optional.empty(),
            true);

    visit(visitor, "one", ImmutableSet.of("foo/aa"));
    visit(visitor, "two", ImmutableSet.of("excluded/aaa"));
    visit(visitor, "three", ImmutableSet.of("foo/bar"));

    assertThat(visitor.getResult().stream().map(DummyRevision::asString)).containsExactly("three");
  }

  @Test
  public void testLimit() {
    BaselinesWithoutLabelVisitor<DummyRevision> visitor =
        new BaselinesWithoutLabelVisitor<>(
            Glob.createGlob(ImmutableList.of("foo/**"), ImmutableList.of("foo/bar")),
            3,
            Optional.empty(),
            false);

    visit(visitor, "one", ImmutableSet.of("foo/aa"));
    visit(visitor, "two", ImmutableSet.of("excluded/aaa"));
    visit(visitor, "three", ImmutableSet.of("foo/aa"));
    assertThat(visit(visitor, "four", ImmutableSet.of("foo/aa")))
        .isEqualTo(VisitResult.TERMINATE);

    assertThat(
            visitor.getResult().stream().map(DummyRevision::asString).collect(Collectors.toList()))
        .containsExactly("one", "three", "four")
        .inOrder();
  }

  private VisitResult visit(BaselinesWithoutLabelVisitor<DummyRevision> visitor, String rev,
      @Nullable ImmutableSet<String> files) {

    DummyRevision dummyRevision = new DummyRevision(rev);
    return visitor.visit(new Change<>(
        dummyRevision,
        new Author("a", "foo@example.com"),
        dummyRevision.getMessage(),
        ZonedDateTime.now(ZoneId.systemDefault()),
        dummyRevision.getLabels(),
        files));
  }
}
