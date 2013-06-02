package checkers.latticetainting;

import checkers.basetype.BaseTypeChecker;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.types.TreeAnnotator;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LiteralTree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Set;

/**
 * John L. Singleton
 * Date: 4/20/13
 */
public class LatticeTaintingAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<LatticeTaintingChecker> {

    @Override
    public void annotateImplicit(Element elt, AnnotatedTypeMirror type) {

//        System.out.println("Asking for implicit annotation!!!");
//
//        System.out.println(String.format("Element is: %s", elt.getKind().toString()));


//           if (!type.isAnnotated() && elt.getKind().isClass()) {
//               type.addAnnotation(Level.class);
//           }

        super.annotateImplicit(elt, type);
    }


//    @Override
//    public void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
//        treeAnnotator.visit(tree, type);
//        typeAnnotator.visit(type);
//
//        System.out.println("Tree Annotation Implicit!!");
//
//
//
//        final Set<AnnotationMirror> inferred = flow.test(tree);
//
//        if (tree.getKind() == Tree.Kind.VARIABLE) {
//
//            if (inferred != null) {
//                // case 7: flow analysis
//                type.replaceAnnotations(inferred);
//            }
//        }
//
//
//        defaults.annotate(tree, type);
//
//        // case 6: apply default
//
////        if (TreeUtils.isExpressionTree(tree)) {
////            substituteRaw((ExpressionTree) tree, type);
////        }
////        substituteUnused(tree, type);
////
////        if (useFlow) {
////            final Set<AnnotationMirror> inferred = flow.test(tree);
////            if (inferred != null) {
////                // case 7: flow analysis
////                type.replaceAnnotations(inferred);
////            }
////        }
////
////        dependentTypes.handle(tree, type);
////        completer.visit(type);
////
//    }


    public LatticeTaintingAnnotatedTypeFactory(LatticeTaintingChecker checker, CompilationUnitTree root) {

        super(checker, root);

        this.postInit();

    }


    @Override
    public LatticeTaintingFlow createFlow(LatticeTaintingChecker checker, CompilationUnitTree tree,
                                          Set<AnnotationMirror> flowQuals) {
        return new LatticeTaintingFlow(checker, tree, flowQuals, this);
    }

    @Override
    public TreeAnnotator createTreeAnnotator(LatticeTaintingChecker checker) {
        return new LatticeTaintingTreeAnnotator(checker);
    }


    private class LatticeTaintingTreeAnnotator extends TreeAnnotator {
        //        @Override
//        public Void defaultAction(Tree tree, AnnotatedTypeMirror type) {
//
//
//            if (!type.isAnnotated()) {
//                //
//                System.out.println("\n\nCalling Default Action\n\n");
//
//                // by default annotate with parivate
//                AnnotationBuilder builder =
//                        new AnnotationBuilder(processingEnv, Level.class.getCanonicalName());
//                builder.setValue("value", "Private");
//
//                type.addAnnotation(builder.build());
//            }
//
//            return super.defaultAction(tree, type);
//        }
//
//        @Override
//        public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
//
//            System.out.println("COMPOUND ASSIGNMENT\n\n");
//
//            //return super.visitCompoundAssignment(node, type);
//            return null;
//        }
//
        public LatticeTaintingTreeAnnotator(BaseTypeChecker checker) {

            super(checker, LatticeTaintingAnnotatedTypeFactory.this);

            factory = LatticeTaintingAnnotatedTypeFactory.this;
        }

        private AnnotatedTypeFactory factory;
//
//        @Override
//        public Void visitAssignment(AssignmentTree node, AnnotatedTypeMirror annotatedTypeMirror) {
//
//            System.out.println("Visiting Assignement!\n\n");
//
//            return super.visitAssignment(node, annotatedTypeMirror);
//        }

        @Override
        public Void visitLiteral(LiteralTree tree, AnnotatedTypeMirror type) {
            if (!type.isAnnotated()) {

                if (factory.getVisitorState().getAssignmentContext().getExplicitAnnotations().size() > 0) {

                    for (AnnotationMirror m : factory.getVisitorState().getAssignmentContext().getExplicitAnnotations()) {

                        type.addAnnotation(m);
                        break;
                    }

                }

                //type.atypeFactory.getVisitorState().getAssignmentContext().getExplicitAnnotations().size();
//
//                System.out.println("Visiting unannotated type~\n\n");
//
//                AnnotationBuilder builder =
//                        new AnnotationBuilder(processingEnv, Level.class.getCanonicalName());
//                builder.setValue("value", "Public");
//
////                type.addAnnotation(builder.build());
//
//                String regex = null;
////                   if (tree.getKind() == Tree.Kind.STRING_LITERAL) {
////                       regex = (String) tree.getValue();
////                   } else if (tree.getKind() == Tree.Kind.CHAR_LITERAL) {
////                       regex = Character.toString((Character) tree.getValue());
////                   }
////                   if (regex != null) {
////                       if (isRegex(regex)) {
////                           int groupCount = checker.getGroupCount(regex);
////                           type.addAnnotation(createRegexAnnotation(groupCount));
////                       } else {
////                           type.addAnnotation(createPartialRegexAnnotation(regex));
////                       }
////                   }
            }
            return super.visitLiteral(tree, type);
        }
    }
}
