import checkers.regex.quals.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Segment;

public class Simple {

    void regString() {
        String s1 = "validRegex";
        String s2 = "(InvalidRegex";
    }

    void validRegString() {
        @Regex String s1 = "validRegex";
        //:: error: (assignment.type.incompatible)
        @Regex String s2 = "(InvalidRegex";    // error
    }

    void compileCall() {
        Pattern.compile("test.*[^123]$");
        //:: error: (argument.type.incompatible)
        Pattern.compile("$test.*[^123");    // error
    }

    void requireValidReg(@Regex String reg, String nonReg) {
        Pattern.compile(reg);
        //:: error: (argument.type.incompatible)
        Pattern.compile(nonReg);    // error
    }

    void testAddition(@Regex String reg, String nonReg) {
        @Regex String s1 = reg;
        @Regex String s2 = reg + "d.*sf";
        @Regex String s3 = reg + reg;

        //:: error: (assignment.type.incompatible)
        @Regex String n1 = nonReg;     // error
        //:: error: (assignment.type.incompatible)
        @Regex String n2 = reg + "(df";    // error
        //:: error: (assignment.type.incompatible)
        @Regex String n3 = reg + nonReg;   // error

        //:: error: (assignment.type.incompatible)
        @Regex String o1 = nonReg;     // error
        //:: error: (assignment.type.incompatible)
        @Regex String o2 = nonReg + "sdf";     // error
        //:: error: (assignment.type.incompatible)
        @Regex String o3 = nonReg + reg;     // error
    }

    @Regex String regex = "()";
    String nonRegex = "()";

    void testCompoundConcatenation() {
        takesRegex(regex);
        //:: error: (assignment.type.incompatible)
        regex += ")";    // error
        takesRegex(regex);

        nonRegex = "()";
        // nonRegex is refined by flow to be a regular expression
        takesRegex(nonRegex);
        nonRegex += ")";
        //:: error: (argument.type.incompatible)
        takesRegex(nonRegex);    // error
    }

    void takesRegex(@Regex String s) {}

    void testChar() {
        @Regex char c1 = 'c';
        @Regex Character c2 = 'c';

        //:: error: (assignment.type.incompatible)
        @Regex char c3 = '(';   // error
        //:: error: (assignment.type.incompatible)
        @Regex Character c4 = '(';   // error
    }

    void testCharConcatenation() {
        @Regex String s1 = "rege" + 'x';
        @Regex String s2 = 'r' + "egex";

        //:: error: (assignment.type.incompatible)
        @Regex String s4 = "rege" + '(';   // error
        //:: error: (assignment.type.incompatible)
        @Regex String s5 = "reg(" + 'x';   // error
        //:: error: (assignment.type.incompatible)
        @Regex String s6 = '(' + "egex";   // error
        //:: error: (assignment.type.incompatible)
        @Regex String s7 = 'r' + "ege(";   // error
    }

//    TODO: Uncomment this once isValidUse works better. See RegexChecker.isValidUse for details.
//    class TestAllowedTypes {
//        @Regex CharSequence cs;
//        @Regex String s11;
//        @Regex StringBuilder sb;
//        @Regex Segment s21;
//        @Regex char c;
//        @Regex Pattern p;
//        @Regex Matcher m;
//
//        //:: error: (type.invalid)
//        @Regex Object o;   // error
//        //:: error: (type.invalid)
//        @Regex List<String> l;   // error
//        //:: error: (type.invalid)
//        ArrayList<@Regex Double> al;   // error
//        //:: error: (type.invalid)
//        @Regex int i;   // error
//        //:: error: (type.invalid)
//        @Regex boolean b;   // error
//    }

//    TODO: This is not supported until the checker supports getting explicit
//    annotations from local variables (instead of just fields.)
//    void testAllowedTypes() {
//        @Regex CharSequence cs;
//        @Regex String s11;
//        @Regex StringBuilder sb;
//        @Regex Segment s21;
//        @Regex char c;
//
//        //:: error: (type.invalid)
//        @Regex Object o;   // error
//        //:: error: (type.invalid)
//        @Regex List<String> l;   // error
//        //:: error: (type.invalid)
//        ArrayList<@Regex Double> al;   // error
//        //:: error: (type.invalid)
//        @Regex int i;   // error
//        //:: error: (type.invalid)
//        @Regex boolean b;   // error
//
//        @Regex String regex = "a";
//        //:: error: (argument.type.incompatible)
//        regex += "(";
//
//        String nonRegex = "a";
//        nonRegex += "(";
//    }

    void testPatternLiteral() {
        Pattern.compile("non(", Pattern.LITERAL);
        Pattern.compile(foo("regex"), Pattern.LITERAL);

        //:: error: (argument.type.incompatible)
        Pattern.compile(foo("regex("), Pattern.LITERAL);    // error
        //:: error: (argument.type.incompatible)
        Pattern.compile("non(");    // error
        //:: error: (argument.type.incompatible)
        Pattern.compile(foo("regex"));    // error
        //:: error: (argument.type.incompatible)
        Pattern.compile("non(", Pattern.CASE_INSENSITIVE);    // error
    }

    public static String foo(@Regex String s) {
        return "non((";
    }

//    TODO: This is not supported until the framework can read explicit
//    annotations from arrays.
//    void testArrayAllowedTypes() {
//        @Regex char[] ca1;
//        char @Regex [] ca2;
//        @Regex char @Regex [] ca3;
//        @Regex String[] s1;
//
//        //:: error: (type.invalid)
//        @Regex double[] da1;   // error
//        //:: error: (type.invalid)
//        double @Regex [] da2;   // error
//        //:: error: (type.invalid)
//        @Regex double @Regex [] da3;   // error
//        //:: error: (type.invalid)
//        String @Regex [] s2;    // error
//    }

//    TODO: This is not supported until the regex checker supports flow
//    sensitivity. See the associated comment at
//    checkers/regex/RegexAnnotatedTypeFactory.java:visitNewArray
//    void testCharArrays(char c, @Regex char r) {
//        char @Regex [] c1 = {'r', 'e', 'g', 'e', 'x'};
//        char @Regex [] c2 = {'(', 'r', 'e', 'g', 'e', 'x', ')', '.', '*'};
//        char @Regex [] c3 = {r, 'e', 'g', 'e', 'x'};
//
//        //:: error: (assignment.type.incompatible)
//        char @Regex [] c4 = {'(', 'r', 'e', 'g', 'e', 'x'};   // error
//        //:: error: (assignment.type.incompatible)
//        char @Regex [] c5 = {c, '.', '*'};   // error
//    }
}
