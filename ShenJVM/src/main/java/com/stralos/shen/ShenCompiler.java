package com.stralos.shen;

import static com.stralos.asm.ASMUtil.*;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import com.stralos.lang.Lambda0;
import com.stralos.shen.model.S;


public class ShenCompiler {
    public static Lambda0 compile(S s) {
        return compile(Environment.theEnvironment(), s);
    }

    public static Lambda0 compile(Environment env, S s) {
        return env.run("shen/eval/ToEvaluate", s);
    }

    public static byte[] makeLambda(S s, String fullName, EvalContext context) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, fullName, null, Primitives.LAMBDA_PATH_BASE + 0, null);
        setupConstructor(fullName, cw);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, Primitives.LAMBDA_METHOD_NAME, "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(7, l0);

        s.visit(context, mv);

        mv.visitInsn(ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "L" + fullName + ";", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        byte[] byteArray = cw.toByteArray();
        ClassReader cr = new ClassReader(byteArray);
        cr.accept(new CheckClassAdapter(new ClassWriter(0)), 0);

        context.addClass(fullName.replace('/', '.'), byteArray);
        return byteArray;
    }

    static void setupConstructor(String path, ClassWriter cv) {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "L" + path + ";", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }


    public static final String COMPILER_METHOD_SIGNATURE = "(Lcom/stralos/shen/model/S;)Lcom/stralos/lang/Lambda0;";
    public static final String LLIST_PATH = "com/stralos/shen/model/LList";
}
