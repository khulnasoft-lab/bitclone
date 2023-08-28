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

package com.khulnasoft.bitclone.transform.metadata;

import com.google.common.base.Preconditions;
import com.khulnasoft.bitclone.TransformWork;
import com.khulnasoft.bitclone.Transformation;
import com.khulnasoft.bitclone.TransformationStatus;
import com.khulnasoft.bitclone.exception.ValidationException;
import com.khulnasoft.bitclone.transform.ExplicitReversal;
import com.khulnasoft.bitclone.transform.IntentionalNoop;
import java.io.IOException;
import net.starlark.java.syntax.Location;

public class RemoveLabelInMessage implements Transformation {

  private final String label;
  private final Location location;

  RemoveLabelInMessage(String label, Location location) {
    this.label = Preconditions.checkNotNull(label);
    this.location = Preconditions.checkNotNull(location);
  }

  @Override
  public TransformationStatus transform(TransformWork work)
      throws IOException, ValidationException {
    String message = work.getMessage();
    work.removeLabel(label, /*wholeMessage*/ false);
    // Lets try to find the message all the text
    if (work.getMessage().equals(message)) {
      work.removeLabel(label, /*wholeMessage*/ true);
    }
    return TransformationStatus.success();
  }

  @Override
  public Transformation reverse() {
    return new ExplicitReversal(IntentionalNoop.INSTANCE, this);
  }

  @Override
  public String describe() {
    return "Removing label " + label;
  }

  @Override
  public Location location() {
    return location;
  }
}
