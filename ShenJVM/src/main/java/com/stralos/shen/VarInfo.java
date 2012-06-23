package com.stralos.shen;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class VarInfo {
    public final int index;
    public final String name;
    public final Label beginLabel;
    public final Label endLabel;

    public VarInfo(int index, String name, Label beginLabel, Label endLabel) {
        this.index = index;
        this.name = name;
        this.beginLabel = beginLabel;
        this.endLabel = endLabel;
    }

    public void visitLoad(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, index);
    }
}
