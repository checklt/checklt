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

        //System.out.println("calling flow constructor!");

    }


    protected void propagate(Tree lhs,
                             ExpressionTree rhs) {

        System.out.println("Propagating type!!!\n\n");
    }


    protected void scanCond(ExpressionTree tree) {

        System.out.println("Scanning conditional!!");

    }

    //@Override
    protected void scanDef(Tree tree) {

        System.out.println("Scanning Definition!");

    }

    //@Override
    protected void scanStat(StatementTree tree) {

        System.out.println("Scanning Statement!!!!\n\n");

    }

    //@Override
    protected void scanExpr(ExpressionTree tree) {
        System.out.println("Scanning Statement!!!!\n\n");

    }

    //@Override
    public Void visitClass(ClassTree node,
                           Void p) {


        //System.out.println("Visiting Class!!!!!\n\n");

        return null;

    }
}
