import java.util.ArrayList;
import java.util.List;

// 4.1.1. Mise à jour de la syntaxe des termes

class Int extends Pterm {
    int value;
    Int(int value) {
        this.value = value;
    }
}

class Add extends Pterm {
    Pterm left, right;
    Add(Pterm left, Pterm right) {
        this.left = left;
        this.right = right;
    }
}

class Sub extends Pterm {
    Pterm left, right;
    Sub(Pterm left, Pterm right) {
        this.left = left;
        this.right = right;
    }
}

class Mul extends Pterm {
    Pterm left, right;
    Mul(Pterm left, Pterm right) {
        this.left = left;
        this.right = right;
    }
}

class Div extends Pterm {
    Pterm left, right;
    Div(Pterm left, Pterm right) {
        this.left = left;
        this.right = right;
    }
}

class Head extends Pterm {
    Pterm list;
    Head(Pterm list) {
        this.list = list;
    }
}

class Tail extends Pterm {
    Pterm list;
    Tail(Pterm list) {
        this.list = list;
    }
}

class Cons extends Pterm {
    Pterm head, tail;
    Cons(Pterm head, Pterm tail) {
        this.head = head;
        this.tail = tail;
    }
}

// Pour représenter la fin d’une liste
class Nil extends Pterm {
    Nil() {}
}

class IfZero extends Pterm {
    Pterm condition, consequence, alternative;
    IfZero(Pterm condition, Pterm consequence, Pterm alternative) {
        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }
}

class IfEmpty extends Pterm {
    Pterm list, consequence, alternative;
    IfEmpty(Pterm list, Pterm consequence, Pterm alternative) {
        this.list = list;
        this.consequence = consequence;
        this.alternative = alternative;
    }
}

class Fix extends Pterm {
    String x;
    Pterm function;
    Fix(String x, Pterm function) {
        this.x = x;
        this.function = function;
    }
}

class Let extends Pterm {
    Pterm variable, value, body;
    Let(Pterm variable, Pterm value, Pterm body) {
        this.variable = variable;
        this.value = value;
        this.body = body;
    }
}

// 4.1.2. Mise à jour de la syntaxe des types
class IntType extends Ptype {}

class ListType extends Ptype {
    Ptype t;
    ListType(Ptype t) {
        this.t = t;
    }
}

class PourToutType extends Ptype {
    String x;
    Ptype t;
    PourToutType(String x, Ptype t) {
        this.x = x;
        this.t = t;
    }
}

class Part4 {
    
    // 4.1.3. fonction qui généralise un type à partir d’un environnement e
    static Ptype generalizeType(Ptype type, Environment env) {
        List<String> envVars = new ArrayList<>(env.getAllTypeVars()); // Récupère les variables de type dans l'environnement
        List<String> freeVars = getFreeTypeVars(type); // Variables libres dans le type

        Ptype result = type;
        for (String var : freeVars) {
            if (!envVars.contains(var)) {
                result = new PourToutType(var, result); // Ajoute ∀ autour du type
            }
        }
        return result;
    }

    // fonction qui récupère les variables de type libres dans un type
    static List<String> getFreeTypeVars(Ptype type) {
        List<String> freeVars = new ArrayList<>();
        if (type instanceof TypeVar) {
            freeVars.add(((TypeVar) type).x);
        } else if (type instanceof Arr) {
            freeVars.addAll(getFreeTypeVars(((Arr) type).t1));
            freeVars.addAll(getFreeTypeVars(((Arr) type).t2));
        } else if (type instanceof ListType) {
            freeVars.addAll(getFreeTypeVars(((ListType) type).t));
        } else if (type instanceof PourToutType) {
            freeVars.remove(((PourToutType) type).x); // Supprimer les variables liées
            freeVars.addAll(getFreeTypeVars(((PourToutType) type).t));
        }
        return freeVars;
    }
}