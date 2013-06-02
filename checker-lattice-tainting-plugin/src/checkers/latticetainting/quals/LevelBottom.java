package checkers.latticetainting.quals;

import checkers.quals.InvisibleQualifier;
import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

import java.lang.annotation.Target;


@TypeQualifier
@InvisibleQualifier
//@ImplicitFor(trees = {Tree.Kind.NULL_LITERAL},
//  typeNames = {java.lang.Void.class})
@SubtypeOf({Level.class})
@Target({}) // empty target prevents programmers from writing this in a program
public @interface LevelBottom {
}
