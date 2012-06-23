package com.stralos.shen;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

public class FieldInfo extends VarInfo {
    String className;

    public FieldInfo(String className, String name) {
        super(-1, name, null, null);
        this.className = className;
    }

    @Override
    public void visitLoad(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0); // Load "this"
        mv.visitFieldInsn(GETFIELD, className, "val$" + name, "Ljava/lang/Object;");
    }
}
