package com.stralos.shen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Model {
    public static S nil = slist();

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

    public static Str string(String s) {
        return new Str(s);
    }

    public static S slist(S... ss) {
        return new SList(ss);
    }

    public static S slist(LList ll) {
        fj.data.List<Object> list = ll.toList();
        S[] result = new S[list.length()];
        int i = 0;
        for (Object o : list) {
            if (o instanceof LList) {
                result[i] = slist((LList) o);
            } else {
                result[i] = toS(o);
            }
            i++;
        }
        return slist(result);
    }

    public static LList list(Object... os) {
        return new LList(os);
    }

    public static S toS(Object o) {
        S newS;
        if (o instanceof S) {
            newS = (S) o;
        } else if (o instanceof String) {
            newS = string((String) o);
        } else if (o instanceof Double) {
            newS = flot((double) o);
        } else if (o instanceof Long) {
            newS = integer((long) o);
        } else {
            throw new RuntimeException("Unknown type in llist->slist: " + o.getClass());
        }
        return newS;
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
