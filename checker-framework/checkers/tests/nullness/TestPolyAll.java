import checkers.quals.PolyAll;
import checkers.nullness.quals.Nullable;
import checkers.nullness.quals.NonNull;

// Same test as TestPolyNull, just using PolyAll as qualifier.
// Behavior must be the same.
class TestPolyAll {
   @PolyAll String identity(@PolyAll String str) { return str; }
   void test1() { identity(null); }
   void test2() { identity((@Nullable String) null); }

   public static @PolyAll String[] typeArray(@PolyAll Object[] seq) {
    @SuppressWarnings("nullness") // ignore array initialization here. 
    @PolyAll String[] retval = new @Nullable String[seq.length];
    for (int i = 0 ; i < seq.length ; i++) {
      if (seq[i] == null) {
        retval[i] = null;
        retval[i] = "ok";
      } else {
        retval[i] = seq[i].getClass().toString();
        //TODO:: error: (assignment.type.incompatible)
        retval[i] = null;
      }
    }
    return retval;
  }

}
