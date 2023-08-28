package com.khulnasoft.bitclone.action;

import static com.khulnasoft.bitclone.action.ActionResult.Result.ERROR;
import static com.khulnasoft.bitclone.action.ActionResult.Result.NO_OP;
import static com.khulnasoft.bitclone.action.ActionResult.Result.SUCCESS;

import com.khulnasoft.common.base.MoreObjects;
import javax.annotation.Nullable;
import net.starlark.java.annot.StarlarkBuiltin;
import net.starlark.java.annot.StarlarkMethod;
import net.starlark.java.eval.Printer;
import net.starlark.java.eval.StarlarkValue;

/** Represents the result returned by an {@link Action}. */
@SuppressWarnings("unused")
@StarlarkBuiltin(
    name = "dynamic.action_result",
    doc = "Result objects created by actions to tell Bitclone what happened.")
public class ActionResult implements StarlarkValue {

  private final Result result;
  @Nullable private final String msg;

  private ActionResult(Result result, @Nullable String msg) {
    this.result = result;
    this.msg = msg;
  }

  public enum Result {
    SUCCESS,
    ERROR,
    NO_OP
  }

  public static ActionResult success() {
    return new ActionResult(SUCCESS, /*msg*/ null);
  }

  public static ActionResult error(String msg) {
    return new ActionResult(ERROR, msg);
  }

  public static ActionResult noop(@Nullable String msg) {
    return new ActionResult(NO_OP, msg);
  }

  public Result getResult() {
    return result;
  }

  @StarlarkMethod(
      name = "result",
      doc = "The result of this action",
      structField = true
  )
  public String getResultForSkylark() {
    return result.name();
  }

  @StarlarkMethod(
      name = "msg",
      doc = "The message associated with the result",
      structField = true,
      allowReturnNones = true)
  @Nullable
  public String getMsg() {
    return msg;
  }

  @Override
  public void repr(Printer printer) {
    printer.append(toString());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("result", result)
        .add("msg", msg)
        .toString();
  }
}
