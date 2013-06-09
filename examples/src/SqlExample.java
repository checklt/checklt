import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;
import checkers.latticetainting.quals.*;


public class SqlExample {

    public Connection getConnection() throws Exception {
	Class.forName("com.mysql.jdbc.Driver");
	return DriverManager.getConnection("jdbc:mysql://localhost/checklt_example?user=root&password=fruitile");
    }

    // require that all of the executed queries are "Safe"
    private void executeQuery(@Level("Internal") String q) throws Exception {

	Connection c = getConnection();

	try {
	    Statement stmt = getConnection().createStatement();
	    ResultSet rs =   stmt.executeQuery(q);
	}finally {
	    c.close();
	}
    }

    // internal queries are "Safe" by definition
    @Level("Internal")
    public String getBaseQuery()
    {
	@Level("Internal")
	String query = "SELECT * from test_table";

	return query;
    }

    // running an internal query is no problem, since the types @Level("Internal") will match throughout.
    public void runInternalQuery() throws Exception {
	executeQuery(getBaseQuery());
    }

    public void getAndRunQueryFromUser() throws Exception {

	// baseQuery is implicitly @Level("Internal")
	String baseQuery = getBaseQuery();
	
	// newQuery is now @Level("TouchedByUser");
	String newQuery = baseQuery + getQueryFromUser();

	
	// This call will fail
	//executeQuery(newQuery);
	
	// It must be called like:
	executeQuery(SqlExample.sanitizeUserInput(newQuery));
    }

    

    // Because this has not been explicitly marked with @Level("Internal"), it is "TouchedByUser" by default
    public String getQueryFromUser(){

      System.out.print("Enter Query Filter [NONE]: ");

      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      String query = null;

      try {
	  query =  br.readLine();
      } catch (IOException ioe) {
	  System.out.println("IO Error?");
	  System.exit(1);
      }
      return query;
    }


    @Level("Internal")
    public static String sanitizeUserInput(@Level("TouchedByUser") String dirty){

	//
	// do some cleanup, remove SQL injections, etc
	//
	
	// Mark this as "clean" input
        @SuppressWarnings("safe")
        @Level("Internal")
        String clean = dirty;
        
        return clean;
    } 

    public static void main(String args[]) throws Exception {

	System.out.println("====Executing Database Test===");

	SqlExample ex = new SqlExample();

	// first, try to run an internal query.
	ex.runInternalQuery();
	// next, try to execute an internal query that has 
	// been modified by the user
	ex.getAndRunQueryFromUser();
    }
    
}
