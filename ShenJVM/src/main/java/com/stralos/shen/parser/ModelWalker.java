package com.stralos.shen.parser;

import static com.stralos.shen.model.Model.*;

import java.util.Stack;

import com.stralos.shen.model.S;
import com.stralos.shen.parser.AST.Atom;
import com.stralos.shen.parser.AST.Expr;
import com.stralos.shen.parser.AST.ExprList;
import com.stralos.shen.parser.AST.ListOfExpr;
import com.stralos.shen.parser.AST.Walker;

import fj.data.List;


public class ModelWalker extends Walker {
    private String filename;


    public ModelWalker(String filename) {
        this.filename = filename;
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


    Stack<S> st = new Stack<>();
    Stack<Integer> containerIndices = new Stack<>();

    // TODO: is this going to cause issues because it is different from LList.NIL--especially with eval-kl or list
    // creation
    List<S> result = List.nil();


    @Override
    boolean enter(ListOfExpr node) {
        assert st.size() == 0;
        containerIndices.push(0);
        return true;
    }

    @Override
    void leave(ListOfExpr node) {
        int containerIndex = containerIndices.pop();
        S[] items = new S[st.size() - containerIndex];
        int i = items.length - 1;
        while (st.size() > containerIndex) {
            items[i--] = st.pop();
        }
        result = List.list(items);
    }

    boolean enter(ExprList node) {
        containerIndices.push(st.size());
        return true;
    }

    void leave(ExprList node) {
        int containerIndex = containerIndices.pop();
        S[] items = new S[st.size() - containerIndex];
        int i = items.length - 1;
        while (st.size() > containerIndex) {
            items[i--] = st.pop();
        }
        st.push(slist(items));
    }

    void leave(Atom.Bool node) {
        st.push(bool("true".equals(node.bool.text), new FileLocation(filename, node.bool.line, node.bool.column)));
    }

    void leave(Atom.Str node) {
        // TODO: ignore surrounding quotes in scanner/parser
        String s = (String) node.string.text;
        st.push(string(s.substring(1, s.length() - 1), new FileLocation(filename, node.string.line, node.string.column)));
    }

    void leave(Atom.Symbol node) {
        st.push(symbol((String) node.symbol.text, new FileLocation(filename, node.symbol.line, node.symbol.column)));
    }

    void leave(Atom._Int node) {
        try {
            st.push(integer(Double.valueOf((String) node._int.text).longValue(), new FileLocation(filename,
                                                                                                  node._int.line,
                                                                                                  node._int.column)));
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("Invalid number: " + node._int.text + " in " + node.parent.parent.toString());
        }
    }

    void leave(Atom._Float node) {
        st.push(flot(Double.valueOf((String) node._float.text), new FileLocation(filename,
                                                                                 node._float.line,
                                                                                 node._float.column)));
    }

    public List<S> getResult() {
        assert st.size() == 0;
        // return st.pop();
        return result;
    }
}
