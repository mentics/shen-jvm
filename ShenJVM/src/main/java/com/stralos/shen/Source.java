package com.stralos.shen;

import com.stralos.lang.Lambda1;
import com.stralos.shen.model.LList;
import com.stralos.shen.model.Location;
import com.stralos.shen.model.Model;
import com.stralos.shen.parser.FileLocation;


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

    public static Object and(Object x0, Object x1) {
        Boolean value = (boolean) x0 && (boolean) x1;
        System.out.println(value);
        return value;
    }

    public static Object or(Object x0, Object x1) {
        Boolean value = (boolean) x0 || (boolean) x1;
        System.out.println(value);
        return value;
    }

    public static Object if_(Object x0, Object x1, Object x2) {
        return ((boolean) x0) ? x1 : x2;
    }

    public static Object thr() {
        throw new RuntimeException("No clause returned true in cond.");
    }

    public static Object trycatch(Object test, Lambda1 handler) {
        Object v;
        try {
            v = test;
        } catch (Throwable e) {
            v = handler.apply(e);
        }
        return v;
    }

    public static FileLocation fl() {
        
        Object os = null;
        Location loc = null;
        LList.list(loc, os);
        
        return new FileLocation("filename", 17, 13);
    }
}
