package checkers.latticetainting;

import checkers.basetype.BaseTypeChecker;
import checkers.flow.DefaultFlow;
import checkers.flow.DefaultFlowState;
import checkers.types.AnnotatedTypeFactory;
import com.sun.source.tree.*;

import javax.lang.model.element.AnnotationMirror;
import java.util.Set;

/**
 * John L. Singleton
 * Date: 4/20/13
 */
public class LatticeTaintingFlow extends DefaultFlow<DefaultFlowState> {
    public LatticeTaintingFlow(BaseTypeChecker checker, CompilationUnitTree root, Set<AnnotationMirror> annotations, AnnotatedTypeFactory factory) {
        super(checker, root, annotations, factory);
    }


    protected void propagate(Tree lhs,
                             ExpressionTree rhs) {
    }


    protected void scanCond(ExpressionTree tree) {
    }

    //@Override
    protected void scanDef(Tree tree) {
    }

    //@Override
    protected void scanStat(StatementTree tree) {
    }

    //@Override
    protected void scanExpr(ExpressionTree tree) {
    }

    //@Override
    public Void visitClass(ClassTree node,
                           Void p) {

        return null;

    }
}
