import java.io.*;
import java.util.*;
import java.nio.file.*;

class Parser {

    // Liste des opérateurs et mots-clés pris en charge
    private static final String OPERATORS = "+-*/()=,::->";
    private static final Set<String> KEYWORDS = Set.of("let", "in", "ifZero", "fix", "fun");

    private List<String> tokens; // Liste des tokens extraits du fichier
    private int position = 0; // Position actuelle dans les tokens

    public Parser(String input) {
        this.tokens = tokenize(input);
    }

    // Méthode pour convertir une chaîne d'entrée en tokens
    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else if (OPERATORS.contains(String.valueOf(c))) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    // Méthodes pour lire les tokens
    private String peek() {
        return position < tokens.size() ? tokens.get(position) : null;
    }

    private String consume() {
        return position < tokens.size() ? tokens.get(position++) : null;
    }

    // Méthode principale pour parser un terme
    public Pterm parseTerm() {
        String token = consume();

        if ("let".equals(token)) {
            String var = consume(); // Nom de la variable
            consume(); // =
            Pterm value = parseTerm(); // Valeur assignée
            consume(); // in
            Pterm body = parseTerm(); // Corps
            return new Let(new Var(var), value, body);
        } else if ("fun".equals(token)) {
            String param = consume(); // Paramètre
            consume(); // ->
            Pterm body = parseTerm(); // Corps
            return new Abs(param, body);
        } else if ("ifZero".equals(token)) {
            Pterm condition = parseTerm(); // Condition
            consume(); // ,
            Pterm consequence = parseTerm(); // Conséquence
            consume(); // ,
            Pterm alternative = parseTerm(); // Alternative
            return new IfZero(condition, consequence, alternative);
        } else if ("fix".equals(token)) {
            String var = consume(); // Nom de la fonction
            consume(); // ->
            Pterm function = parseTerm(); // Fonction
            return new Fix(var, function);
        } else if (isInteger(token)) {
            return new Int(Integer.parseInt(token));
        } else if ("(".equals(token)) {
            Pterm term = parseTerm(); // Terme entre parenthèses
            consume(); // )
            return term;
        } else if (token != null && Character.isAlphabetic(token.charAt(0))) {
            return new Var(token);
        } else if (token != null && "+-*/".contains(token)) {
            String operator = token;
            Pterm left = parseTerm(); // Opérande gauche
            Pterm right = parseTerm(); // Opérande droite
            switch (operator) {
                case "+" -> {
                    return new Add(left, right);
                }
                case "-" -> {
                    return new Sub(left, right);
                }
                case "*" -> {
                    return new Mul(left, right);
                }
                case "/" -> {
                    return new Div(left, right);
                }
            }
        }

        throw new IllegalArgumentException("Unexpected token: " + token);
    }

    // Méthode utilitaire pour vérifier si une chaîne est un entier
    private boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Méthode pour lire un fichier et évaluer chaque ligne
    public static void evaluateFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            if (line.isBlank()) continue;

            // Tokenizer, parser, et évaluation
            try {
                Parser parser = new Parser(line);
                Pterm term = parser.parseTerm();
                System.out.println("Terme : " + Part2.print_term(term));

                // Normaliser et afficher le résultat
                Pterm normalized = Part2.lt_cbv_norm(term);
                System.out.println("Résultat : " + Part2.print_term(normalized));
            } catch (Exception e) {
                System.out.println("Erreur lors de l'évaluation : " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String filePath = "input.txt"; // Fichier d'entrée
        evaluateFile(filePath);
    }
}
