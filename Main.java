public class Main {
    public static void main(String[] args) {

        // Part2 : Evaluateur pour un λ calcul pur
        System.out.println("\n===== Partie 2 : Evaluateur pour un λ calcul pur =====\n");

        Pterm I = new Abs("x", new Var("x")); // I = λx.x
        Pterm delta = new Abs("x", new App(new Var("x"), new Var("x"))); // δ = λx.(x x)
        Pterm omega = new App(delta, delta); // Ω = δ δ = (λx.(x x))(λy.(y y))
        Pterm S = new Abs("x", new Abs("y", new Abs("z", new App(new App(new Var("x"), new Var("z")), new App(new Var("y"), new Var("z")))))); // S = λxyz.(x z) (y z)
        Pterm K = new Abs("x", new Abs("y", new Var("x"))); // K = λxy.x
        Pterm SKK = new App(new App(S, K), K); // S K K
        Pterm SII = new App(new App(S, I), I); // S I I
        Pterm testAlphaConv = new App(new Abs("x", new App(new Var("x"), new Var("y"))), new Var("x")); // (λx.(xy))x
        
        // Affichage des termes
        System.out.println("I : " + Part2.print_term(I));
        System.out.println("δ : " + Part2.print_term(delta));
        System.out.println("Ω : " + Part2.print_term(omega));
        System.out.println("S : " + Part2.print_term(S));
        System.out.println("S K K : " + Part2.print_term(SKK));
        System.out.println("S I I : " + Part2.print_term(SII));
        System.out.println("(λx.(xy))x : " + Part2.print_term(testAlphaConv));
        
        // Réduction des termes
        System.out.println("\nRéduction des termes :");
        System.out.println("Réduction de I : " + Part2.print_term(Part2.lt_cbv_norm(I)));
        System.out.println("Réduction de δ : " + Part2.print_term(Part2.lt_cbv_norm(delta)));
        System.out.println("Réduction de Ω (δ δ) : " + Part2.print_term(Part2.lt_cbv_norm(omega)));
        System.out.println("Réduction de S : " + Part2.print_term(Part2.lt_cbv_norm(S)));
        System.out.println("Réduction de S K K : " + Part2.print_term(Part2.lt_cbv_norm(SKK)));
        System.out.println("Réduction de S I I : " + Part2.print_term(Part2.lt_cbv_norm(SII)));
        System.out.println("Réduction de (λx.(xy))x : " + Part2.print_term(Part2.lt_cbv_norm(testAlphaConv)));


        // Part3 : Types simples pour le λ-calcul
        System.out.println("\n\n===== Partie 3 : Types simples pour le λ-calcul =====\n");

        Ptype IType = Part3.infereType(I);
        System.out.println("Type de I : " + (IType != null ? Part3.printType(IType) : "Type non inférable"));

        Ptype deltaType = Part3.infereType(delta);
        System.out.println("Type de δ : " + (deltaType != null ? Part3.printType(deltaType) : "Type non inférable"));

        Ptype omegaType = Part3.infereType(omega);
        System.out.println("Type de Ω : " + (omegaType != null ? Part3.printType(omegaType) : "Type non inférable"));

        Ptype SType = Part3.infereType(S);
        System.out.println("Type de S : " + (SType != null ? Part3.printType(SType) : "Type non inférable"));

        Ptype KType = Part3.infereType(K);
        System.out.println("Type de K : " + (KType != null ? Part3.printType(KType) : "Type non inférable"));

        Ptype SKKType = Part3.infereType(SKK);
        System.out.println("Type de S K K : " + (SKKType != null ? Part3.printType(SKKType) : "Type non inférable"));

        Ptype SIIType = Part3.infereType(SII);
        System.out.println("Type de S I I : " + (SIIType != null ? Part3.printType(SIIType) : "Type non inférable"));


        // Part4 : Un λ-calcul enrichi et polymorphe
        System.out.println("\n\n===== Partie 4 : Un λ-calcul enrichi et polymorphe =====\n");

        Pterm add = new Add(new Int(3), new Int(5));
        Pterm sub = new Sub(new Int(10), new Int(5));
        Pterm list = new Cons(add, new Cons(sub, new Nil()));
        Pterm head = new Head(list);
        Pterm tail = new Tail(list);
        Pterm listOfList = new Cons(new Cons(new Int(1), new Int(2)), new Cons(new Cons(new Int(3), new Int(4)), new Cons(new Cons (new Nil(), new Nil()), new Nil())));
        Pterm head2 = new Head(listOfList);
        Pterm tail2 = new Tail(listOfList);
        Pterm ifZero = new IfZero(new Int(0), new Int(1), new Int(2));
        Pterm ifZero2 = new IfZero(new Int(1), new Int(1), new Int(2));
        Pterm ifEmpty = new IfEmpty(list, new Int(1), new Int(2));
        Pterm ifEmpty2 = new IfEmpty(new Cons(new Nil(), new Nil()), new Int(1), new Int(2));
        Pterm factorial = new Fix("fact", 
            new Abs("n", 
                new IfZero(new Var("n"), 
                    new Int(1), 
                    new Mul(new Var("n"), new App(new Var("fact"), new Sub(new Var("n"), new Int(1))))
                )
            )
        );
        Pterm fibonacci = new Fix("fib", new Abs("n", 
            new IfZero(new Var("n"), 
                new Int(0),
                new IfZero(new Sub(new Var("n"), new Int(1)), 
                    new Int(1),
                    new Add(
                        new App(new Var("fib"), new Sub(new Var("n"), new Int(1))),
                        new App(new Var("fib"), new Sub(new Var("n"), new Int(2)))
                    )
                )
            )
        ));
        Pterm length = new Fix("len", 
            new Abs("l", 
                new IfEmpty(new Var("l"), 
                    new Int(0), 
                    new Add(new Int(1), new App(new Var("len"), new Tail(new Var("l"))))
                )
            )
        );
        Pterm letTerm = new Let(new Var("x"), new Int(10), new Add(new Var("x"), new Int(5)));
        Pterm letTermAlphaConversion = new Let(new Var("x"), new Let(new Var("x"), new Int(10), new Add(new Var("x"), new Int(5))), new Add(new Var("x"), new Int(5)));
        Pterm letFactorial = new Let(new Var("x"), new Int(5), new App(factorial, new Var("x")));
        Pterm letFibonacci = new Let(new Var("x"), new Int(7), new App(fibonacci, new Var("x")));
        Pterm letLength = new Let(new Var("l"), listOfList, new App(length, new Var("l")));

        // Affichage des termes
        /*System.out.println("3 + 5 : " + Part2.print_term(add));
        System.out.println("10 - 5 : " + Part2.print_term(sub));
        System.out.println("Liste [(3 + 5), (10 - 5)] : " + Part2.print_term(list));
        System.out.println("Tête de la liste : " + Part2.print_term(head));
        System.out.println("Queue de la liste : " + Part2.print_term(tail));
        System.out.println("Liste [[1, 2], [3, 4], []] : " + Part2.print_term(listOfList));
        System.out.println("Tête de la liste : " + Part2.print_term(head2));
        System.out.println("Queue de la liste : " + Part2.print_term(tail2));
        System.out.println("IfZero(0, 1, 2) : " + Part2.print_term(ifZero));
        System.out.println("IfZero(1, 1, 2) : " + Part2.print_term(ifZero2));
        System.out.println("IfEmpty([(3 + 5), (10 - 5)], 1, 2) : " + Part2.print_term(ifEmpty));
        System.out.println("IfEmpty([], 1, 2) : " + Part2.print_term(ifEmpty2));
        System.out.println("Factorielle : " + Part2.print_term(factorial));
        System.out.println("Fibonacci : " + Part2.print_term(fibonacci));
        System.out.println("Length : " + Part2.print_term(length));
        System.out.println("Let x = 10 in x + 5 : " + Part2.print_term(letTerm));
        System.out.println("Let x = Let x = 10 in x + 5 in x + 5 : " + Part2.print_term(letTermAlphaConversion));
        System.out.println("Let x = 5 in Factorielle(x) : " + Part2.print_term(letFactorial));
        System.out.println("Let x = 10 in Fibonacci(x) : " + Part2.print_term(letFibonacci));
        System.out.println("Let l = [[1, 2], [3, 4], []] in Length(l) : " + Part2.print_term(letLength));*/

        // Réduction des termes
        System.out.println("Réduction de 3 + 5 : " + Part2.print_term(Part2.lt_cbv_norm(add)));
        System.out.println("Réduction de 10 - 5 : " + Part2.print_term(Part2.lt_cbv_norm(sub)));
        System.out.println("Réduction de la liste [(3 + 5), (10 - 5)] : " + Part2.print_term(Part2.lt_cbv_norm(list)));
        System.out.println("Réduction de la Tête de la liste : " + Part2.print_term(Part2.lt_cbv_norm(head)));
        System.out.println("Réduction de la Queue de la liste : " + Part2.print_term(Part2.lt_cbv_norm(tail)));
        System.out.println("Réduction de la liste [[1, 2], [3, 4], []] : " + Part2.print_term(Part2.lt_cbv_norm(listOfList)));
        System.out.println("Réduction de la Tête de la liste : " + Part2.print_term(Part2.lt_cbv_norm(head2)));
        System.out.println("Réduction de la Queue de la liste : " + Part2.print_term(Part2.lt_cbv_norm(tail2)));
        System.out.println("Réduction de IfZero(0, 1, 2) : " + Part2.print_term(Part2.lt_cbv_norm(ifZero)));
        System.out.println("Réduction de IfZero(1, 1, 2) : " + Part2.print_term(Part2.lt_cbv_norm(ifZero2)));
        System.out.println("Réduction de IfEmpty([(3 + 5), (10 - 5)], 1, 2) : " + Part2.print_term(Part2.lt_cbv_norm(ifEmpty)));
        System.out.println("Réduction de IfEmpty([], 1, 2) : " + Part2.print_term(Part2.lt_cbv_norm(ifEmpty2)));
        System.out.println("Réduction de Factorielle : " + Part2.print_term(Part2.lt_cbv_norm(factorial)));
        System.out.println("Réduction de Fibonacci : " + Part2.print_term(Part2.lt_cbv_norm(fibonacci)));
        System.out.println("Réduction de Length : " + Part2.print_term(Part2.lt_cbv_norm(length)));
        System.out.println("Réduction de Let x = 10 in x + 5 : " + Part2.print_term(Part2.lt_cbv_norm(letTerm)));
        System.out.println("Réduction de Let x = Let x = 10 in x + 5 in x + 5 : " + Part2.print_term(Part2.lt_cbv_norm(letTermAlphaConversion)));
        System.out.println("Réduction de Let x = 5 in Factorielle(x) : " + Part2.print_term(Part2.lt_cbv_norm(letFactorial)));
        System.out.println("Réduction de Let x = 7 in Fibonacci(x) : " + Part2.print_term(Part2.lt_cbv_norm(letFibonacci)));
        System.out.println("Réduction de Let l = [[1, 2], [3, 4], []] in Length(l) : " + Part2.print_term(Part2.lt_cbv_norm(letLength)));
        System.out.println();
        
        // Types des termes
        Ptype addType = Part3.infereType(add);
        System.out.println("Type de 3 + 5 : " + (addType != null ? Part3.printType(addType) : "Type non inférable"));

        Ptype subType = Part3.infereType(sub);
        System.out.println("Type de 10 - 5 : " + (subType != null ? Part3.printType(subType) : "Type non inférable"));

        Ptype listType = Part3.infereType(list);
        System.out.println("Type de la liste [(3 + 5), (10 - 5)] : " + (listType != null ? Part3.printType(listType) : "Type non inférable"));

        Ptype headType = Part3.infereType(head);
        System.out.println("Type de la Tête de la liste : " + (headType != null ? Part3.printType(headType) : "Type non inférable"));

        Ptype tailType = Part3.infereType(tail);
        System.out.println("Type de la Queue de la liste : " + (tailType != null ? Part3.printType(tailType) : "Type non inférable"));

        Ptype listOfListType = Part3.infereType(listOfList);
        System.out.println("Type de la liste [[1, 2], [3, 4], []] : " + (listOfListType != null ? Part3.printType(listOfListType) : "Type non inférable"));

        Ptype head2Type = Part3.infereType(head2);
        System.out.println("Type de la Tête de la liste : " + (head2Type != null ? Part3.printType(head2Type) : "Type non inférable"));

        Ptype tail2Type = Part3.infereType(tail2);
        System.out.println("Type de la Queue de la liste : " + (tail2Type != null ? Part3.printType(tail2Type) : "Type non inférable"));

        Ptype ifZeroType = Part3.infereType(ifZero);
        System.out.println("Type de IfZero(0, 1, 2) : " + (ifZeroType != null ? Part3.printType(ifZeroType) : "Type non inférable"));

        Ptype ifZero2Type = Part3.infereType(ifZero2);
        System.out.println("Type de IfZero(1, 1, 2) : " + (ifZero2Type != null ? Part3.printType(ifZero2Type) : "Type non inférable"));

        Ptype ifEmptyType = Part3.infereType(ifEmpty);
        System.out.println("Type de IfEmpty([(3 + 5), (10 - 5)], 1, 2) : " + (ifEmptyType != null ? Part3.printType(ifEmptyType) : "Type non inférable"));

        Ptype ifEmpty2Type = Part3.infereType(ifEmpty2);
        System.out.println("Type de IfEmpty([], 1, 2) : " + (ifEmpty2Type != null ? Part3.printType(ifEmpty2Type) : "Type non inférable"));

        Ptype factorialType = Part3.infereType(factorial);
        System.out.println("Type de Factorielle : " + (factorialType != null ? Part3.printType(factorialType) : "Type non inférable"));

        Ptype fibonacciType = Part3.infereType(fibonacci);
        System.out.println("Type de Fibonacci : " + (fibonacciType != null ? Part3.printType(fibonacciType) : "Type non inférable"));

        Ptype lengthType = Part3.infereType(length);
        System.out.println("Type de Length : " + (lengthType != null ? Part3.printType(lengthType) : "Type non inférable"));

        Ptype letTermType = Part3.infereType(letTerm);
        System.out.println("Type de Let x = 10 in x + 5 : " + (letTermType != null ? Part3.printType(letTermType) : "Type non inférable"));

        Ptype letTermAlphaConversionType = Part3.infereType(letTermAlphaConversion);
        System.out.println("Type de Let x = Let x = 10 in x + 5 in x + 5 : " + (letTermAlphaConversionType != null ? Part3.printType(letTermAlphaConversionType) : "Type non inférable"));

        Ptype letFactorialType = Part3.infereType(letFactorial);
        System.out.println("Type de Let x = 5 in Factorielle(x) : " + (letFactorialType != null ? Part3.printType(letFactorialType) : "Type non inférable"));

        Ptype letFibonacciType = Part3.infereType(letFibonacci);
        System.out.println("Type de Let x = 7 in Fibonacci(x) : " + (letFibonacciType != null ? Part3.printType(letFibonacciType) : "Type non inférable"));

        Ptype letLengthType = Part3.infereType(letLength);
        System.out.println("Type de Let l = [[1, 2], [3, 4], []] in Length(l) : " + (letLengthType != null ? Part3.printType(letLengthType) : "Type non inférable"));

        // Part6 : Aller plus loin
        System.out.println("\n\n===== Partie 6 : Aller plus loin =====\n");

        Pterm pair = new Pair(new Int(3), new Int(5));
        Pterm proj1 = new Proj1(pair);
        Pterm proj2 = new Proj2(pair);
        Pterm left = new Left(new Int(42));
        Pterm right = new Right(new Int(24));
        Pterm switchCase = new Switch(left, "x", new Var("x"), "y", new Int(0));

        // Réduction des termes
        System.out.println("Réduction de " + Part2.print_term(pair) + " : " + Part2.print_term(Part2.lt_cbv_norm(pair)));
        System.out.println("Réduction de " + Part2.print_term(proj1) + " : " + Part2.print_term(Part2.lt_cbv_norm(proj1)));
        System.out.println("Réduction de " + Part2.print_term(proj2) + " : " + Part2.print_term(Part2.lt_cbv_norm(proj2)));
        System.out.println("Réduction de " + Part2.print_term(left) + " : " + Part2.print_term(Part2.lt_cbv_norm(left)));
        System.out.println("Réduction de " + Part2.print_term(right) + " : " + Part2.print_term(Part2.lt_cbv_norm(right)));
        System.out.println("Réduction de " + Part2.print_term(switchCase) + " : " + Part2.print_term(Part2.lt_cbv_norm(switchCase)));
        System.out.println();
        
        // Types des termes
        Ptype pairType = Part3.infereType(pair);
        System.out.println("Type de " + Part2.print_term(pair) + " : " + (pairType != null ? Part3.printType(pairType) : "Type non inférable"));

        Ptype proj1Type = Part3.infereType(proj1);
        System.out.println("Type de " + Part2.print_term(proj1) + " : " + (proj1Type != null ? Part3.printType(proj1Type) : "Type non inférable"));

        Ptype proj2Type = Part3.infereType(proj2);
        System.out.println("Type de " + Part2.print_term(proj2) + " : " + (proj2Type != null ? Part3.printType(proj2Type) : "Type non inférable"));

        Ptype leftType = Part3.infereType(left);
        System.out.println("Type de " + Part2.print_term(left) + " : " + (leftType != null ? Part3.printType(leftType) : "Type non inférable"));

        Ptype rightType = Part3.infereType(right);
        System.out.println("Type de " + Part2.print_term(right) + " : " + (rightType != null ? Part3.printType(rightType) : "Type non inférable"));

        Ptype switchCaseType = Part3.infereType(switchCase);
        System.out.println("Type de " + Part2.print_term(switchCase) + " : " + (switchCaseType != null ? Part3.printType(switchCaseType) : "Type non inférable"));
    }
}
