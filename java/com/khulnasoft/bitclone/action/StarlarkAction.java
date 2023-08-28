package com.khulnasoft.bitclone.action;

import com.khulnasoft.common.base.MoreObjects;
import com.khulnasoft.common.base.Preconditions;
import com.khulnasoft.common.collect.ImmutableList;
import com.khulnasoft.common.collect.ImmutableMap;
import com.khulnasoft.common.collect.ImmutableSetMultimap;
import com.khulnasoft.bitclone.SkylarkContext;
import com.khulnasoft.bitclone.exception.RepoException;
import com.khulnasoft.bitclone.exception.ValidationException;
import net.starlark.java.eval.Dict;
import net.starlark.java.eval.EvalException;
import net.starlark.java.eval.Mutability;
import net.starlark.java.eval.Starlark;
import net.starlark.java.eval.StarlarkCallable;
import net.starlark.java.eval.StarlarkSemantics;
import net.starlark.java.eval.StarlarkThread;
import net.starlark.java.eval.StarlarkThread.PrintHandler;

/**
 * An implementation of {@link Action} that delegates to a Starlark function.
 */
public class StarlarkAction implements Action {

  private final String name;
  private final StarlarkCallable function;
  private final Dict<?, ?> params;
  private final StarlarkThread.PrintHandler printHandler;

  public StarlarkAction(
      String name, StarlarkCallable function, Dict<?, ?> params,
      PrintHandler printHandler) {
    this.name = name;
    this.function = Preconditions.checkNotNull(function);
    this.params = Preconditions.checkNotNull(params);
    this.printHandler = Preconditions.checkNotNull(printHandler);
  }

  @Override
   public <T extends SkylarkContext<T>> void run(ActionContext<T> context)
      throws ValidationException, RepoException {
    SkylarkContext<T> actionContext = context.withParams(params);
    try (Mutability mu = Mutability.create("dynamic_action")) {
      StarlarkThread thread = new StarlarkThread(mu, StarlarkSemantics.DEFAULT);
      thread.setPrintHandler(printHandler);
      Object result =
          Starlark.call(
              thread, function, ImmutableList.of(actionContext), /*kwargs=*/ ImmutableMap.of());
      context.onFinish(result, actionContext);
    } catch (EvalException e) {
      Throwable cause = e.getCause();
      String error =
          String.format(
              "Error while executing the skylark transformation %s: %s.",
              function.getName(), e.getMessageWithStack());
      if (cause instanceof RepoException) {
        throw new RepoException(error, cause);
      }
      throw new ValidationException(error, cause);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("This should not happen.", e);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ImmutableSetMultimap<String, String> describe() {
    ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
    for (Object paramKey : params.keySet()) {
      builder.put(paramKey.toString(), params.get(paramKey).toString());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", function.getName())
        .toString();

  }
}
