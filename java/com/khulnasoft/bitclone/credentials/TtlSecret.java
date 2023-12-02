package com.khulnasoft.bitclone.credentials;

import com.khulnasoft.common.base.Preconditions;
import java.time.Clock;
import java.time.Instant;

/**
 * A credential with a limited TTL
 */
public class TtlSecret extends StaticSecret {
  private final Instant ttl;
  private final Clock clock;

  public TtlSecret(String secret, String name, Instant ttl, Clock clock) {
    super(name, secret);
    this.ttl = Preconditions.checkNotNull(ttl);
    this.clock = Preconditions.checkNotNull(clock);
  }

  @Override
  public String printableValue() {
    return String.format("<static secret name %s with expiration %s>", name, ttl);
  }

  @Override
  public String provideSecret() throws CredentialRetrievalException {
    if (ttl.isBefore(clock.instant())) {
      throw new CredentialRetrievalException(
          String.format("Credential %s is expired.", printableValue()));
    }
    return super.provideSecret();
  }

  @Override
  public boolean valid() {
    return ttl.isBefore(clock.instant().minusSeconds(/* 10s grace */ 10));
  }

  @Override
  public String toString() {
    return printableValue();
  }
}
