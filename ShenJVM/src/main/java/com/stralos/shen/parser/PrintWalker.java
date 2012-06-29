package com.stralos.shen.parser;

import com.stralos.shen.parser.AST.Atom;
import com.stralos.shen.parser.AST.Expr;
import com.stralos.shen.parser.AST.ExprList;
import com.stralos.shen.parser.AST.ListOfExpr;


final class PrintWalker extends AST.Walker {
    int indent = 0;


    void indentText() {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
    }

    boolean enter(ListOfExpr node) {
        indentText();
        System.out.println("ListOfExpr");
        indent++;
        return true;
    }

    void leave(ListOfExpr node) {
        indent--;
    }


    // Overridden because genned code throws NPE
    void visit(Expr node) {
        if (enter(node)) {
            if (node.atom != null) {
                node.atom.accept(this);
            } else if (node.exprList != null) {
                node.exprList.accept(this);
            }
            leave(node);
        }
    }

    boolean enter(Atom.Bool node) {
        indentText();
        System.out.println("Bool " + node.bool.text);
        indent++;
        return true;
    }

    void leave(Atom.Bool node) {
        indent--;
    }

    boolean enter(Atom.Str node) {
        indentText();
        System.out.println("Str " + node.string.text);
        indent++;
        return true;
    }

    void leave(Atom.Str node) {
        indent--;
    }

    boolean enter(ExprList node) {
        indentText();
        System.out.println("ExprList");
        indent++;
        return true;
    }

    void leave(ExprList node) {
        indent--;
    }

    boolean enter(Atom.Symbol node) {
        indentText();
        System.out.println("Symbol " + node.symbol.text);
        indent++;
        return true;
    }

    void leave(Atom.Symbol node) {
        indent--;
    }

    boolean enter(Atom._Int node) {
        indentText();
        System.out.println("Int " + node._int.text);
        indent++;
        return true;
    }

    void leave(Atom._Int node) {
        indent--;
    }

    boolean enter(Atom._Float node) {
        indentText();
        System.out.println("Float " + node._float.text);
        indent++;
        return true;
    }

    void leave(Atom._Float node) {
        indent--;
    }
}