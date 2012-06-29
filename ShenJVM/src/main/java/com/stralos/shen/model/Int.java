package com.stralos.shen.model;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;

public class Int extends Number {
    private static final long serialVersionUID = 975576818519372843L;

    private long i;

    public Int(long i) {
        this.i = i;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(21, l0);
        mv.visitLdcInsn(Long.valueOf(i));
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
    }
    
    public String toString() {
        return Long.toString(i);
    }
}
