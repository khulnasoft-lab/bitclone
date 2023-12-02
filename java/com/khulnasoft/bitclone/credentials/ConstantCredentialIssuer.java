package com.khulnasoft.bitclone.credentials;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;

/**
 * A static CredentialIssuer, e.g. a password, username, api key, etc
 */
public class ConstantCredentialIssuer implements CredentialIssuer {

  private final String secret;
  private final String name;

  private final boolean open;

  public static ConstantCredentialIssuer createConstantSecret(String name, String secret) {
    return new ConstantCredentialIssuer(
        Preconditions.checkNotNull(name), Preconditions.checkNotNull(secret), false);
  }

  public static ConstantCredentialIssuer createConstantOpenValue(String value) {
    return new ConstantCredentialIssuer(Preconditions.checkNotNull(value), value, true);
  }

  private ConstantCredentialIssuer(String name, String secret, boolean open) {
    this.secret = secret;
    this.name = name;
    this.open = open;
  }

  @Override
  public Credential issue() throws CredentialIssuingException {
    return open ? new OpenCredential(secret) : new StaticSecret(name, secret);
  }

  @Override
  public ImmutableSetMultimap<String, String> describe() {
    return ImmutableSetMultimap.of("type", "constant", "name", name, "open", "" + open);
  }
}
