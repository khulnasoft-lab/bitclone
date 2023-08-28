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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface for opening a URL for downloading a file
 */
public interface HttpStreamFactory {

  /**
   * Open the referenced URL and return the stream to the contents.
   */
  InputStream open(URL url) throws IOException;
}
