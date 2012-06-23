package com.stralos.shen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
    static Map<String, Symbol> symbols = new HashMap<>();

    public static Symbol symbol(String label) {
        Symbol s = symbols.get(label);
        if (s == null) {
            s = new Symbol(label);
            symbols.put(label, s);
        }
        return s;
    }

    public static Int integer(long i) {
        return new Int(i);
    }

    public static Flot flot(double d) {
        return new Flot(d);
    }

    public static S slist(S... ss) {
        return new SList(ss);
    }

    public static String[] toStringArray(S s) {
        if (s instanceof Symbol) {
            return new String[] { ((Symbol) s).toString() };
        } else if (s instanceof SList) {
            List<String> str = new ArrayList<>();
            for (S inside : ((SList) s).ss) {
                str.add(inside.toString());
            }
            return str.toArray(new String[str.size()]);
        } else {
            throw new RuntimeException("Invalid expression for first param of lambda");
        }
    }
}
