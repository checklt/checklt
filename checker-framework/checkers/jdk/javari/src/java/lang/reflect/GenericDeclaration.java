package java.lang.reflect;
import checkers.javari.quals.*;

public @ReadOnly interface GenericDeclaration {
    public TypeVariable<?>[] getTypeParameters();
}
