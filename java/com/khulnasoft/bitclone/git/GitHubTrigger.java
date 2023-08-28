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

package com.khulnasoft.bitclone.git;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.khulnasoft.bitclone.Endpoint;
import com.khulnasoft.bitclone.LazyResourceLoader;
import com.khulnasoft.bitclone.Trigger;
import com.khulnasoft.bitclone.git.github.api.GitHubApi;
import com.khulnasoft.bitclone.git.github.util.GitHubHost;
import com.khulnasoft.bitclone.util.console.Console;
import java.util.stream.Collectors;

/**
 * A feedback trigger based on updates on a GitHub PR.
 */
public class GitHubTrigger implements Trigger {

  private final LazyResourceLoader<GitHubApi> apiSupplier;
  private final String url;
  private GitHubHost ghHost;
  private final ImmutableSet<EventTrigger> events;
  private final Console console;

  GitHubTrigger(
      LazyResourceLoader<GitHubApi> apiSupplier,
      String url,
      ImmutableSet<EventTrigger> events,
      Console console,
      GitHubHost ghHost) {
    this.apiSupplier = Preconditions.checkNotNull(apiSupplier);
    this.url = Preconditions.checkNotNull(url);
    this.ghHost = Preconditions.checkNotNull(ghHost);
    Preconditions.checkArgument(!events.isEmpty());
    this.events = events;
    this.console = Preconditions.checkNotNull(console);
  }

  @Override
  public Endpoint getEndpoint() {
    return new GitHubEndPoint(apiSupplier, url, console, ghHost);
  }

  @Override
  public ImmutableSetMultimap<String, String> describe() {
    ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
    builder.put("type", "github_trigger");
    builder.put("url", url);
    builder.putAll("events",
        events.stream().map(s -> s.type().name()).collect(Collectors.toList()));
    for (EventTrigger trigger : events) {
      if (trigger.subtypes().isEmpty()) {
        continue;
      }
      builder.putAll(
          String.format("SUBTYPES_%s", trigger.type().name()),
          trigger.subtypes());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("url", url)
        .add("event_types", Joiner.on(",").join(events))
        .toString();
  }

}
