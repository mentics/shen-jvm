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
        st.push(bool("true".equals(node.bool.text)));
    }

    void leave(Atom.Str node) {
        // TODO: ignore surrounding quotes in scanner/parser
        String s = (String) node.string.text;
        st.push(string(s.substring(1, s.length() - 1)));
    }

    void leave(Atom.Symbol node) {
        st.push(symbol((String) node.symbol.text));
    }

    void leave(Atom._Int node) {
        st.push(integer(Double.valueOf((String) node._int.text).longValue()));
    }

    void leave(Atom._Float node) {
        st.push(flot(Double.valueOf((String) node._float.text)));
    }

    public List<S> getResult() {
        assert st.size() == 0;
        // return st.pop();
        return result;
    }
}
