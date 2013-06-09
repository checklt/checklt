import checkers.latticetainting.quals.Level;


public class Simple {
     

    public static void main(String args[]) {

	// This assiangment is from Private -> Public
	// which is not allowed in our lattice
        
	//@Level("Public")
        //int e = Simple.testVal();

	// To allow such an assignment, we can "declassify" the value
	// like so:

	@Level("Private")
	int secretValue = Simple.testVal();

	@Level("Public")
	int publicValue = declassify(secretValue);
	
	


    }

    public static int testVal() {
	@Level("Private")
        int notSecret = 1000;
        return notSecret;
    }



    @Level("Public")
    public static int declassify(@Level("Private") int privateVal){
        @SuppressWarnings("safe")
        @Level("Public")
        int publicVal = privateVal;
        
        return publicVal;
    }  



}
