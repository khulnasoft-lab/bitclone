 package com.khulnasoft.bitclone.doc.annotations;

 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Adds a custom prefix to the signature example and reference in the generated Markdown
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.TYPE})
 public @interface DocSignaturePrefix {
 
   /**
    * When generating documentation use varPrefix + "." + method/field for generating the docs.
    * For example "ctx.origin"
    */
   String value();
 }