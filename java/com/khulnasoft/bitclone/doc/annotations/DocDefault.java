 package com.khulnasoft.bitclone.doc.annotations;

 import java.lang.annotation.ElementType;
 import java.lang.annotation.Repeatable;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Annotation for documenting fields for a @Param or return types
  *
  * TODO(malcon): Rename this to DocParam
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD, ElementType.TYPE_USE})
 @Repeatable(DocDefaults.class)
 public @interface DocDefault {
   String field();
   // TODO(malcon): Rename to defaultValue
   String value() default "";
   String[] allowedTypes() default {};
 }