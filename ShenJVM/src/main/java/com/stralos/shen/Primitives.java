package com.stralos.shen;

import static com.stralos.shen.model.Model.*;

import java.lang.reflect.Field;

import com.stralos.asm.ASMUtil;
import com.stralos.lang.Lambda;
import com.stralos.lang.Lambda1;
import com.stralos.lang.Lambda2;

import fj.data.List;


public class Primitives {
    public static final String PRIMITIVES_PATH = "com/stralos/shen/Primitives";

    public static final String LAMBDA_METHOD_NAME = "apply";
    public static final String LAMBDA_PATH_BASE = "com/stralos/lang/Lambda";
    public static final String NEW_LAMBDA_PATH_BASE = "shen/lambda/ToRun";

    public static final String SYMBOL_PATH = "com/stralos/shen/model/Symbol";
    public static final String MODEL_PATH = "com/stralos/shen/model/Model";

    public static final String COMPILER_PATH = "com/stralos/shen/ShenCompiler";
    public static final String COMPILER_METHOD_NAME = "compile";

    public static final Object FAIL = new Object();

    // Other //

    public static Lambda intern = new Lambda1() {
        public Object apply(Object str) {
            return symbol((String) str);
        }
    };


    // Boolean Operations are handled specially because they don't evaluate all their arguments //
    // TODO: remove this todo when they're done


    // Strings //

    /**
     * string --> number --> string
     * given a natural number 0 ...n and a string S returns the nth unit string in S
     */
    public static Lambda pos = new Lambda2() {
        public Object apply(Object string, Object index) {
            int ind = ((Number) index).intValue();
            return ((String) string).substring(ind, ind + 1);
        }
    };

    /**
     * string --> string
     * returns all but the first unit string of a string
     */
    public static Lambda tlstr = new Lambda1() {
        public Object apply(Object str) {
            return ((String) str).substring(1);
        }
    };

    /**
     * string --> string --> string
     * concatenates two strings
     */
    public static Lambda cn = new Lambda2() {
        public Object apply(Object str0, Object str1) {
            return (String) str0 + (String) str1;
        }
    };

    /**
     * A --> string
     * maps any atom to a string
     */
    public static Lambda str = new Lambda1() {
        public Object apply(Object atom) {
            return atom.toString();
        }
    };

    /**
     * A --> boolean
     * test for strings
     */
    public static Lambda string_ = new Lambda1() {
        public Object apply(Object string) {
            return string instanceof String;
        }
    };

    /**
     * number --> string
     * maps a code point in decimal to the corresponding unit string
     */
    public static Lambda n__string = new Lambda1() {
        public Object apply(Object codePoint) {
            return String.valueOf((char) ((Number) codePoint).intValue());
        }
    };

    /**
     * string --> number
     * maps a unit string to the corresponding decimal
     */
    public static Lambda string__n = new Lambda1() {
        public Object apply(Object string) {
            // TODO: throw error if more than 1 in length?
            return Long.valueOf(((String) string).charAt(0));
        }
    };


    // Assignments //

    /**
     * (value X) : A;
     * X : symbol;
     * Y : A;______
     * (set X Y) : A;
     * assigns a value to a symbol
     */
    public static Lambda set = new Lambda2() {
        public Object apply(Object symbol, Object value) {
            Environment.theEnvironment().assign(symbol, value);
            return value;
        }
    };

    /**
     * retrieves the value of a symbol
     */
    public static Lambda value = new Lambda1() {
        public Object apply(Object symbol) {
            Object o = Environment.theEnvironment().get(symbol);
            return o != null ? o : List.nil();
        }
    };


    // Error Handling //

    /**
     * throws an exception
     */
    public static Lambda simple_error = new Lambda1() {
        public Object apply(Object message) {
            throw new RuntimeException((String) message);
        }
    };

    /**
     * trap-error has to be handled specially
     * evaluates its first argument A; if it is not an exception returns the normal form, returns A else applies its
     * second argument to the exception
     * TODO: remove this todo when it's done
     */

    /**
     * exception --> string
     * maps an exception to a string
     */
    public static Lambda error_to_string = new Lambda1() {
        public Object apply(Object exc) {
            return exc.toString();
        }
    };


    // Lists //

    public static Lambda2 cons = new Lambda2() {
        public Object apply(Object newElm, Object list) {
            return ((List<Object>) list).cons(newElm);
        }
    };

    public static Lambda1 hd = new Lambda1() {
        public Object apply(Object list) {
            return ((List<Object>) list).head();
        }
    };

    public static Lambda1 tl = new Lambda1() {
        public Object apply(Object list) {
            return ((List<Object>) list).tail();
        }
    };

    public static Lambda1 cons_ = new Lambda1() {
        public Object apply(Object list) {
            return list instanceof List && !((List<Object>) list).isEmpty();
        }
    };

    // Generic Functions are handled specially //
    // TODO: remove this todo when they're done


    // Vectors //

    /**
     * create a vector in the native platform
     */
    public static Lambda1 absvector = new Lambda1() {
        public Object apply(Object size) {
            int sz = ((Number)size).intValue();
            Object[] os = new Object[sz + 1];
            os[0] = size;
            for (int i=1; i<=sz; i++) {
                os[i] = FAIL;
            }
            return os;
        }
    };


    // Arithmetic //

    public static Lambda plus = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x + (Long) y;
            } else {
                return ((Number) x).doubleValue() + ((Number) y).doubleValue();
            }
        }
    };

    public static Lambda minus = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x - (Long) y;
            } else {
                return ((Number) x).doubleValue() - ((Number) y).doubleValue();
            }
        }
    };

    public static Lambda multiply = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x * (Long) y;
            } else {
                return ((Number) x).doubleValue() * ((Number) y).doubleValue();
            }
        }
    };

    public static Lambda divide = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                // TODO: it should return float unless divisor
                return (Long) x / (Long) y;
            } else {
                return ((Number) x).doubleValue() / ((Number) y).doubleValue();
            }
        }
    };


    static {
        try {
            for (Field f : Primitives.class.getFields()) {
                if (Lambda.class.isAssignableFrom(f.getType())) {
                    Environment.functions.put(ASMUtil.fromIdentifier(f.getName()), (Lambda) f.get(null));
                    System.out.println("put: " + ASMUtil.fromIdentifier(f.getName()) + ", " + (Lambda) f.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
