import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// 3.1. Classes permettant de représenter les types simples sous forme d’AST
abstract class Ptype {}

class TypeVar extends Ptype {
    String x;
    TypeVar(String x) {
        this.x = x;
    }
}

class Arr extends Ptype {
    Ptype t1, t2;
    Arr(Ptype t1, Ptype t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
}

// 3.3. Classe pour les équations de typage
class Environment {

    private final Map<String, Ptype> env = new HashMap<>();

    // Fonction de recherche dans l’environnement
    public Ptype cherche_type(String var) {
        return env.get(var);
    }

    public void add(String var, Ptype type) {
        env.put(var, type);
    }

    public List<String> getAllTypeVars() {
        return new ArrayList<>(env.keySet());
    }
}

class Equation {
    Ptype left, right;

    Equation(Ptype left, Ptype right) {
        this.left = left;
        this.right = right;
    }

    // Fonction qui génère des équations de typage à partir d’un terme
    static List<Equation> genere_equa(Pterm pterm, Ptype ptype, Environment env) {
        List<Equation> equations = new ArrayList<>();

        if (pterm instanceof Var) {
            String varName = ((Var) pterm).x;
            Ptype Tv = env.cherche_type(varName);
            if (Tv != null) {
                equations.add(new Equation(Tv, ptype));
            }
        } else if (pterm instanceof Abs) {
            String varName = ((Abs) pterm).x;
            Ptype Ta = Part3.nouvelle_var_t();
            Ptype Tr = Part3.nouvelle_var_t();
            env.add(varName, Ta);
            equations.add(new Equation(ptype, new Arr(Ta, Tr)));
            equations.addAll(genere_equa(((Abs) pterm).t, Tr, env));
        } else if (pterm instanceof App) {
            Ptype Ta = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((App) pterm).t1, new Arr(Ta, ptype), env));
            equations.addAll(genere_equa(((App) pterm).t2, Ta, env));
        }
        // 4.2.2. Mise à jour du générateur d’équations pour les nouveaux termes
        else if (pterm instanceof Int) {
            equations.add(new Equation(ptype, new IntType()));
        } else if (pterm instanceof Add) {
            Ptype t1 = Part3.nouvelle_var_t();
            Ptype t2 = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Add) pterm).left, t1, env));
            equations.addAll(genere_equa(((Add) pterm).right, t2, env));
        } else if (pterm instanceof Sub) {
            Ptype t1 = Part3.nouvelle_var_t();
            Ptype t2 = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Sub) pterm).left, t1, env));
            equations.addAll(genere_equa(((Sub) pterm).right, t2, env));
        } else if (pterm instanceof Mul) {
            Ptype t1 = Part3.nouvelle_var_t();
            Ptype t2 = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Mul) pterm).left, t1, env));
            equations.addAll(genere_equa(((Mul) pterm).right, t2, env));
        } else if (pterm instanceof Div) {
            Ptype t1 = Part3.nouvelle_var_t();
            Ptype t2 = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Div) pterm).left, t1, env));
            equations.addAll(genere_equa(((Div) pterm).right, t2, env));
        } else if (pterm instanceof Cons) {
            Ptype elementType = Part3.nouvelle_var_t();
            Ptype listType = new ListType(elementType);
            equations.addAll(genere_equa(((Cons) pterm).head, elementType, env));
            equations.addAll(genere_equa(((Cons) pterm).tail, listType, env));
            equations.add(new Equation(ptype, listType));
        } else if (pterm instanceof Nil) {
            Ptype elementType = Part3.nouvelle_var_t();
            equations.add(new Equation(ptype, new ListType(elementType)));
        } else if (pterm instanceof Head) {
            Ptype elementType = Part3.nouvelle_var_t();
            Ptype listType = new ListType(elementType);
            equations.addAll(genere_equa(((Head) pterm).list, listType, env));
            equations.add(new Equation(ptype, elementType)); // le type de la tête est le type des éléments
        } else if (pterm instanceof Tail) {
            Ptype elementType = Part3.nouvelle_var_t();
            Ptype listType = new ListType(elementType);
            equations.addAll(genere_equa(((Tail) pterm).list, listType, env));
            equations.add(new Equation(ptype, listType)); // le type de la queue est aussi une liste
        } else if (pterm instanceof IfZero) {
            equations.addAll(genere_equa(((IfZero) pterm).condition, new IntType(), env));
            equations.addAll(genere_equa(((IfZero) pterm).consequence, ptype, env));
            equations.addAll(genere_equa(((IfZero) pterm).alternative, ptype, env));
        } else if (pterm instanceof IfEmpty) {
            Ptype listType = new ListType(Part3.nouvelle_var_t());
            equations.addAll(genere_equa(((IfEmpty) pterm).list, listType, env));
            equations.addAll(genere_equa(((IfEmpty) pterm).consequence, ptype, env));
            equations.addAll(genere_equa(((IfEmpty) pterm).alternative, ptype, env));
        } else if (pterm instanceof Fix) {
            String x = ((Fix) pterm).x;
            Ptype functionType = Part3.nouvelle_var_t();
            env.add(x, functionType);
            equations.add(new Equation(ptype, functionType));
            equations.addAll(genere_equa(((Fix) pterm).function, functionType, env));
        } else if (pterm instanceof Let) {
            Ptype t0 = Part3.infereType(((Let) pterm).value); // on type e1
            Ptype generalizedType = Part4.generalizeType(t0, env);
            env.add(((Var) ((Let) pterm).variable).x, generalizedType); // x a le type ∀X1, ..., Xk.T0
            equations.addAll(genere_equa(((Let) pterm).body, ptype, env));
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (pterm instanceof Pair) {
            Ptype leftType = Part3.nouvelle_var_t();
            Ptype rightType = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Pair) pterm).left, leftType, env));
            equations.addAll(genere_equa(((Pair) pterm).right, rightType, env));
            equations.add(new Equation(ptype, new ProdType(leftType, rightType)));
        } else if (pterm instanceof Proj1) {
            Ptype leftType = Part3.nouvelle_var_t();
            Ptype rightType = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Proj1) pterm).pair, new ProdType(leftType, rightType), env));
            equations.add(new Equation(ptype, leftType));
        } else if (pterm instanceof Proj2) {
            Ptype leftType = Part3.nouvelle_var_t();
            Ptype rightType = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Proj2) pterm).pair, new ProdType(leftType, rightType), env));
            equations.add(new Equation(ptype, rightType));
        } else if (pterm instanceof Left) {
            Ptype leftType = Part3.nouvelle_var_t();
            Ptype rightType = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Left) pterm).value, leftType, env));
            equations.add(new Equation(ptype, new SumType(leftType, rightType)));
        } else if (pterm instanceof Right) {
            Ptype leftType = Part3.nouvelle_var_t();
            Ptype rightType = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Right) pterm).value, rightType, env));
            equations.add(new Equation(ptype, new SumType(leftType, rightType)));
        } else if (pterm instanceof Switch) {
            Ptype leftType = Part3.nouvelle_var_t();
            Ptype rightType = Part3.nouvelle_var_t();
            equations.addAll(genere_equa(((Switch) pterm).branch, new SumType(leftType, rightType), env));
            equations.addAll(genere_equa(((Switch) pterm).leftBranch, ptype, env));
            equations.addAll(genere_equa(((Switch) pterm).rightBranch, ptype, env));
        }        
        
        return equations;
    }
}

class Part3 {

    // 3.2. Prettyprinter pour les types
    public static String printType(Ptype t) {
        if (t instanceof TypeVar) {
            return ((TypeVar) t).x;
        } else if (t instanceof Arr) {
            return "(" + printType(((Arr) t).t1) + " -> " + printType(((Arr) t).t2) + ")";
        }
        // 4.2.1. Mise à jour du prettyprinter pour les nouveaux types
        else if (t instanceof IntType) {
            return "N";
        } else if (t instanceof ListType) {
            return "[" + printType(((ListType) t).t) + "]";
        } else if (t instanceof PourToutType) {
            return "∀" + ((PourToutType) t).x + "." + printType(((PourToutType) t).t);
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (t instanceof ProdType) {
            return "(" + printType(((ProdType) t).left) + " × " + printType(((ProdType) t).right) + ")";
        } else if (t instanceof SumType) {
            return "(" + printType(((SumType) t).left) + " + " + printType(((SumType) t).right) + ")";
        }        
        return "";
    }

    // Compteur global
    private static int compteur_var_t = 0;

    // Générateur de variables de type
    public static Ptype nouvelle_var_t() {
        return new TypeVar("T" + compteur_var_t++);
    }

    // 3.4. Fonction d’occur check qui vérifie si une variable appartient à un type
    public static boolean occurCheck(String var, Ptype t) {
        if (t instanceof TypeVar) {
            return var.equals(((TypeVar) t).x);
        } else if (t instanceof Arr) {
            return occurCheck(var, ((Arr) t).t1) || occurCheck(var, ((Arr) t).t2);
        } 
        // 4.2.1. Mise à jour de la fonction d’occur check pour les nouveaux types
        else if (t instanceof IntType) {
            return false;
        } else if (t instanceof ListType) {
            return occurCheck(var, ((ListType) t).t);
        } else if (t instanceof PourToutType) {
            return occurCheck(var, ((PourToutType) t).t);
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (t instanceof ProdType) {
            return occurCheck(var, ((ProdType) t).left) || occurCheck(var, ((ProdType) t).right);
        } else if (t instanceof SumType) {
            return occurCheck(var, ((SumType) t).left) || occurCheck(var, ((SumType) t).right);
        }
        return false;
    }

    // 3.5. Fonction qui substitue une variable de type par un type à l’intérieur d’un autre type
    public static Ptype substituteType(String var, Ptype remplacement, Ptype t) {
        if (t instanceof TypeVar) {
            return var.equals(((TypeVar) t).x) ? remplacement : t;
        } else if (t instanceof Arr) {
            return new Arr(substituteType(var, remplacement, ((Arr) t).t1), substituteType(var, remplacement, ((Arr) t).t2));
        }
        return t;
    }

    // 3.5. Fonction qui substitue une variable de type par un type partout dans un système d’équation
    public static void substituteEquations(String var, Ptype remplacement, List<Equation> equations) {
        for (Equation eq : equations) {
            eq.left = substituteType(var, remplacement, eq.left);
            eq.right = substituteType(var, remplacement, eq.right);
        }
    }

    // 3.6. Fonction pour réaliser une étape d’unification dans les systèmes d’équations de typage
    public static boolean unificationEtape(List<Equation> equations, Map<String, Ptype> substitutions) throws OccurCheckException, UnificationException {
        if (equations.isEmpty()) return true;

        Equation eq = equations.remove(0);
        Ptype left = eq.left;
        Ptype right = eq.right;

        //System.out.println("Unification de: " + printType(left) + " et " + printType(right));

        if (left instanceof TypeVar) {
            String var = ((TypeVar) left).x;
            if (!left.equals(right)) {
                if (occurCheck(var, right)) {
                    throw new OccurCheckException("Occur check échouée pour la variable " + var);
                }
                substituteEquations(var, right, equations);
                substitutions.put(var, right);
                //System.out.println("Substitution de " + var + " avec " + printType(right));
            }
        } else if (right instanceof TypeVar) {
            equations.add(new Equation(right, left));  // on change l’ordre pour y revenir plus tard
        } else if (left instanceof Arr && right instanceof Arr) {
            equations.add(new Equation(((Arr) left).t1, ((Arr) right).t1));
            equations.add(new Equation(((Arr) left).t2, ((Arr) right).t2));
        } 
        // 4.2.1. Mise à jour de la fonction d’unification pour les nouveaux types
        else if (left instanceof ListType && right instanceof ListType) {
            equations.add(new Equation(((ListType) left).t, ((ListType) right).t));
        } else if (left instanceof PourToutType || right instanceof PourToutType) {
            // Barendregtisation
            String freshVar = "T" + compteur_var_t++;
            if (left instanceof PourToutType) {
                left = substituteType(((PourToutType) left).x, new TypeVar(freshVar), ((PourToutType) left).t);
            }
            if (right instanceof PourToutType) {
                right = substituteType(((PourToutType) right).x, new TypeVar(freshVar), ((PourToutType) right).t);
            }
            equations.add(new Equation(left, right)); // Ajoute l'équation sans le ∀
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (left instanceof ProdType && right instanceof ProdType) {
            ProdType leftProd = (ProdType) left;
            ProdType rightProd = (ProdType) right;
            equations.add(new Equation(leftProd.left, rightProd.left));
            equations.add(new Equation(leftProd.right, rightProd.right));
        } else if (left instanceof SumType && right instanceof SumType) {
            SumType leftSum = (SumType) left;
            SumType rightSum = (SumType) right;
            equations.add(new Equation(leftSum.left, rightSum.left));
            equations.add(new Equation(leftSum.right, rightSum.right));
        } else if (!left.getClass().equals(right.getClass())) {
            throw new UnificationException("Constructeurs incompatibles entre " + printType(left) + " et " + printType(right));
        } else if (!(left instanceof IntType) && !(right instanceof IntType)) {
            throw new UnificationException("Unification échouée entre " + printType(left) + " et " + printType(right));
        }
        return true;
    }

    // 3.7. Fonction qui résout un système d’équation
    public static Map<String, Ptype> resoudreEquations(List<Equation> equations) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        Map<String, Ptype> substitutions = new HashMap<>();

        while (!equations.isEmpty()) {
            if (System.currentTimeMillis() - startTime > TimeoutException.TIMEOUT) {
                throw new TimeoutException();
            }

            try {
                unificationEtape(equations, substitutions);
            } catch (OccurCheckException | UnificationException e) {
                System.out.println(e.getMessage());
                return null;
            }
        }
        return substitutions;
    }

    // 3.7. Fonction qui infère le type d’un terme
    public static Ptype infereType(Pterm pterm) {
        compteur_var_t = 0;
        Ptype ptype = nouvelle_var_t();
        Environment env = new Environment();

        List<Equation> equations = Equation.genere_equa(pterm, ptype, env);

        try {
            Map<String, Ptype> substitutions = resoudreEquations(equations);
            //printSubstitutions(substitutions);
            if (substitutions != null) {
                return infereType(ptype, substitutions);
            }
            return null;
        } catch (TimeoutException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Fonction pour reconstruire le type inféré en remplaçant les variables par leurs valeurs
    public static Ptype infereType(Ptype t, Map<String, Ptype> substitutions) {
        if (t instanceof TypeVar) {
            String var = ((TypeVar) t).x;
            if (substitutions.containsKey(var)) {
                Ptype ptype = substitutions.get(var);
                if (ptype instanceof TypeVar) {
                    return infereType(ptype, substitutions);
                } else if (ptype instanceof Arr) {
                    return new Arr(infereType(((Arr) ptype).t1, substitutions), infereType(((Arr) ptype).t2, substitutions));
                } else if (ptype instanceof IntType) {
                    return new IntType();
                } else if (ptype instanceof ListType) {
                    return new ListType(infereType(((ListType) ptype).t, substitutions));
                } else if (ptype instanceof PourToutType) {
                    return new PourToutType(((PourToutType) ptype).x, infereType(((PourToutType) ptype).t, substitutions));
                }
            }
            return t;
        } else if (t instanceof Arr) {
            Arr arr = (Arr) t;
            Ptype newT1 = infereType(arr.t1, substitutions);
            Ptype newT2 = infereType(arr.t2, substitutions);
            return new Arr(newT1, newT2);
        } 
        // 4.2.1. Mise à jour de la fonction d’inférence pour les nouveaux types
        else if (t instanceof IntType) {
            return new IntType();
        } else if (t instanceof ListType) {
            ListType listType = (ListType) t;
            Ptype elementType = infereType(listType.t, substitutions);
            return new ListType(elementType);
        } else if (t instanceof PourToutType) {
            PourToutType pourTout = (PourToutType) t;
            Ptype innerType = infereType(pourTout.t, substitutions);
            return new PourToutType(pourTout.x, innerType);
        }
        // 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)
        else if (t instanceof ProdType) {
            ProdType prod = (ProdType) t;
            Ptype newLeft = infereType(prod.left, substitutions);
            Ptype newRight = infereType(prod.right, substitutions);
            return new ProdType(newLeft, newRight);
        } else if (t instanceof SumType) {
            SumType sum = (SumType) t;
            Ptype newLeft = infereType(sum.left, substitutions);
            Ptype newRight = infereType(sum.right, substitutions);
            return new SumType(newLeft, newRight);
        }        
        return t;
    }

    // Fonction pour afficher les substitutions
    public static void printSubstitutions(Map<String, Ptype> substitutions) {
        if (substitutions == null) {
            return;
        }
        for (Map.Entry<String, Ptype> entry : substitutions.entrySet()) {
            System.out.println(entry.getKey() + " -> " + printType(entry.getValue()));
        }
    }
}
