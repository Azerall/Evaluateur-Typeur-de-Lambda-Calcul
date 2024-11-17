// 6. Ajout de la gestion des types produits (enregistrements) et/ou des types sommes (ou option)

// Terme produit (M, N)
class Pair extends Pterm {
    Pterm left, right;

    Pair(Pterm left, Pterm right) {
        this.left = left;
        this.right = right;
    }
}

// Projection Π1 M
class Proj1 extends Pterm {
    Pterm pair;

    Proj1(Pterm pair) {
        this.pair = pair;
    }
}

// Projection Π2 M
class Proj2 extends Pterm {
    Pterm pair;

    Proj2(Pterm pair) {
        this.pair = pair;
    }
}

// Branche gauche g : M
class Left extends Pterm {
    Pterm value;

    Left(Pterm value) {
        this.value = value;
    }
}

// Branche droite d : M
class Right extends Pterm {
    Pterm value;

    Right(Pterm value) {
        this.value = value;
    }
}

// Switch sw M ▷ x : N1 + N2
class Switch extends Pterm {
    Pterm target;
    String leftVar, rightVar;
    Pterm leftBranch, rightBranch;

    Switch(Pterm target, String leftVar, Pterm leftBranch, String rightVar, Pterm rightBranch) {
        this.target = target;
        this.leftVar = leftVar;
        this.leftBranch = leftBranch;
        this.rightVar = rightVar;
        this.rightBranch = rightBranch;
    }
}

// Type produit T × U
class ProdType extends Ptype {
    Ptype left, right;

    ProdType(Ptype left, Ptype right) {
        this.left = left;
        this.right = right;
    }
}

// Type somme T + U
class SumType extends Ptype {
    Ptype left, right;

    SumType(Ptype left, Ptype right) {
        this.left = left;
        this.right = right;
    }
}
