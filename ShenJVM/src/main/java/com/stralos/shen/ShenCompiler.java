package com.stralos.shen;

import static org.objectweb.asm.Opcodes.*;

import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import com.stralos.asm.ASMUtil;
import com.stralos.lang.Lambda0;
import com.stralos.shen.model.S;

public class ShenCompiler {
    public static final String LAMBDA_METHOD_NAME = "apply";
    public static final String LAMBDA_PATH_BASE = "com/stralos/lang/Lambda";
    public static final String NEW_LAMBDA_PATH_BASE = "shen/lambda/ToRun";

    public static final String SYMBOL_PATH = "com/stralos/shen/model/Symbol";
    public static final String MODEL_PATH = "com/stralos/shen/model/Model";
    public static final String PRIMITIVES_PATH = "com/stralos/shen/Primitives";

    public static Lambda0 compile(S s) {
        return compile(new Environment(), s);
    }

    public static Lambda0 compile(Environment env, S s) {
        try {
            String fullName = "shen/eval/ToEvaluate" + env.nextLambdaId();
            EvalContext context = env.newEvalContext();

            Map<String, byte[]> classes = ASMUtil
                    .createLambdaClass(context, new VarInfo[0], fullName, s, new String[0]);

            String clName = fullName.replace('/', '.');

            DirectClassLoader dcl = new DirectClassLoader(env, classes);

            Class<?> cl = dcl.loadClass(clName);
            return (Lambda0) cl.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] makeLambda(S s, String fullName, EvalContext context) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, fullName, null, LAMBDA_PATH_BASE + 0, null);
        setupConstructor(fullName, cw);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, LAMBDA_METHOD_NAME, "()Ljava/lang/Object;", null, null);
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

        context.putClass(fullName.replace('/', '.'), byteArray);
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
}
