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


//           if (!type.isAnnotated() && elt.getKind().isClass()) {
//               type.addAnnotation(Level.class);
//           }

        super.annotateImplicit(elt, type);
    }



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

        public LatticeTaintingTreeAnnotator(BaseTypeChecker checker) {

            super(checker, LatticeTaintingAnnotatedTypeFactory.this);

            factory = LatticeTaintingAnnotatedTypeFactory.this;
        }

        private AnnotatedTypeFactory factory;

        @Override
        public Void visitLiteral(LiteralTree tree, AnnotatedTypeMirror type) {
            if (!type.isAnnotated()) {

                if (factory.getVisitorState().getAssignmentContext().getExplicitAnnotations().size() > 0) {

                    for (AnnotationMirror m : factory.getVisitorState().getAssignmentContext().getExplicitAnnotations()) {

                        type.addAnnotation(m);
                        break;
                    }

                }

            }
            return super.visitLiteral(tree, type);
        }
    }
}
