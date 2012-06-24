package com.stralos.shen;

import com.stralos.shen.model.Model;

import fj.data.List;


/**
 * It's important to not modify the line numbers in this file since they are used in the generated code. 
 */
public class Source {
    public static Object bool() {
        return Boolean.TRUE;
    }

    public static Object str() {
        return "constant string";
    }

    public static Object flot() {
        return 5.5d;
    }

    public static Object _int() {
        return 7l;
    }

    public static Object symbol() {
        return Model.symbol("constant string");
    }

    public static Object callPlus(Object x, Object y) {
        return Primitives.plus(x, y);
    }
    
    public static Object list() {
        List<Object> list = List.list((Object)5, "blue", callPlus(5, 4), Boolean.TRUE);
        return list;
    }
}
