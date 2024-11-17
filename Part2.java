import java.util.ArrayList;
import java.util.List;

// 2.1. Classes permettant de représenter les λ-termes sous forme d’AST
abstract class Pterm {}

class Var extends Pterm {
    String x;
    Var(String x) {
        this.x = x;
    }
}

class App extends Pterm {
    Pterm t1, t2;
    App(Pterm t1, Pterm t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
}

class Abs extends Pterm {
    String x;
    Pterm t;
    Abs(String x, Pterm t) {
        this.x = x;
        this.t = t;
    }
}

class Part2 {

    // 2.2. Pretty printer de termes permettant de convertir les termes en chaînes de caractères lisibles
    static String print_term(Pterm t) {
        if (t instanceof Var) {
            return ((Var) t).x;
        } else if (t instanceof App) {
            return "(" + print_term(((App) t).t1) + " " + print_term(((App) t).t2) + ")";
        } else if (t instanceof Abs) {
            return "(fun " + ((Abs) t).x + " -> " + print_term(((Abs) t).t) + ")";
        } else if (t instanceof Int) {
            return Integer.toString(((Int) t).value);
        } 
        // 4.1.1. Mise à jour du prettyprinter pour les nouveaux termes
        else if (t instanceof Add) {
            return "(" + print_term(((Add) t).left) + " + " + print_term(((Add) t).right) + ")";
        } else if (t instanceof Sub) {
            return "(" + print_term(((Sub) t).left) + " - " + print_term(((Sub) t).right) + ")";
        } else if (t instanceof Mul) {
            return "(" + print_term(((Mul) t).left) + " * " + print_term(((Mul) t).right) + ")";
        } else if (t instanceof Div) {
            return "(" + print_term(((Div) t).left) + " / " + print_term(((Div) t).right) + ")";
        } else if (t instanceof Cons) {
            if (((Cons) t).head instanceof Nil) {
                return "()";
            }
            if (((Cons) t).tail instanceof Nil) {
                return "(" + print_term(((Cons) t).head) + ")";
            }
            return "(" + print_term(((Cons) t).head) + " :: " + print_term(((Cons) t).tail) + ")";
        } else if (t instanceof Nil) {
            return "";
        }
        else if (t instanceof Head) {
            return "head(" + print_term(((Head) t).list) + ")";
        } else if (t instanceof Tail) {
            return "tail(" + print_term(((Tail) t).list) + ")";
        } else if (t instanceof IfZero) {
            return "if " + print_term(((IfZero) t).condition) +
                   " == 0 then " + print_term(((IfZero) t).consequence) +
                   " else " + print_term(((IfZero) t).alternative);
        } else if (t instanceof IfEmpty) {
            return "ifEmpty(" + print_term(((IfEmpty) t).list) + 
                   ", " + print_term(((IfEmpty) t).consequence) + 
                   ", " + print_term(((IfEmpty) t).alternative) + ")";
        } else if (t instanceof Fix) {
            return "fix(" + ((Fix) t).x + " -> " + print_term(((Fix) t).function) + ")";
        } else if (t instanceof Let) {
            return "let " + print_term(((Let) t).variable) + " = " + print_term(((Let) t).value) +
                   " in " + print_term(((Let) t).body);
        } // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (t instanceof Pair) {
            return "(" + print_term(((Pair) t).left) + ", " + print_term(((Pair) t).right) + ")";
        } else if (t instanceof Proj1) {
            return "Π1(" + print_term(((Proj1) t).pair) + ")";
        } else if (t instanceof Proj2) {
            return "Π2(" + print_term(((Proj2) t).pair) + ")";
        } else if (t instanceof Left) {
            return "Left(" + print_term(((Left) t).value) + ")";
        } else if (t instanceof Right) {
            return "Right(" + print_term(((Right) t).value) + ")";
        } else if (t instanceof Switch) {
            return "switch(" + print_term(((Switch) t).branch) + ") { " +
                   "Left(" + ((Switch) t).leftVar + ") -> " + print_term(((Switch) t).leftBranch) + ", " +
                   "Right(" + ((Switch) t).rightVar + ") -> " + print_term(((Switch) t).rightBranch) + " }";
        }
        return "Terme inconnu";
    }

    // Compteur global
    static int compteur_var = 0;

    // Fonction pour générer des nouvelles variables
    static String nouvelle_var() {
        return "X" + compteur_var++;
    }

    // 2.3. Fonction d’alpha-conversion qui renomme toutes les variables liées d’un terme par des nouvelles variables
    static Pterm alphaconv(Pterm t) {
        if (t instanceof Var) {
            return new Var(((Var) t).x);
        } else if (t instanceof App) {
            Pterm t1 = alphaconv(((App) t).t1);
            Pterm t2 = alphaconv(((App) t).t2);
            return new App(t1, t2);
        } else if (t instanceof Abs) {
            String newX = nouvelle_var();
            Pterm newT = alphaconv(((Abs) t).t);
            return new Abs(newX, newT);
        } 
        // 4.1.1. Mise à jour du renommage pour les nouveaux termes
        else if (t instanceof Fix) {
            String newX = nouvelle_var();
            Pterm newFunction = alphaconv(((Fix) t).function);
            return new Fix(newX, substitution(((Fix) t).x, new Var(newX), newFunction));
        } else if (t instanceof Let) {
            String newX = nouvelle_var();
            Pterm newValue = alphaconv(((Let) t).value);
            Pterm newBody = alphaconv(substitution(((Var) ((Let) t).variable).x, new Var(newX), ((Let) t).body));
            return new Let(new Var(newX), newValue, newBody);
        }
        return t;
    }

    // 2.4. Fonction de substitution qui remplace toutes les occurences libres d’une variable données par un terme
    static Pterm substitution(String x, Pterm n, Pterm t) {
        if (t instanceof Var) {
            return ((Var) t).x.equals(x) ? n : t;
        } else if (t instanceof App) {
            return new App(substitution(x, n, ((App) t).t1), substitution(x, n, ((App) t).t2));
        } else if (t instanceof Abs) {
            // Si la variable est liée, on ne substitue pas
            if (((Abs) t).x.equals(x)) {
                return t;  
            }
            // Sinon on renomme si nécessaire
            String x2 = ((Abs) t).x;
            Pterm t2 = ((Abs) t).t;
            if (n instanceof Var && ((Var) n).x.equals(x2)) { // Conflit
                t2 = alphaconv(t2);
                x2 = ((Abs) t2).x;
            }
            return new Abs(x2, substitution(x, n, t2));
        } 
        // 4.1.1. Mise à jour de la substitution pour les nouveaux termes
        else if (t instanceof Add) {
            return new Add(substitution(x, n, ((Add) t).left), substitution(x, n, ((Add) t).right));
        } else if (t instanceof Sub) {
            return new Sub(substitution(x, n, ((Sub) t).left), substitution(x, n, ((Sub) t).right));
        } else if (t instanceof Mul) {
            return new Mul(substitution(x, n, ((Mul) t).left), substitution(x, n, ((Mul) t).right));
        } else if (t instanceof Div) {
            return new Div(substitution(x, n, ((Div) t).left), substitution(x, n, ((Div) t).right));
        } else if (t instanceof Cons) {
            return new Cons(substitution(x, n, ((Cons) t).head), substitution(x, n, ((Cons) t).tail));
        } else if (t instanceof Head) {
            return new Head(substitution(x, n, ((Head) t).list));
        } else if (t instanceof Tail) {
            return new Tail(substitution(x, n, ((Tail) t).list));
        } else if (t instanceof IfZero) {
            return new IfZero(substitution(x, n, ((IfZero) t).condition),
                              substitution(x, n, ((IfZero) t).consequence),
                              substitution(x, n, ((IfZero) t).alternative));
        } else if (t instanceof IfEmpty) {
            return new IfEmpty(substitution(x, n, ((IfEmpty) t).list),
                               substitution(x, n, ((IfEmpty) t).consequence),
                               substitution(x, n, ((IfEmpty) t).alternative));
        } else if (t instanceof Fix) {
            // Si la variable est liée, on ne substitue pas
            if (((Fix) t).x.equals(x)) {
                return t;
            }
            // Sinon on renomme si nécessaire
            String x2 = ((Fix) t).x;
            Pterm function = ((Fix) t).function;
            if (n instanceof Var && ((Var) n).x.equals(x2)) {
                function = alphaconv(function);
                x2 = ((Fix) function).x;
            }
            return new Fix(x2, substitution(x, n, function));
        } else if (t instanceof Let) {
            Pterm newValue = substitution(x, n, ((Let) t).value);
            if (((Var) ((Let) t).variable).x.equals(x)) {
                return new Let(((Let) t).variable, newValue, ((Let) t).body); // Pas de substitution dans le corps
            }
            return new Let(((Let) t).variable, newValue, substitution(x, n, ((Let) t).body));
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (t instanceof Pair) {
            return new Pair(substitution(x, n, ((Pair) t).left), substitution(x, n, ((Pair) t).right));
        } else if (t instanceof Proj1) {
            return new Proj1(substitution(x, n, ((Proj1) t).pair));
        } else if (t instanceof Proj2) {
            return new Proj2(substitution(x, n, ((Proj2) t).pair));
        } else if (t instanceof Left) {
            return new Left(substitution(x, n, ((Left) t).value));
        } else if (t instanceof Right) {
            return new Right(substitution(x, n, ((Right) t).value));
        } else if (t instanceof Switch) {
            if (!((Switch) t).leftVar.equals(x) && !((Switch) t).rightVar.equals(x)) { // Pas de substitution si lié
                Pterm newBranch = substitution(x, n, ((Switch) t).branch);
                Pterm newLeftBranch = substitution(x, n, ((Switch) t).leftBranch);
                Pterm newRightBranch = substitution(x, n, ((Switch) t).rightBranch);
                return new Switch(newBranch, ((Switch) t).leftVar, newLeftBranch, ((Switch) t).rightVar, newRightBranch);
            }
            return t;
        }        
        return t;
    }

    // 2.5. Fonction de réduction Call-by-Value qui implémente la stratégie LtR-CbV
    static Pterm ltr_ctb_step(Pterm t) {
        if (t instanceof App) {
            Pterm t1 = ltr_ctb_step(((App) t).t1);
            Pterm t2 = ltr_ctb_step(((App) t).t2);
            if (t1 instanceof Abs) {
                return substitution(((Abs) t1).x, t2, ((Abs) t1).t);
            }
            return new App(t1, t2);
        } 
        // 4.1.1. Mise à jour de la réduction pour les nouveaux termes
        else if (t instanceof Abs || t instanceof Var || t instanceof Int || t instanceof Nil) {
            return t;
        } else if (t instanceof Add) {
            Pterm left = ltr_ctb_step(((Add) t).left);
            Pterm right = ltr_ctb_step(((Add) t).right);
            if (left instanceof Int && right instanceof Int) {
                return new Int(((Int) left).value + ((Int) right).value);
            }
            return new Add(left, right);
        } else if (t instanceof Sub) {
            Pterm left = ltr_ctb_step(((Sub) t).left);
            Pterm right = ltr_ctb_step(((Sub) t).right);
            if (left instanceof Int && right instanceof Int) {
                return new Int(((Int) left).value - ((Int) right).value);
            }
            return new Sub(left, right);
        } else if (t instanceof Mul) {
            Pterm left = ltr_ctb_step(((Mul) t).left);
            Pterm right = ltr_ctb_step(((Mul) t).right);
            if (left instanceof Int && right instanceof Int) {
                return new Int(((Int) left).value * ((Int) right).value);
            }
            return new Mul(left, right);
        } else if (t instanceof Div) {
            Pterm left = ltr_ctb_step(((Div) t).left);
            Pterm right = ltr_ctb_step(((Div) t).right);
            if (left instanceof Int && right instanceof Int) {
                return new Int(((Int) left).value / ((Int) right).value);
            }
            return new Div(left, right);
        } else if (t instanceof Cons) {
            Pterm head = ltr_ctb_step(((Cons) t).head);
            Pterm tail = ltr_ctb_step(((Cons) t).tail);
            return new Cons(head, tail);
        } else if (t instanceof Head) {
            Pterm list = ltr_ctb_step(((Head) t).list);
            if (list instanceof Cons) {
                return ((Cons) list).head;
            }
            return t;
        } else if (t instanceof Tail) {
            Pterm list = ltr_ctb_step(((Tail) t).list);
            if (list instanceof Cons) {
                return ((Cons) list).tail;
            }
            return t;
        } else if (t instanceof IfZero) {
            Pterm condition = ltr_ctb_step(((IfZero) t).condition);
            if (condition instanceof Int) {
                return ((Int) condition).value == 0
                    ? ltr_ctb_step(((IfZero) t).consequence)
                    : ltr_ctb_step(((IfZero) t).alternative);
            }
            return t;
        } else if (t instanceof IfEmpty) {
            Pterm list = ltr_ctb_step(((IfEmpty) t).list);
            if (list instanceof Nil || (list instanceof Cons && ((Cons) list).head instanceof Nil)) {
                return ltr_ctb_step(((IfEmpty) t).consequence);
            } else {
                return ltr_ctb_step(((IfEmpty) t).alternative);
            }
        } else if (t instanceof Fix) {
            String x = ((Fix) t).x;
            Pterm function = ((Fix) t).function;
            return substitution(x, t, function); // On remplace phi par fix(phi -> M)
        } else if (t instanceof Let) {
            Pterm value = ltr_ctb_step(((Let) t).value);
            return ltr_ctb_step(substitution(((Var) ((Let) t).variable).x, value, ((Let) t).body));
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (t instanceof Pair) {
            Pterm left = ltr_ctb_step(((Pair) t).left);
            Pterm right = ltr_ctb_step(((Pair) t).right);
            return new Pair(left, right);
        } else if (t instanceof Proj1) {
            Pterm pair = ltr_ctb_step(((Proj1) t).pair);
            if (pair instanceof Pair) {
                return ((Pair) pair).left;
            }
            return new Proj1(pair);
        } else if (t instanceof Proj2) {
            Pterm pair = ltr_ctb_step(((Proj2) t).pair);
            if (pair instanceof Pair) {
                return ((Pair) pair).right;
            }
            return new Proj2(pair);
        }  else if (t instanceof Left) {
            return new Left(ltr_ctb_step(((Left) t).value));
        } else if (t instanceof Right) {
            return new Right(ltr_ctb_step(((Right) t).value));
        } else if (t instanceof Switch) {
            Pterm branch = ltr_ctb_step(((Switch) t).branch);
            if (branch instanceof Left) {
                return substitution(((Switch) t).leftVar, ((Left) branch).value, ((Switch) t).leftBranch);
            } else if (branch instanceof Right) {
                return substitution(((Switch) t).rightVar, ((Right) branch).value, ((Switch) t).rightBranch);
            }
            return new Switch(branch, ((Switch) t).leftVar, ((Switch) t).leftBranch, ((Switch) t).rightVar, ((Switch) t).rightBranch);
        }              
        return null;
    }

    // 2.6. Fonction de normalisation qui prend un terme, et le réeduit jusqu’à obtenir sa forme normale
    static Pterm lt_cbv_norm(Pterm t) {
        Pterm reducedTerm = ltr_ctb_step(t);
        if (print_term(reducedTerm).equals(print_term(t))) {
            return reducedTerm;
        } else {
            return lt_cbv_norm(reducedTerm);
        }
    }
}
