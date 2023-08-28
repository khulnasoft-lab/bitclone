 package com.khulnasoft.bitclone.doc.annotations;

 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Annotation to associate flags to functions in SkylarkModules.
  *
  * <p>Can be set to a whole module or specific field functions.
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.TYPE,
     ElementType.FIELD, // TODO(malcon): Remove this once everything migrated to @StarlarkMethod
     ElementType.METHOD})
 public @interface UsesFlags {
 
   /**
    * An associated flags class annotated with {@code Parameter}
    */
   // TODO(copybara-team): change to <? extends Option>.
   Class<?>[] value();
 }