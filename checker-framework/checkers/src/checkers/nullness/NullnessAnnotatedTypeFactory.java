package checkers.nullness;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import checkers.basetype.BaseTypeChecker;
import checkers.flow.Flow;
import checkers.nullness.quals.*;
import checkers.quals.DefaultLocation;
import checkers.quals.DefaultQualifier;
import checkers.quals.PolyAll;
import checkers.quals.Unused;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.types.GeneralAnnotatedTypeFactory;
import checkers.types.TreeAnnotator;
import checkers.types.TypeAnnotator;
import checkers.types.visitors.AnnotatedTypeScanner;
import checkers.util.AnnotationUtils;
import checkers.util.DependentTypes;
import checkers.util.ElementUtils;
import checkers.util.InternalUtils;
import checkers.util.Pair;
import checkers.util.TreeUtils;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Attribute.TypeCompound;

/**
 * Adds the {@link NonNull} annotation to a type that is:
 *
 * <ol>
 * <li value="1">(*) all literals (except for {@code null} literal)
 * <li value="2">(*) all primitive types
 * <li value="3">(*) an array-creation expression (with new)
 * <li value="4">(*) an object-creation expression (with new)
 * <li value="5">(*) a package declaration
 * <li value="6">in the scope of a {@link DefaultQualifier} annotation and
 *  matches its location criteria
 * <li value="7">determined to be {@link NonNull} by flow-sensitive inference
 * <li value="8">the class in a static member access (e.g., "System" in "System.out")
 * <li value="9">an exception parameter
 * <li value="10">the receiver type of a non-static (and non-constructor) method
 * </ol>
 *
 * Adds the {@link Raw} annotation to
 * <ol>
 * <li value="11">the receiver type of constructors.
 * </ol>
 *
 * <p>
 *
 * Additionally, the type factory will add the {@link Nullable} annotation to a
 * type if the input is
 * <ol>
 * <li value="12">(*) the null literal
 * <li value="13">of type {@link Void},
 * <li value="14">may resolve the types of some {@link NonNull} fields as
 *  {@link Nullable} depending on the presence of a {@link Raw} annotation on
 *  a constructor or method receiver.  Please review
 *  <a href="http://homes.cs.washington.edu/~mernst/pubs/pluggable-checkers-issta2008-abstract.html">
 *  the Checker Framework</a> for {@link Raw} semantics.
 * </ol>
 *
 * Implementation detail:  (*) cases are handled by a meta-annotation
 * rather than by code in this class.
 */
public class NullnessAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<NullnessSubchecker> {

    private final DependentTypes dependentTypes;

    /*package*/ final AnnotatedTypeFactory rawnessFactory;

    /** Factory for arbitrary qualifiers, used for declarations and "unused" qualifier. */
    private final GeneralAnnotatedTypeFactory generalFactory;

    private final AnnotationCompleter completer = new AnnotationCompleter();

    /** Represents the Nullness Checker qualifiers */
    protected final AnnotationMirror NONNULL, NULLABLE, LAZYNONNULL, RAW,
            PRIMITIVE, POLYNULL, POLYALL, UNUSED;

    private final MapGetHeuristics mapGetHeuristics;
    private final SystemGetPropertyHandler systemGetPropertyHandler;
    private final CollectionToArrayHeuristics collectionToArrayHeuristics;

    /** Creates a {@link NullnessAnnotatedTypeFactory}. */
    public NullnessAnnotatedTypeFactory(NullnessSubchecker checker,
            RawnessSubchecker rawnesschecker,
            CompilationUnitTree root) {
        super(checker, root);

        generalFactory = new GeneralAnnotatedTypeFactory(checker, root);

        mapGetHeuristics = new MapGetHeuristics(processingEnv, this, generalFactory);
        systemGetPropertyHandler = new SystemGetPropertyHandler(processingEnv, this);

        NONNULL = checker.NONNULL;
        NULLABLE = checker.NULLABLE;
        LAZYNONNULL = checker.LAZYNONNULL;
        RAW = AnnotationUtils.fromClass(elements, Raw.class);
        PRIMITIVE = checker.PRIMITIVE;
        POLYNULL = checker.POLYNULL;
        POLYALL = AnnotationUtils.fromClass(elements, PolyAll.class);
        UNUSED = AnnotationUtils.fromClass(elements, Unused.class);

        // If you update the following, also update ../../../manual/nullness-checker.tex .
        // aliases for nonnull
        addAliasedAnnotation(com.sun.istack.NotNull.class, NONNULL);
        addAliasedAnnotation(edu.umd.cs.findbugs.annotations.NonNull.class, NONNULL);
        addAliasedAnnotation(javax.annotation.Nonnull.class, NONNULL);
        addAliasedAnnotation(javax.validation.constraints.NotNull.class, NONNULL);
        addAliasedAnnotation(org.eclipse.jdt.annotation.NonNull.class, NONNULL);
        addAliasedAnnotation(org.jetbrains.annotations.NotNull.class, NONNULL);
        addAliasedAnnotation(org.netbeans.api.annotations.common.NonNull.class, NONNULL);
        addAliasedAnnotation(org.jmlspecs.annotation.NonNull.class, NONNULL);

        // aliases for nullable
        addAliasedAnnotation(com.sun.istack.Nullable.class, NULLABLE);
        addAliasedAnnotation(edu.umd.cs.findbugs.annotations.CheckForNull.class, NULLABLE);
        addAliasedAnnotation(edu.umd.cs.findbugs.annotations.Nullable.class, NULLABLE);
        addAliasedAnnotation(edu.umd.cs.findbugs.annotations.UnknownNullness.class, NULLABLE);
        addAliasedAnnotation(javax.annotation.CheckForNull.class, NULLABLE);
        addAliasedAnnotation(javax.annotation.Nullable.class, NULLABLE);
        addAliasedAnnotation(org.eclipse.jdt.annotation.Nullable.class, NULLABLE);
        addAliasedAnnotation(org.jetbrains.annotations.Nullable.class, NULLABLE);
        addAliasedAnnotation(org.netbeans.api.annotations.common.CheckForNull.class, NULLABLE);
        addAliasedAnnotation(org.netbeans.api.annotations.common.NullAllowed.class, NULLABLE);
        addAliasedAnnotation(org.netbeans.api.annotations.common.NullUnknown.class, NULLABLE);
        addAliasedAnnotation(org.jmlspecs.annotation.Nullable.class, NULLABLE);

        addAliasedDeclAnnotation(checkers.nullness.quals.Pure.class, org.jmlspecs.annotation.Pure.class,
                AnnotationUtils.fromClass(elements, checkers.nullness.quals.Pure.class));

        defaults.addAbsoluteDefault(NULLABLE, DefaultLocation.LOCALS);
        defaults.addAbsoluteDefault(NONNULL, DefaultLocation.OTHERWISE);

        this.dependentTypes = new DependentTypes(checker, root);

        rawnessFactory = rawnesschecker.createFactory(root);

        // do this last, as it might use the factory again.
        this.collectionToArrayHeuristics = new CollectionToArrayHeuristics(processingEnv, this);

        this.postInit();
    }

    @Override
    protected Flow createFlow(NullnessSubchecker checker, CompilationUnitTree root,
            Set<AnnotationMirror> flowQuals) {
        return new NullnessFlow(checker, root, flowQuals, this);
    }

    @Override
    protected Set<AnnotationMirror> createFlowQualifiers(NullnessSubchecker checker) {
        Set<AnnotationMirror> flowQuals = AnnotationUtils.createAnnotationSet();
        flowQuals.add(NONNULL);
        flowQuals.add(PRIMITIVE);
        return flowQuals;
    }

    @Override
    protected TreeAnnotator createTreeAnnotator(NullnessSubchecker checker) {
        return new NonNullTreeAnnotator(checker);
    }

    @Override
    protected TypeAnnotator createTypeAnnotator(NullnessSubchecker checker) {
        return new NonNullTypeAnnotator(checker);
    }

    @Override
    public void annotateImplicit(Element elt, AnnotatedTypeMirror type) {
        typeAnnotator.visit(type);

        // case 6: apply default
        defaults.annotate(elt, type);

        completer.visit(type);
    }

    @Override
    public void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        treeAnnotator.visit(tree, type);
        typeAnnotator.visit(type);
        // case 6: apply default
        defaults.annotate(tree, type);

        if (TreeUtils.isExpressionTree(tree)) {
            substituteRaw((ExpressionTree)tree, type);
        }
        substituteUnused(tree, type);

        if (useFlow) {
            final Set<AnnotationMirror> inferred = flow.test(tree);
            if (inferred != null) {
                // case 7: flow analysis
                type.replaceAnnotations(inferred);
            }
        }

        dependentTypes.handle(tree, type);
        completer.visit(type);
    }

    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        // 'this' should always be nonnull
        if (type != null) {
            type.replaceAnnotation(NONNULL);
        }
        return type;
    }

    @Override
    public final AnnotatedDeclaredType getEnclosingType(TypeElement element, Tree tree) {
        AnnotatedDeclaredType dt = super.getEnclosingType(element, tree);
        if (dt != null && dt.hasEffectiveAnnotation(NULLABLE)) {
            dt.replaceAnnotation(NONNULL);
        }
        return dt;
    }

    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(MethodInvocationTree tree) {
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super.methodFromUse(tree);
        AnnotatedExecutableType method = mfuPair.first;

        TreePath path = this.getPath(tree);
        if (path!=null) {
            /* The above check for null ensures that Issue 109 does not arise.
             * TODO: I'm a bit concerned about one aspect: it looks like the field
             * initializer is used to determine the type of a read field. Why is this
             * not just using the declared type of the field?
             * Could this lead to confusion for programmers?
             * I think skipping the mapGetHeuristics is always a safe option.
             */
            mapGetHeuristics.handle(path, method);
        }
        systemGetPropertyHandler.handle(tree, method);
        collectionToArrayHeuristics.handle(tree, method);
        return mfuPair;
    }

    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> constructorFromUse(NewClassTree tree) {
        Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> fromUse = super.constructorFromUse(tree);
        AnnotatedExecutableType constructor = fromUse.first;
        dependentTypes.handleConstructor(tree, generalFactory.getAnnotatedType(tree), constructor);
        return fromUse;
    }

    public Set<VariableElement> initializedAfter(MethodTree node) {
        return ((NullnessFlow)flow).initializedFieldsAfter(
                TreeUtils.elementFromDeclaration(node));
    }

    // called for side effect; return value is always ignored.
    private boolean substituteUnused(Tree tree, AnnotatedTypeMirror type) {
        if (tree.getKind() != Tree.Kind.MEMBER_SELECT
            && tree.getKind() != Tree.Kind.IDENTIFIER)
            return false;

        Element field = InternalUtils.symbol(tree);
        if (field == null || field.getKind() != ElementKind.FIELD)
            return false;

        AnnotationMirror unused = getDeclAnnotation(field, Unused.class);
        if (unused == null) {
            return false;
        }

        Name whenName = AnnotationUtils.getElementValueClassName(unused, "when", false);
        MethodTree method = TreeUtils.enclosingMethod(this.getPath(tree));
        if (TreeUtils.isConstructor(method)) {
            /* TODO: this is messy and should be cleaned up.
             * The problem is that "receiver" means something different in
             * constructors and methods. Should we adapt .getReceiverType to do something
             * different in constructors vs. methods?
             * Or should we change this annotation into a declaration annotation?
             */
            com.sun.tools.javac.code.Symbol meth =
                    (com.sun.tools.javac.code.Symbol)TreeUtils.elementFromDeclaration(method);
            com.sun.tools.javac.util.List<TypeCompound> retannos = meth.getRawTypeAttributes();
            if (retannos == null) {
                return false;
            }
            boolean matched = false;
            for (TypeCompound anno :  retannos) {
                if (anno.getAnnotationType().toString().equals(whenName.toString())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        } else {
            AnnotatedTypeMirror receiver = generalFactory.getReceiverType((ExpressionTree)tree);
            Elements elements = processingEnv.getElementUtils();
            if (receiver == null || !receiver.hasAnnotation(elements.getName(whenName))) {
                return false;
            }
        }
        type.replaceAnnotation(NULLABLE);
        return true;
    }

    /**
     * Substitutes {@link Raw} annotations on a type. If the receiver of
     * the member select expression tree (which might implicitly be "this") is
     * {@link Raw}, this replaces the annotations on {@code type} with
     * {@link Raw}.
     *
     * @param tree a select expression, which might be implicit
     * @param type the type for which to substitute annotations based on the
     *        placement of {@link Raw}
     * @return true iff we reduced the annotated type from {@link NonNull} to
     *         {@link Raw}
     */
    private boolean substituteRaw(ExpressionTree tree, AnnotatedTypeMirror type) {

        // If it's not an expression tree, it's definitely not a select.
        if (tree.getKind() != Tree.Kind.MEMBER_SELECT
                && tree.getKind() != Tree.Kind.IDENTIFIER)
            return false;

        // Identifiers might implicitly select from "this", but not if they're
        // class or interface identifiers.
        if (tree instanceof IdentifierTree) {

            Element elt = TreeUtils.elementFromUse((IdentifierTree) tree);
            if (elt == null || !elt.getKind().isField())
                return false;

            Tree decl = declarationFromElement(elt);
            if (decl != null
                && decl instanceof VariableTree
                && ((VariableTree) decl).getInitializer() != null
                && getAnnotatedType(((VariableTree) decl).getInitializer()).hasEffectiveAnnotation(NONNULL)) {
                return false;
            }
        }

        if (tree instanceof MemberSelectTree
                && "class".contentEquals(((MemberSelectTree)tree).getIdentifier())) {
            // TODO: do not make a "C.class" LazyNonNull.
            // Is there a nicer way for this? Should the type factory
            // assign a type to "C"?
            return false;
        }

        // case 13
        final AnnotatedTypeMirror select = rawnessFactory.getReceiverType(tree);
        if (select != null && select.hasEffectiveAnnotation(RAW)
                && !type.hasEffectiveAnnotation(NULLABLE)
                && !type.getKind().isPrimitive()) {
            boolean wasNN = type.hasEffectiveAnnotation(NONNULL);
            type.replaceAnnotation(LAZYNONNULL);
            return wasNN;
        }
        return false;
    }

    /**
     * If the element is {@link NonNull} when used in a static member access,
     *  modifies the element's type (by adding {@link NonNull}).
     *
     * @param elt the element being accessed
     * @param type the type of the element {@code elt}
     */
    private void annotateIfStatic(Element elt, AnnotatedTypeMirror type) {
        if (elt.getKind().isClass() || elt.getKind().isInterface()
                // Workaround for System.{out,in,err} issue: assume all static
                // fields in java.lang.System are nonnull.
                || isSystemField(elt)) {
            type.replaceAnnotation(NONNULL);
        }
    }

    private boolean isSystemField(Element elt) {
        if (!elt.getKind().isField())
            return false;

        if (!ElementUtils.isStatic(elt) || !ElementUtils.isFinal(elt))
            return false;

        VariableElement var = (VariableElement)elt;

        // Heuristic: if we have a static final field in a system package,
        // treat it as NonNull (many like Boolean.TYPE and System.out
        // have constant value null but are set by the VM).
        boolean inJavaPackage = ElementUtils.getQualifiedClassName(var).toString().startsWith("java.");

        return (var.getConstantValue() != null
                || var.getSimpleName().contentEquals("class")
                || inJavaPackage);
    }

    /**
     * Ensures that every type is fully annotated, no type is unqualified,
     * and no type has multiple qualifiers.
     */
    private class AnnotationCompleter extends AnnotatedTypeScanner<Void, Void> {

        @Override
        protected Void scan(AnnotatedTypeMirror type, Void p) {

            if (type == null) {
                return super.scan(type, p);
            }

            if ( (type.getKind() == TypeKind.TYPEVAR || type.getKind() == TypeKind.WILDCARD)
                    && !type.isAnnotated()) {
                return super.scan(type, p);
            }

            if (!type.isAnnotated()) {
                type.addAnnotation(NULLABLE);
            } else if (type.hasEffectiveAnnotation(RAW)) {
                type.removeAnnotation(NONNULL);
            }

            assert type.isAnnotated() : "NullnessAnnotatedTypeFactory found un-annotated type: " + type;

            return super.scan(type, p);
        }
    }

    /**
     * Adds {@link NonNull} and {@link Raw} annotations to the type based on
     * the underlying (unannotated) type.
     */
    private class NonNullTypeAnnotator extends TypeAnnotator {

        /** Creates a {@link NonNullTypeAnnotator} for the given checker. */
        NonNullTypeAnnotator(BaseTypeChecker checker) {
            super(checker, NullnessAnnotatedTypeFactory.this);
        }

        /**
         * cases 10, 11
         * Adds {@link NonNull} and {@link Raw} annotations to the unannotated
         * method receiver
         */
        @Override
        public Void visitExecutable(AnnotatedExecutableType type, ElementKind p) {

            // Don't add implicit receiver annotations if some are already there.
            if (type.getReceiverType().isAnnotated())
                return super.visitExecutable(type, p);

            ExecutableElement elt = type.getElement();
            assert elt != null;

            if (elt.getKind() == ElementKind.CONSTRUCTOR)
                // case 11. Add @Raw to constructors.
                type.getReceiverType().replaceAnnotation(RAW);
            else if (!ElementUtils.isStatic(elt))
                // case 10 Add @NonNull to non-static non-constructors.
                type.getReceiverType().replaceAnnotation(NONNULL);

            return super.visitExecutable(type, p);
        }
    }

    /**
     * Adds {@link NonNull} annotations to a type based on the AST from which
     * the type was obtained.
     */
    private class NonNullTreeAnnotator extends TreeAnnotator {

        /** Creates a {@link NonNullTreeAnnotator} for the given checker. */
        NonNullTreeAnnotator(BaseTypeChecker checker) {
            super(checker, NullnessAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, AnnotatedTypeMirror type) {
            Element elt = TreeUtils.elementFromUse(node);
            assert elt != null;
            // case 8: class in static member access
            annotateIfStatic(elt, type);
            return super.visitMemberSelect(node, type);
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, AnnotatedTypeMirror type) {
            Element elt = TreeUtils.elementFromUse(node);
            assert elt != null;

            // case 8. static method access
            annotateIfStatic(elt, type);

            // Workaround: exception parameters should be implicitly
            // NonNull, but due to a compiler bug they have
            // kind PARAMETER instead of EXCEPTION_PARAMETER. Until it's
            // fixed we manually inspect enclosing catch blocks.
            // case 9. exception parameter
            if (isExceptionParameter(node))
                type.replaceAnnotation(NONNULL);

            return super.visitIdentifier(node, type);
        }

        @Override
        public Void visitMethod(MethodTree node, AnnotatedTypeMirror type) {
            // A constructor that invokes another constructor in the first
            // statement is nonnull by default, as all the fields
            // should be initialized by the first one
            AnnotatedExecutableType execType = (AnnotatedExecutableType)type;
            if (!execType.getReceiverType().isAnnotated()
                    && TreeUtils.containsThisConstructorInvocation(node))
            execType.getReceiverType().replaceAnnotation(NONNULL);

            return super.visitMethod(node, type);
        }

        private boolean isExceptionParameter(IdentifierTree node) {
            Element elt = TreeUtils.elementFromUse(node);
            assert elt != null;
            return elt.getKind() == ElementKind.EXCEPTION_PARAMETER;
        }

        // The result of a binary operation is always non-null.
        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            if (type.getUnderlyingType().getKind().isPrimitive()) {
                type.replaceAnnotation(PRIMITIVE);
            } else {
                type.replaceAnnotation(NONNULL);
            }
            return null; // super.visitBinary(node, type);
        }

        // The result of a compound operation is always non-null.
        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
            if (type.getUnderlyingType().getKind().isPrimitive()) {
                type.replaceAnnotation(PRIMITIVE);
            } else {
                type.replaceAnnotation(NONNULL);
            }
            return null; // super.visitCompoundAssignment(node, type);
        }

        // The result of a unary operation is always non-null.
        @Override
        public Void visitUnary(UnaryTree node, AnnotatedTypeMirror type) {
            if (type.getUnderlyingType().getKind().isPrimitive()) {
                type.replaceAnnotation(PRIMITIVE);
            } else {
                type.replaceAnnotation(NONNULL);
            }
            return null; // super.visitUnary(node, type);
        }
    }

}
