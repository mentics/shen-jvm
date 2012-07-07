package com.stralos.shen;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;


public class VarInfo {
    public final int index;
    public final String name; // name to match in kl source
    public final String valid; // valid name for java stuff
    public final Label beginLabel;
    public final Label endLabel;
    public String typePath;
    public boolean capture; // whether to transmit to lambdas

    public VarInfo(int index, String name, String valid, Label beginLabel, Label endLabel) {
        this(index, name, valid, beginLabel, endLabel, true, "java/lang/Object");
    }
    
    public VarInfo(int index, String name, String valid, Label beginLabel, Label endLabel, boolean capture, String typePath) {
        this.index = index;
        this.name = name;
        this.valid = valid;
        this.beginLabel = beginLabel;
        this.endLabel = endLabel;
        this.capture = capture;
        this.typePath = typePath;
    }

    public void visitLoad(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, index);
    }
    
    public String toString() {
        return name+", "+index+", "+beginLabel+", "+endLabel+", "+typePath;
    }
}
