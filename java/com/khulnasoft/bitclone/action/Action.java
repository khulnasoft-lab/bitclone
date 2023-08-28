package com.khulnasoft.bitclone.action;

import com.khulnasoft.common.collect.ImmutableSetMultimap;
import com.khulnasoft.bitclone.SkylarkContext;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import net.starlark.java.annot.StarlarkBuiltin;
import net.starlark.java.eval.StarlarkValue;

/**
 * Actions are Starlark functions that receive a context object (that is different depending
 *  on where it is used) that expose an API to implement custom logic in Starlark. */
@StarlarkBuiltin(
    name = "dynamic.action",
    doc = "An action is an Starlark piece of code that does part of a migration. It is used"
        + "to define the logic of migration for feedback workflow, on_finish hooks, git.mirror,"
        + " etc.",
    documented = false)
public interface Action extends StarlarkValue {

  <T extends SkylarkContext<T>> void run(ActionContext<T> context)
      throws ValidationException, RepoException;

  String getName();

  /** Returns a key-value list of the options the action was instantiated with. */
  ImmutableSetMultimap<String, String> describe();
}
