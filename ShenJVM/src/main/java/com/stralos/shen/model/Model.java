package com.stralos.shen.model;

import java.util.ArrayList;
import java.util.List;

import fj.F;


public class Model {
	// TODO: move
    public static final F<Object, String> TO_STRING = new F<Object, String>() {
        public String f(Object o) {
            return o.toString();
        }
    };


    // static Map<String, Symbol> symbols = new HashMap<>();

    // public static Symbol symbol(String label) {
    // Symbol s = symbols.get(label);
    // if (s == null) {
    // s = new Symbol(label);
    // symbols.put(label, s);
    // }
    // return s;
    // }

//    public static Symbol symbol(String label) {
//        return new Symbol(label);
//    }

    public static Symbol symbol(String label, Location loc) {
        return new Symbol(label, loc);
    }

    public static Int integer(long i, Location loc) {
        return new Int(i, loc);
    }

    public static Flot flot(double d, Location loc) {
        return new Flot(d, loc);
    }

    public static Str string(String s, Location loc) {
        return new Str(s, loc);
    }

    public static S bool(boolean b, Location loc) {
        return new Bool(b, loc);
    }

    public static S slist(S... ss) {
        // TODO: we should handle () nil at the parser level, I think, but for now, we'll put it in here
        if (ss == null || ss.length == 0) {
            return Cons.NIL;
        }
        return new SList(ss);
    }

    public static S slist(final Cons c) {
        // fj.data.List<Object> list = ll.toList();
        // S[] result = new S[list.length()];
        List<Object> result = c.forEach(new F<Object, Object>() {
            public Object f(Object o) {
                Object ret;
                if (o instanceof Cons) {
                    ret = slist((Cons) o);
                } else {
                    ret = toS(o);
                }
                return ret;
            }
        });
        return slist(result.toArray(new S[result.size()]));
    }

    public static Cons cons(Location loc, Object head, Object tail) {
        return new Cons(loc, head, tail);
    }

    public static S toS(Object o) {
        S newS;
        if (o instanceof S) {
            newS = (S) o;
        } else if (o instanceof String) {
            newS = string((String) o, Location.UNKNOWN);
        } else if (o instanceof Double) {
            newS = flot((double) o, Location.UNKNOWN);
        } else if (o instanceof Long) {
            newS = integer((long) o, Location.UNKNOWN);
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
        } else if (s instanceof Cons) {
            // fj.data.List<Object> l = ((Cons) s).toList();
            // String[] result = new String[l.length()];
            List<String> ret = ((Cons) s).forEach(TO_STRING);
            return ret.toArray(new String[ret.size()]);
        } else {
            throw new RuntimeException("Invalid expression for first param of lambda");
        }
    }
}
