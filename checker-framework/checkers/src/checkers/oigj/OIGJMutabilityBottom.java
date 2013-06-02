package checkers.oigj;

import java.lang.annotation.Target;

import com.sun.source.tree.Tree.Kind;

import checkers.oigj.quals.*;
import checkers.quals.*;
import checkers.types.AnnotatedTypeMirror.AnnotatedPrimitiveType;

/**
 * An annotation used to represent a place holder immutability type, that is a
 * subtype of all other types. For example, {@code null} type is a subtype
 * of all immutability types.
 * <p>
 *
 * This annotation may not be written in source code; it is an
 * implementation detail of the checker.
 */
@TypeQualifier
@SubtypeOf({Mutable.class, Immutable.class, I.class})
@Target({}) // empty target prevents programmers from writing this in a program
@ImplicitFor(
        trees = { Kind.NULL_LITERAL, Kind.CLASS, Kind.ENUM,
                Kind.INTERFACE, Kind.ANNOTATION_TYPE,
                Kind.NEW_ARRAY },
        typeClasses = { AnnotatedPrimitiveType.class },
        typeNames = {java.lang.Void.class}
)
@interface OIGJMutabilityBottom { }
