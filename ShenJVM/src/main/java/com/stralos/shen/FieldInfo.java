package com.stralos.shen;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

import com.stralos.asm.ASMUtil;

public class FieldInfo extends VarInfo {
    String className;

    public FieldInfo(String className, String name, String valid) {
        super(-1, name, valid, null, null);
        this.className = className;
    }

    @Override
    public void visitLoad(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0); // Load "this"
        mv.visitFieldInsn(GETFIELD, className, valid, "Ljava/lang/Object;");
    }
}
