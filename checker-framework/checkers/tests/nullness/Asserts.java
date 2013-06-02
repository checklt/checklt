import checkers.nullness.quals.*;

public class Asserts {

    void propogateToExpr() {
        String s = "m";
        assert false : s.getClass();
    }

    void incorrectAssertExpr() {
        String s = null;
        //:: error: (dereference.of.nullable)
        assert s != null : s.getClass() + " suppress nullness";  // error
        s.getClass();  // OK
    }

    void correctAssertExpr() {
        String s = null;
        assert s == null : s.getClass() + " suppress nullness";
        //:: error: (dereference.of.nullable)
        s.getClass();   // error
    }

    class ArrayCell {
        @Nullable Object[] vals = new @Nullable Object[0];
    }

    void assertComplexExpr (ArrayCell ac, int i) {
        assert ac.vals[i] != null : "@SuppressWarnings(nullness)";
        @NonNull Object o = ac.vals[i];
    }

    boolean pairwiseEqual(boolean @Nullable [] seq1, boolean @Nullable [] seq2) {
        if (! sameLength(seq1, seq2)) { return false; }
        if (ne(seq1[0], seq2[0]));
        return true;
      }

    @AssertNonNullIfTrue({"#1", "#2"})
    boolean sameLength(boolean @Nullable [] seq1, boolean @Nullable [] seq2) {
        // don't bother with the implementation
        //:: error: (assertiftrue.postcondition.not.satisfied)
        return true;
    }

    static boolean ne(boolean a, boolean b) { return true; }


    void testAssertBad(boolean @Nullable [] seq1, boolean @Nullable [] seq2) {
        assert sameLength(seq1, seq2);
        // the AssertNonNullIfTrue is not taken from the assert, as it doesn't contain "nullness"
        //:: error: (accessing.nullable)
        if (seq1[0]);        
    }
    
    void testAssertGood(boolean @Nullable [] seq1, boolean @Nullable [] seq2) {
        assert sameLength(seq1, seq2) : "@SuppressWarnings(nullness)";
        // The explanation contains "nullness" and we therefore take the additional assumption
        if (seq1[0]);        
    }
    
    void testAssertAnd(@Nullable Object o) {
        assert o!=null && o.hashCode() > 6;
    }

    void testAssertOr(@Nullable Object o) {
        //:: error: (dereference.of.nullable)
        assert o!=null || o.hashCode() > 6;
    }
    
}
