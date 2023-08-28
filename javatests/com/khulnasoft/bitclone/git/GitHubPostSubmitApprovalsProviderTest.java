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

package com.khulnasoft.bitclone.git;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.khulnasoft.bitclone.approval.ApprovalsProvider;
import com.khulnasoft.bitclone.approval.ApprovalsProvider.ApprovalsResult;
import com.khulnasoft.bitclone.approval.ChangeWithApprovals;
import com.khulnasoft.bitclone.approval.StatementPredicate;
import com.khulnasoft.bitclone.approval.UserPredicate;
import com.khulnasoft.bitclone.authoring.Author;
import com.khulnasoft.bitclone.git.github.api.GitHubGraphQLApi.GetCommitHistoryParams;
import com.khulnasoft.bitclone.git.github.util.GitHubHost;
import com.khulnasoft.bitclone.revision.Change;
import com.khulnasoft.bitclone.testing.OptionsBuilder;
import com.khulnasoft.bitclone.testing.git.GitTestUtil;
import com.khulnasoft.bitclone.util.console.testing.TestingConsole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class GitHubPostSubmitApprovalsProviderTest {
  private OptionsBuilder builder;
  private GitTestUtil gitTestUtil;
  private TestingConsole console;
  private GitRepository gitRepository;
  private GitHubHost githubHost;
  private GetCommitHistoryParams params;
  private static final String TRUSTED_TEST_PROJECT = "khulnasoft.bitclone";
  private static final String PROJECT_URL = "https://github.com/khulnasoft.bitclone";

  @Before
  public void setUp() throws Exception {
    params = new GetCommitHistoryParams(5, 5, 5);
    githubHost = new GitHubHost("github.com");
    console = new TestingConsole();
    builder =
        new OptionsBuilder()
            .setOutputRootToTmpDir()
            .setWorkdirToRealTempDir()
            .setConsole(console)
            .setEnvironment(GitTestUtil.getGitEnv().getEnvironment());

    Path credentialsFile = Files.createTempFile("credentials", "test");
    Files.writeString(credentialsFile, "https://user:SECRET@github.com");
    builder.git.credentialHelperStorePath = credentialsFile.toString();
    Path repoGitDir = Files.createTempDirectory("githubPostSubmitApprovalProvider-repoGitDir");
    gitRepository = GitRepository.newRepo(true, repoGitDir, GitTestUtil.getGitEnv()).init();
    gitTestUtil = new GitTestUtil(builder);
    gitTestUtil.mockRemoteGitRepos();
  }

  public ApprovalsProvider getApprovalProviderUnderTest(GitHubOptions gitHubOptions)
      throws Exception {
    return new GitHubPostSubmitApprovalsProvider(
        githubHost,
        /* branch= */ "main",
        new GitHubSecuritySettingsValidator(
            gitHubOptions.newGitHubApiSupplier(PROJECT_URL, null, githubHost),
            ImmutableList.copyOf(gitHubOptions.allStarAppIds),
            console),
        new GitHubUserApprovalsValidator(
            gitHubOptions.newGitHubGraphQLApiSupplier(PROJECT_URL, null, githubHost),
            console,
            githubHost,
            params));
  }

  private ImmutableList<ChangeWithApprovals> generateChangeList(
      GitRepository gitRepository, String project, String... shas) throws Exception {
    ImmutableList.Builder<ChangeWithApprovals> changes = ImmutableList.builder();
    for (String sha : shas) {
      GitRevision revision =
          new GitRevision(
              gitRepository,
              sha,
              "reviewRef_unused",
              "main",
              ImmutableListMultimap.of(),
              String.format("https://github.com/%s", project));
      Change<GitRevision> change =
          new Change<>(
              revision,
              new Author("bitcloneuser", "bitcloneuser@google.com"),
              "placeholder message",
              ZonedDateTime.now(ZoneId.of("America/Los_Angeles")),
              ImmutableListMultimap.of());
      ChangeWithApprovals changeWithApprovals = new ChangeWithApprovals(change);
      changes.add(changeWithApprovals);
    }
    return changes.build();
  }

  @Test
  public void testGitHubPostSubmitApprovalsProvider_withEmptyChangeList() throws Exception {
    ApprovalsProvider underTest = getApprovalProviderUnderTest(builder.github);
    ImmutableList<ChangeWithApprovals> changes = ImmutableList.of();
    ApprovalsResult approvalsResult = underTest.computeApprovals(changes, console);
    assertThat(approvalsResult.getChanges()).isEmpty();
  }

  @Test
  public void testGitHubPostSubmitApprovalsProvider_withFullyCompliantChangeList()
      throws Exception {
    gitTestUtil.mockApi(
        eq("GET"),
        eq("https://api.github.com/orgs/google/installations?per_page=100"),
        GitTestUtil.mockResponse("{\"installations\":[{\"app_id\": 119816}]}"));
    gitTestUtil.mockApi(
        eq("GET"),
        eq("https://api.github.com/orgs/google"),
        GitTestUtil.mockResponse("{\"two_factor_requirement_enabled\":true}"));
    gitTestUtil.mockApi(
        eq("POST"),
        eq("https://api.github.com/graphql"),
        GitTestUtil.mockResponse(
            "{"
                + "\"data\": {"
                + "\"repository\": {"
                + "\"ref\": {"
                + "\"target\": {"
                + "\"id\": \"C_notreadatall\","
                + "\"history\": {"
                + "\"nodes\": ["
                + "{"
                + "\"id\": \"C_notreadatall\","
                + "\"oid\": \"3368ee55bcad7df67a18b588144e0888d6fa93ac\","
                + "\"associatedPullRequests\": {"
                + "\"edges\": ["
                + "{"
                + "\"node\": {"
                + "\"title\": \"title place holder\","
                + "\"author\": {"
                + "\"login\": \"bitcloneauthor\""
                + "},"
                + "\"reviewDecision\": \"APPROVED\","
                + "\"latestOpinionatedReviews\": {"
                + "\"edges\": ["
                + "{"
                + "\"node\": {"
                + "\"author\": {"
                + "\"login\": \"bitclonereviewer\""
                + "},"
                + "\"state\": \"APPROVED\""
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"));
    ApprovalsProvider underTest = getApprovalProviderUnderTest(builder.github);
    ImmutableList<ChangeWithApprovals> changes =
        generateChangeList(
            gitRepository, "khulnasoft.bitclone", "3368ee55bcad7df67a18b588144e0888d6fa93ac");
    ApprovalsResult approvalsResult = underTest.computeApprovals(changes, console);
    assertThat(approvalsResult.getChanges()).isNotEmpty();
    assertThat(Iterables.getOnlyElement(approvalsResult.getChanges()).getPredicates())
        .containsExactly(
            new StatementPredicate(
                GitHubSecuritySettingsValidator.TWO_FACTOR_PREDICATE_TYPE,
                "Whether the organization that the change originated from has two factor"
                    + " authentication requirement enabled.",
                Iterables.getLast(changes).getChange().getRevision().getUrl()),
            new StatementPredicate(
                GitHubSecuritySettingsValidator.ALL_STAR_PREDICATE_TYPE,
                "Whether the organization that the change originated from has allstar"
                    + " installed",
                Iterables.getLast(changes).getChange().getRevision().getUrl()),
            new UserPredicate(
                "bitcloneauthor",
                UserPredicate.UserPredicateType.OWNER,
                Iterables.getLast(changes).getChange().getRevision().getUrl(),
                "GitHub user 'bitcloneauthor' authored change with sha"
                    + " '3368ee55bcad7df67a18b588144e0888d6fa93ac'."),
            new UserPredicate(
                "bitclonereviewer",
                UserPredicate.UserPredicateType.LGTM,
                Iterables.getLast(changes).getChange().getRevision().getUrl(),
                "GitHub user 'bitclonereviewer' approved change with sha"
                    + " '3368ee55bcad7df67a18b588144e0888d6fa93ac'."));
  }

  @Test
  public void testGitHubPostSubmitApprovalsProvider_withCompliantOrgSettingsButNoApprovals()
      throws Exception {
    gitTestUtil.mockApi(
        eq("GET"),
        eq("https://api.github.com/orgs/google/installations?per_page=100"),
        GitTestUtil.mockResponse("{\"installations\":[{\"app_id\": 119816}]}"));
    gitTestUtil.mockApi(
        eq("GET"),
        eq("https://api.github.com/orgs/google"),
        GitTestUtil.mockResponse("{\"two_factor_requirement_enabled\":true}"));
    gitTestUtil.mockApi(
        eq("POST"),
        eq("https://api.github.com/graphql"),
        GitTestUtil.mockResponse(
            "{"
                + "\"data\": {"
                + "\"repository\": {"
                + "\"ref\": {"
                + "\"target\": {"
                + "\"id\": \"C_notreadatall\","
                + "\"history\": {"
                + "\"nodes\": ["
                + "{"
                + "\"id\": \"C_notreadatall\","
                + "\"oid\": \"3368ee55bcad7df67a18b588144e0888d6fa93ac\","
                + "\"associatedPullRequests\": {"
                + "\"edges\": ["
                + "{"
                + "\"node\": {"
                + "\"title\": \"title place holder\","
                + "\"author\": {"
                + "\"login\": \"bitcloneauthor\""
                + "},"
                + "\"reviewDecision\": \"APPROVED\","
                + "\"latestOpinionatedReviews\": {"
                + "\"edges\": ["
                + "{"
                + "\"node\": {"
                + "\"author\": {"
                + "\"login\": \"bitclonereviewer\""
                + "},"
                + "\"state\": \"CHANGES_REQUESTED\""
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"));
    ApprovalsProvider underTest = getApprovalProviderUnderTest(builder.github);
    ImmutableList<ChangeWithApprovals> changes =
        generateChangeList(
            gitRepository, "khulnasoft.bitclone", "3368ee55bcad7df67a18b588144e0888d6fa93ac");
    ApprovalsResult approvalsResult = underTest.computeApprovals(changes, console);
    assertThat(approvalsResult.getChanges()).isNotEmpty();
    assertThat(Iterables.getOnlyElement(approvalsResult.getChanges()).getPredicates())
        .containsExactly(
            new UserPredicate(
                "bitcloneauthor",
                UserPredicate.UserPredicateType.OWNER,
                Iterables.getLast(changes).getChange().getRevision().getUrl(),
                "GitHub user 'bitcloneauthor' authored change with sha"
                    + " '3368ee55bcad7df67a18b588144e0888d6fa93ac'."),
            new StatementPredicate(
                GitHubSecuritySettingsValidator.TWO_FACTOR_PREDICATE_TYPE,
                "Whether the organization that the change originated from has two factor"
                    + " authentication requirement enabled.",
                Iterables.getLast(changes).getChange().getRevision().getUrl()),
            new StatementPredicate(
                GitHubSecuritySettingsValidator.ALL_STAR_PREDICATE_TYPE,
                "Whether the organization that the change originated from has allstar"
                    + " installed",
                Iterables.getLast(changes).getChange().getRevision().getUrl()));
  }

  @Test
  public void testGitHubPostSubmitApprovalsProvider_withUnTrustWorthyOrgAndRepoSettings()
      throws Exception {
    gitTestUtil.mockApi(
        "GET",
        "https://api.github.com/orgs/google/installations?per_page=100",
        GitTestUtil.mockResponse("{\"installations\":[{\"app_id\": -1}]}"));
    gitTestUtil.mockApi(
        "GET",
        "https://api.github.com/orgs/google",
        GitTestUtil.mockResponse("{\"two_factor_requirement_enabled\":false}"));
    gitTestUtil.mockApi(
        eq("POST"),
        eq("https://api.github.com/graphql"),
        GitTestUtil.mockResponse(
            "{"
                + "\"data\": {"
                + "\"repository\": {"
                + "\"ref\": {"
                + "\"target\": {"
                + "\"id\": \"C_notreadatall\","
                + "\"history\": {"
                + "\"nodes\": ["
                + "{"
                + "\"id\": \"C_notreadatall\","
                + "\"oid\": \"3368ee55bcad7df67a18b588144e0888d6fa93ac\","
                + "\"associatedPullRequests\": {"
                + "\"edges\": ["
                + "{"
                + "\"node\": {"
                + "\"title\": \"title place holder\","
                + "\"author\": {"
                + "\"login\": \"bitcloneauthor\""
                + "},"
                + "\"reviewDecision\": \"APPROVED\","
                + "\"latestOpinionatedReviews\": {"
                + "\"edges\": ["
                + "{"
                + "\"node\": {"
                + "\"author\": {"
                + "\"login\": \"bitclonereviewer\""
                + "},"
                + "\"state\": \"APPROVED\""
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "]"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"));
    ApprovalsProvider underTest = getApprovalProviderUnderTest(builder.github);
    ImmutableList<ChangeWithApprovals> changes =
        generateChangeList(
            gitRepository, "khulnasoft.bitclone", "3368ee55bcad7df67a18b588144e0888d6fa93ac");
    ApprovalsResult approvalsResult = underTest.computeApprovals(changes, console);
    assertThat(approvalsResult.getChanges().size()).isEqualTo(changes.size());
    assertThat(Iterables.getOnlyElement(approvalsResult.getChanges()).getPredicates())
        .containsExactly(
            new UserPredicate(
                "bitcloneauthor",
                UserPredicate.UserPredicateType.OWNER,
                Iterables.getLast(changes).getChange().getRevision().getUrl(),
                "GitHub user 'bitcloneauthor' authored change with sha"
                    + " '3368ee55bcad7df67a18b588144e0888d6fa93ac'."),
            new UserPredicate(
                "bitclonereviewer",
                UserPredicate.UserPredicateType.LGTM,
                Iterables.getLast(changes).getChange().getRevision().getUrl(),
                "GitHub user 'bitclonereviewer' approved change with sha"
                    + " '3368ee55bcad7df67a18b588144e0888d6fa93ac'."));
  }
}
