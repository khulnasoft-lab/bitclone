package com.khulnasoft.bitclone.credentials;

import com.google.common.collect.ImmutableSetMultimap;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.starlark.java.eval.StarlarkValue;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

/** Fetches a value located within a toml file. */
public class TomlKeySource implements CredentialIssuer, StarlarkValue {

  Path file;
  String dotPath;

  public TomlKeySource(Path file, String keyPath) {
    this.file = file;
    this.dotPath = keyPath;
  }

  @Override
  public Credential issue() throws CredentialIssuingException {
    TomlParseResult tomlParseResult = null;
    try {
      tomlParseResult = Toml.parse(file);
    } catch (IOException e) {
      throw new CredentialIssuingException("Error reading Toml file.", e);
    }
    @Nullable String data = tomlParseResult.getString(dotPath);
    if (data == null) {
      throw new CredentialIssuingException(
          String.format("key %s not found in file %s", dotPath, file));
    }
    return new StaticSecret(dotPath, data);
  }

  @Override
  public ImmutableSetMultimap<String, String> describe() {
    return ImmutableSetMultimap.of("type", "Toml", "dotPath", dotPath);
  }
}
