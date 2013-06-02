package checkers.latticetainting;

import checkers.source.SourceChecker;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * John L. Singleton
 * Date: 4/20/13
 */
public class LatticeParser {

    private File config;

    public LatticeParser(File config) {
        this.config = config;
    }

    public Lattice parse() throws MalformedURLException, DocumentException {


        SAXReader reader = new SAXReader();
        Document document = reader.read(config.toURI().toURL());


        AdjacencyMatrix<String> matrix = parseDocument(document);

        // make sure there are no cycles
        checkGraphContainsCycles(matrix);


        return new Lattice(matrix);
    }

    private List<String> getLevels(Document document) {

        List<String> levels = new ArrayList<String>();

        List<Element> list = document.selectNodes("//levels/level");

        for (Element e : list) {

            // don't allow duplicates
            if (levels.contains(e.getTextTrim())) {
                SourceChecker.errorAbort(String.format("Encountered Duplicate Level: %s. Please visit your <levels> configuration and remove any duplicates.", e.getTextTrim()));
            }

            levels.add(e.getTextTrim());
        }


        return levels;
    }

    private boolean checkGraphContainsCycles(AdjacencyMatrix<String> matrix) {

        List<String> vertexes = matrix.getVertexList();

        // run a modified dfs forall vertexes

        for (String vertex : vertexes) {

            Set<String> seen = new HashSet<String>();
            seen.add(vertex);

            try {
                verifyGraph(vertex, matrix, seen);
            } catch (CyclicSubclassGraphException e) {
                SourceChecker.errorAbort("The specified security lattice is invalid. Reason: " + e.getMessage());
            }
        }


        return false;
    }

    private void verifyGraph(String root, AdjacencyMatrix<String> matrix, Set<String> seen) throws CyclicSubclassGraphException {

        for (String v : matrix.getAdjacentVertexes(root)) {

            if (seen.contains(v) == false) {
                Set<String> nseen = new HashSet<String>(seen);
                nseen.add(v);
                verifyGraph(v, matrix, nseen);
            } else {
                throw new CyclicSubclassGraphException(String.format("Lattice contains cycles %s <~~~~> %s", root, v));
            }

        }


    }


    private AdjacencyMatrix<String> parseDocument(Document document) {

        List<String> levels = getLevels(document);

        AdjacencyMatrix<String> matrix = new AdjacencyMatrix<String>(levels);

        // work through the level specs, adding edges where needed.
        List<Element> list = document.selectNodes("//level-specs/level-spec");

        for (Element e : list) {

            List<Element> nameNodes = e.selectNodes("./name");

            if (nameNodes.size() != 1) {
                SourceChecker.errorAbort("Invalid number of <name> nodes in level spec.");

            }

            String levelName = nameNodes.get(0).getTextTrim();

            List<Element> subClasses = e.selectNodes("./trusts/level");

            for (Element subclass : subClasses) {

                if (levels.contains(subclass.getTextTrim()) == false) {
                    SourceChecker.errorAbort(String.format("Tried to reference a level (%s) in a <level-spec> that was not declared in <levels>.", subclass.getTextTrim()));
                }

                matrix.addEdge(levelName, subclass.getTextTrim());
            }
        }


        return matrix;

    }


    public static void main(String args[]) throws MalformedURLException, DocumentException {

        LatticeParser p = new LatticeParser(new File("security.xml"));

        p.parse();


    }

    class CyclicSubclassGraphException extends Exception {
        public CyclicSubclassGraphException(String s) {
            super(s);
        }
    }


}
