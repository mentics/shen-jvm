package com.stralos.asm;

import static com.stralos.shen.Environment.*;
import static com.stralos.shen.ShenCompiler.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.Environment;
import com.stralos.shen.EvalContext;
import com.stralos.shen.FieldInfo;
import com.stralos.shen.ShenCompiler;
import com.stralos.shen.VarInfo;
import com.stralos.shen.model.S;

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
        cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null, ShenCompiler.LAMBDA_PATH_BASE + params.length, null);
        System.err.println("creating lambda: " + className);
        cv.visitSource("com/stralos/shen/Source.java", null);
    
        for (int i = 0; i < vars.length; i++) {
            FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL + ACC_SYNTHETIC, "val$" + vars[i].name,
                    "Ljava/lang/Object;", null, null);
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
            mv.visitMethodInsn(INVOKESPECIAL, ShenCompiler.LAMBDA_PATH_BASE + params.length, "<init>", "()V");
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
            mv = cv.visitMethod(ACC_PUBLIC, LAMBDA_METHOD_NAME, signatureOfArity(params.length), null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            context.push(1); // for "this"
            int varOffset = context.getVarOffset();
            for (int i = 0 ; i < params.length; i++) {
                context.push(1, new VarInfo(i+varOffset, params[i], l0, l1));
            }
            mv.visitLabel(l0);
    
            body.visit(context, mv);
    
            mv.visitInsn(ARETURN);
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
            for (VarInfo var : context.getBoundSymbols().values()) {
                mv.visitLocalVariable(var.name, "Ljava/lang/Object;", null, var.beginLabel, var.endLabel, var.index);
            }
    
            mv.visitMaxs(0, 0);
            // Don't need context.pop because end of method
            mv.visitEnd();
        }
        cv.visitEnd();
        context.putClass(className.replace('/', '.'), cv.toByteArray());
        return context.getClasses();
    }
}
