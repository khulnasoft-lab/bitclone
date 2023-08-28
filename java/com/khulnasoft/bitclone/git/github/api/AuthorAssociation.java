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

package com.khulnasoft.bitclone.git.github.api;

/**
 * GitHub type of author for pull request, reviews, etc.
 *
 * <p>See https://developer.github.com/v4/reference/enum/commentauthorassociation/
 */
public enum AuthorAssociation {
  CONTRIBUTOR,
  COLLABORATOR,
  MEMBER,
  OWNER,
  NONE,
  FIRST_TIMER,
  FIRST_TIME_CONTRIBUTOR,
}
