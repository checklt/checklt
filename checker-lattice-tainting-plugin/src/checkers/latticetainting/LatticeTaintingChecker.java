package checkers.latticetainting;

/**
 * @author John L. Singleton <jsinglet@gmail.com>
 */


import checkers.basetype.BaseTypeChecker;
import checkers.latticetainting.quals.Level;
import checkers.latticetainting.quals.LevelBottom;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceChecker;
import checkers.source.SuppressWarningsKey;
import checkers.types.QualifierHierarchy;
import checkers.util.AnnotationUtils;
import checkers.util.GraphQualifierHierarchy;
import checkers.util.MultiGraphQualifierHierarchy;
import checkers.util.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.io.File;
import java.util.Collection;
import java.util.Set;


@TypeQualifiers({Level.class, LevelBottom.class})
@SuppressWarningsKey("safe")
public class LatticeTaintingChecker extends BaseTypeChecker {

    public static final String LEVEL_VALUE = "value";
    public static final String LEVEL_NAME = "name";


    protected ExecutableElement labelValueElement;
    protected ExecutableElement labelNameElement;
    protected AnnotationMirror LEVELBOTTOM;

    private String defaultLabel;
    private static String defaultConfigFile = "security.xml";
    private Lattice<String> lattice;

    @Override
    public void initChecker() {
        super.initChecker();

        Elements elements = processingEnv.getElementUtils();
        AnnotationMirror m = AnnotationUtils.fromClass(elements, Level.class);

        LEVELBOTTOM = AnnotationUtils.fromClass(elements, LevelBottom.class);
        labelValueElement = TreeUtils.getMethod("checkers.latticetainting.quals.Level", LEVEL_VALUE, 0, processingEnv);
        labelNameElement = TreeUtils.getMethod("checkers.latticetainting.quals.Level", LEVEL_NAME, 0, processingEnv);


        //setup the lattice.

        // this may be overridden with -Alattice=myfile.xml

        if (processingEnv.getOptions().get("lattice") != null && processingEnv.getOptions().get("lattice").length() > 0) {
            defaultConfigFile = processingEnv.getOptions().get("lattice");
        }

        try {
            lattice = new LatticeParser(new File(defaultConfigFile)).parse();
        } catch (Exception e) {
            SourceChecker.errorAbort(String.format("Encountered fatal error during lattice parsing: " + e.getMessage()));
        }

        // the default label is the one that has the least outgoing edges.
        defaultLabel = lattice.getBottom();
    }


    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphQualifierHierarchy.MultiGraphFactory factory) {
        return new LatticeHierarchy(factory);
    }


    private final class LatticeHierarchy extends GraphQualifierHierarchy {

        @Override
        public Set<AnnotationMirror> leastUpperBounds(Collection<AnnotationMirror> annos1, Collection<AnnotationMirror> annos2) {

            System.out.println("Least upper bounds!");

            return super.leastUpperBounds(annos1, annos2);
        }

        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2) {

            System.out.println("Finding GLB Of Two Types.");

            return super.greatestLowerBound(a1, a2);


        }

        public LatticeHierarchy(MultiGraphFactory f) {
            super(f, LEVELBOTTOM);
        }

        @Override
        public boolean isSubtype(AnnotationMirror anno1, AnnotationMirror anno2) {
//            if (AnnotationUtils.areSameIgnoringValues(rhs, REGEX)
//                    && AnnotationUtils.areSameIgnoringValues(lhs, REGEX)) {
//                int rhsValue = getRegexValue(rhs);
//                int lhsValue = getRegexValue(lhs);
//                return lhsValue <= rhsValue;
//            }


            //System.out.println(String.format("Anno1 Label: %s, Anno2 Label: %s", getAnnotationLabel(anno1), getAnnotationLabel(anno2)));


            // is anno1 a subtype of anno2?

            String label1 = getAnnotationLabel(anno1);
            String label2 = getAnnotationLabel(anno2);

            if (label1.equals(label2))
                return true;


            if (lattice.isSubclass(label1, label2)) {
                return true;
            }


            return false;
            //return super.isSubtype(anno1, anno2);
        }


        /**
         * Gets the label from an annotation. We support multiple types of label syntax.
         *
         * @param anno the annotation to check
         * @return the string label to compare against
         * @Level("Private") means the same thing as @Level(name="Private"), but note that the value
         * parameter is used in the case of the first one. This makes things easier to annotate, but
         * we must check both fields, giving precedence to the "value" (default) field.
         */
        private String getAnnotationLabel(AnnotationMirror anno) {

            ExecutableElement[] elements = new ExecutableElement[]{labelValueElement, labelNameElement};

            for (ExecutableElement e : elements) {
                if (AnnotationUtils.getElementValuesWithDefaults(anno).get(e).getValue().equals("") == false) {
                    return AnnotationUtils.getElementValuesWithDefaults(anno).get(e).getValue().toString();
                }

            }

            return defaultLabel;
        }
    }


}
