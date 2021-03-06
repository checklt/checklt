package polyall.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import checkers.quals.PolymorphicQualifier;
import checkers.quals.TypeQualifier;

@TypeQualifier
@PolymorphicQualifier(H2Top.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface H2Poly {}
