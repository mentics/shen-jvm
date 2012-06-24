package com.stralos.shen;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;
import static tomove.ArrayUtil.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.lang.Lambda1;
import com.stralos.lang.Lambda2;
import com.stralos.shen.model.S;

import fj.data.List;

public class Primitives {
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

    
    // Arithmetic //
    
    public static Lambda2 plus = new Lambda2() {
        public Object apply(Object x, Object y) {
            if (x instanceof Long && y instanceof Long) {
                return (Long) x + (Long) y;
            } else {
                return ((Number) x).doubleValue() + ((Number) y).doubleValue();
            }
        }
    };
    
    
    

    private static Map<String, PrimOp> ops = new HashMap<>();
    static {
        ops.put("+", new PrimOp(array("X0", "X1")) {
            public void visit(EvalContext context, MethodVisitor mv, S[] params) {
                if (params.length < 2) {
                    // TODO: return lambda
                }
                for (int i = 0; i < params.length; i++) {
                    params[i].visit(context, mv);
                }
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(31, l0);
                mv.visitMethodInsn(INVOKESTATIC, ShenCompiler.PRIMITIVES_PATH, "plus",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            }
        });

        /**
         * lambda lambda function X : A >> Y : B;
         * (lambda X Y) : (A --> B);
         */
        ops.put("lambda", new PrimOp(array("Params", "Body")) {
            public void visit(EvalContext context, MethodVisitor mv, S[] def) {
                String className = context.newLambdaName();

                // Capture the lexical scoped variables
                Collection<VarInfo> vars = context.getBoundSymbols().values();

                Map<String, byte[]> newClasses = createLambdaClass(context.newChildContext(),
                        vars.toArray(new VarInfo[vars.size()]), className, def[1], toStringArray(def[0]));
                context.putClasses(newClasses);
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitTypeInsn(NEW, className);
                mv.visitInsn(DUP);

                for (VarInfo var : vars) {
                    mv.visitVarInsn(ALOAD, var.index);
                }
                mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", constructorOfArity(vars.size()));
            }
        });
    }

    public static PrimOp get(String opName) {
        return ops.get(opName);
    }

    public static Object plus(Object x, Object y) {
        if (x instanceof Long && y instanceof Long) {
            return (Long) x + (Long) y;
        } else {
            return ((Number) x).doubleValue() + ((Number) y).doubleValue();
        }
    }
}
