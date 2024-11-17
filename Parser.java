import java.io.*;
import java.util.*;
import java.nio.file.*;

class Parser {

    private String input;
    private int index;

    public Parser(String input) {
        this.input = input;
        this.index = 0;
    }

    private void skipWhitespace() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }
    }

    private boolean match(String keyword) {
        skipWhitespace();
        if (input.startsWith(keyword, index)) {
            index += keyword.length();
            return true;
        }
        return false;
    }

    private void expect(String keyword) {
        skipWhitespace();
        if (!match(keyword)) {
            System.out.println(keyword + " attendu mais " + current() + " trouvé");
        }
    }

    private char current() {
        skipWhitespace();
        if (index < input.length()) {
            return input.charAt(index);
        }
        return ' ';
    }

    private char next() {
        if (index < input.length()) {
            return input.charAt(index++);
        }
        return ' ';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isVarStart(char c) {
        return !Character.isWhitespace(c) && Character.isLetter(c);
    }

    private boolean isVarPart(char c) {
        return isVarStart(c) || isDigit(c);
    }

    public Pterm parse() {
        skipWhitespace();
        if (match("fun")) {
            return parseAbs();
        } else if (match("(")) {
            Pterm term = parseParentheses();
            if (match("(")) {
                Pterm arg = parseParentheses();
                return new App(term, arg);
            }
            return term;
        } else if (match("cons")) {
            return parseList();
        } else if (match("nil")) {
            return new Nil();
        } else if (match("head")) {
            return new Head(parse());
        } else if (match("tail")) {
            return new Tail(parse());
        } else if (match("ifZero")) {
            return parseIfZero();
        } else if (match("ifEmpty")) {
            return parseIfEmpty();
        } else if (match("fix")) {
            return parseFix();
        } else if (match("let")) {
            return parseLet();
        } else if (match("pair")) {
            return parsePair();
        } else if (match("proj1")) {
            return new Proj1(parse());
        } else if (match("proj2")) {
            return new Proj2(parse());
        } else if (match("left")) {
            return new Left(parse());
        } else if (match("right")) {
            return new Right(parse());
        } else if (match("switch")) {
            return parseSwitch();
        } 
        // On vérifie si le terme est un entier ou une variable en dernier
        else if (isDigit(current())) {
            return parseInt();
        } else if (isVarStart(current())) {
            return parseVar();
        } else {
            System.out.println("Terme invalide");
            return null;
        }
    }

    private Pterm parseInt() {
        StringBuilder number = new StringBuilder();
        while (isDigit(current())) {
            number.append(next());
        }
        return new Int(Integer.parseInt(number.toString()));
    }

    private Pterm parseVar() {
        String varName = parseVarName();
        skipWhitespace();
        if (isVarStart(current())) {
            String varName2 = parseVarName();
            return new App(new Var(varName), new Var(varName2));
        } else if (match("(")) {
            Pterm arg = parseParentheses();
            return new App(new Var(varName), arg);
        }
        return new Var(varName);
    }

    private String parseVarName() {
        StringBuilder varName = new StringBuilder();
        if (isVarStart(current())) {
            varName.append(next());
            while (isVarPart(input.charAt(index))) {
                varName.append(next());
            }
        } else {
            return "";
        }
        return varName.toString();
    }

    private Pterm parseAbs() {
        match("fun");
        String varName = parseVarName();
        expect("->");
        Pterm body = parse();
        return new Abs(varName, body);
    }
    
    private Pterm parseParentheses() {
        match("(");
        Pterm left = parse();
        skipWhitespace();
        if (match("+")) {
            Pterm right = parse();
            expect(")");
            return new Add(left, right);
        } else if (match("-")) {
            Pterm right = parse();
            expect(")");
            return new Sub(left, right);
        } else if (match("*")) {
            Pterm right = parse();
            expect(")");
            return new Mul(left, right);
        } else if (match("/")) {
            Pterm right = parse();
            expect(")");
            return new Div(left, right);
        } else if (match("(")) {
            return parseParentheses();
        } else {
            expect(")");
            return left;
        }
    }
    
    private Pterm parseList() {
        match("cons");
        expect("(");
        Pterm head = parse();
        expect(",");
        Pterm tail = parse();
        expect(")");
        return new Cons(head, tail);
    }

    private Pterm parseIfZero() {
        match("ifZero");
        expect("(");
        Pterm condition = parse();
        expect(",");
        Pterm consequence = parse();
        expect(",");
        Pterm alternative = parse();
        expect(")");
        return new IfZero(condition, consequence, alternative);
    }

    private Pterm parseIfEmpty() {
        match("ifEmpty");
        expect("(");
        Pterm list = parse();
        expect(",");
        Pterm consequence = parse();
        expect(",");
        Pterm alternative = parse();
        expect(")");
        return new IfEmpty(list, consequence, alternative);
    }
    
    private Pterm parseFix() {
        match("fix");
        String varName = parseVarName();
        expect("=");
        Pterm function = parse();
        return new Fix(varName, function);
    }

    private Pterm parseLet() {
        match("let");
        String varName = parseVarName();
        expect("=");
        Pterm value = parse();
        expect("in");
        Pterm body = parse();
        if (body instanceof Abs) {
            body = new App(body, new Var(varName));
        }
        return new Let(new Var(varName), value, body);
    }

    private Pterm parsePair() {
        expect("(");
        Pterm left = parse();
        expect(",");
        Pterm right = parse();
        expect(")");
        return new Pair(left, right);
    }

    private Pterm parseSwitch() {
        expect("(");
        Pterm branch = parse();
        expect(",");
        String leftVar = parseVarName();
        expect(":");
        Pterm leftBranch = parse();
        expect(",");
        String rightVar = parseVarName();
        expect(":");
        Pterm rightBranch = parse();
        expect(")");
        return new Switch(branch, leftVar, leftBranch, rightVar, rightBranch);
    }
    
    public static void evaluateFile(String filePath) throws IOException {
        List<String> lignes = Files.readAllLines(Paths.get(filePath));

        for (String ligne : lignes) {
            if (ligne.isBlank()) continue;

            Parser parser = new Parser(ligne);
            Pterm term = parser.parse();
            System.out.println("Terme : " + Part2.print_term(term));

            // Réduire et afficher le résultat
            Pterm normalized = Part2.lt_cbv_norm(term);
            System.out.println("Résultat : " + Part2.print_term(normalized));
            Ptype type = Part3.infereType(term);
            System.out.println("Type : " + Part3.printType(type) + "\n");
        }
    }

    public static void main(String[] args) throws IOException {
        String filePath = "input.txt"; // Fichier d'entrée
        evaluateFile(filePath);
    }
}
