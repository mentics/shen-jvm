package com.stralos.asm;

import static com.stralos.shen.Environment.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.lang.Lambda;
import com.stralos.shen.Environment;
import com.stralos.shen.EvalContext;
import com.stralos.shen.FieldInfo;
import com.stralos.shen.Primitives;
import com.stralos.shen.VarInfo;
import com.stralos.shen.model.S;
import com.stralos.shen.model.Symbol;


public class ASMUtil {
    public static String constructorOfArity(int arity) {
        StringBuilder b = new StringBuilder(arity * 20);
        b.append('(');
        for (int i = 0; i < arity; i++) {
            b.append("Ljava/lang/Object;");
        }
        b.append(")V");
        return b.toString();
    }

    public static String signatureOfArity(int arity) {
        // It's a little extra big. 20 was higher than needful
        StringBuilder b = new StringBuilder(arity * 20);
        b.append('(');
        for (int i = 0; i < arity; i++) {
            b.append("Ljava/lang/Object;");
        }
        b.append(")Ljava/lang/Object;");
        return b.toString();
    }

    public static void loadGlobalFunctions(MethodVisitor mv) {
        mv.visitFieldInsn(GETSTATIC, ENV_PATH, "functions", "Ljava/util/Map;");
    }

    /**
     * @param context for just this lambda class. Don't use the same as the parent.
     */
    public static Map<String, byte[]> createLambdaClass(EvalContext context, VarInfo[] vars, String className, S body,
                                                        String[] params) {
        ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, Primitives.LAMBDA_PATH_BASE + params.length, null);
        // System.err.println("creating lambda: " + className);
        cv.visitSource("com/stralos/shen/Source.java", null);

        for (int i = 0; i < vars.length; i++) {
            FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL + ACC_SYNTHETIC,
                                            "val$" + vars[i].name,
                                            "Ljava/lang/Object;",
                                            null,
                                            null);
            fv.visitEnd();
        }

        MethodVisitor mv;

        {
            mv = cv.visitMethod(ACC_PUBLIC, "<init>", constructorOfArity(vars.length), null, null);
            mv.visitCode();
            Label begin = new Label();
            mv.visitLabel(begin);
            mv.visitLineNumber(1, begin);
            for (int i = 0; i < vars.length; i++) {
                mv.visitVarInsn(ALOAD, 0); // load "this"
                mv.visitVarInsn(ALOAD, i + 1); // load the value
                mv.visitFieldInsn(PUTFIELD, className, "val$" + vars[i].name, "Ljava/lang/Object;");
                context.push(new FieldInfo(className, vars[i].name));
            }
            Label construct = new Label();
            mv.visitLabel(construct);
            mv.visitLineNumber(2, begin);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, Primitives.LAMBDA_PATH_BASE + params.length, "<init>", "()V");
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(3, l1);
            mv.visitInsn(RETURN);
            Label end = new Label();
            mv.visitLabel(end);

            mv.visitLocalVariable("this", "L" + className + ";", null, begin, end, 0);
            for (int i = 0; i < vars.length; i++) {
                mv.visitLocalVariable("val" + i, "Ljava/lang/Object;", null, begin, end, i + 1);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cv.visitMethod(ACC_PUBLIC, Primitives.LAMBDA_METHOD_NAME, signatureOfArity(params.length), null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            context.push(1); // for "this"
            int varOffset = context.getVarOffset();
            for (int i = 0; i < params.length; i++) {
                context.push(1, new VarInfo(i + varOffset, params[i], l0, l1));
            }
            mv.visitLabel(l0);

            body.visit(context, mv);

            mv.visitInsn(ARETURN);
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
            for (VarInfo var : context.getBoundSymbols().values()) {
                // TODO: make less ugly
                if (!(var instanceof FieldInfo)) {
                    mv.visitLocalVariable(var.name, "Ljava/lang/Object;", null, var.beginLabel, var.endLabel, var.index);
                }
            }

            mv.visitMaxs(0, 0);
            // Don't need context.pop because end of method
            mv.visitEnd();
        }
        cv.visitEnd();
        context.putClass(className.replace('/', '.'), cv.toByteArray());
        return context.getClasses();
    }

    /**
     * Converts all characters that are invalid in a Java identifier to _
     */
    public static String toIdentifier(String string) {
        // TODO: do more efficiently

        String ret;
        switch (string) {
        case "=":
            ret = "equal";
            break;
        case "+":
            ret = "plus";
            break;
        case "-":
            ret = "minus";
            break;
        case "*":
            ret = "multiply";
            break;
        case "/":
            ret = "divide";
            break;
        case "<":
            ret = "lessThan";
            break;
        case ">":
            ret = "greaterThan";
            break;
        case "<=":
            ret = "lessThanOrEqual";
            break;
        case ">=":
            ret = "greaterThanOrEqual";
            break;
        case "if":
            ret = "if_";
            break;
        default:
            ret = toValidJava(string);
        }
        return ret;
    }

    public static String fromIdentifier(String string) {
        // TODO: do more efficiently

        String ret;
        switch (string) {
        case "equal":
            ret = "=";
            break;
        case "plus":
            ret = "+";
            break;
        case "minus":
            ret = "-";
            break;
        case "multiply":
            ret = "*";
            break;
        case "divide":
            ret = "/";
            break;
        case "lessThan":
            ret = "<";
            break;
        case "greaterThan":
            ret = ">";
            break;
        case "lessThanOrEqual":
            ret = "<=";
            break;
        case "greaterThanOrEqual":
            ret = ">=";
            break;
        case "if_":
            ret = "if";
            break;
        default:
            // throw new RuntimeException("Invalid parameter to fromIdentifier: " + string);
            ret = string;
        }
        return ret;
    }

    public static String toValidJava(String string) {
        String ret;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                b.append('_');
            } else {
                b.append(c);
            }
        }
        ret = b.toString();
        return ret;
    }

    public static void visitArgs(EvalContext context, MethodVisitor mv, S[] args) {
        // for (int i = args.length - 1; i >= 0; i--) {
        for (int i = 0; i < args.length; i++) {
            args[i].visit(context, mv);
        }
    }

    public static void invokeToLambda(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC,
                           ASMUtil.class.getName().replace('.', '/'),
                           "toLambda",
                           "(Ljava/lang/Object;)Lcom/stralos/lang/Lambda;");
    }

    /**
     * Either a lambda already or a symbol that should be associated with a function so should be in global functions or
     * Primitives.
     */
    public static Lambda toLambda(Object o) {
        Lambda result;
        if (o instanceof Lambda) {
            result = (Lambda) o;
        } else if (o instanceof Symbol) {
            result = Environment.functions.get(o.toString());
            if (result == null) {
                throw new RuntimeException("Unknown symbol returned by first position s-expression: " + o);
            }
        } else {
            throw new RuntimeException("Invalid type in first position: " + o.getClass());
        }
        return result;
    }

    public static void throwExc(MethodVisitor mv, String msg) {
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(msg);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
    }
}
