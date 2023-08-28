package com.khulnasoft.bitclone.doc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A collection of {@link DocDefault} for a @StarlarkMethod function.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DocDefaults {

  DocDefault[] value();
}