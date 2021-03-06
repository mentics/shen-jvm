package com.stralos.shen.model;

import static com.stralos.shen.model.Loc.*;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;

public class Flot extends Number {
    private static final long serialVersionUID = 2016561068074216978L;
    
    private Double num;

    public Flot(double num) {
        this(num, null);
    }

    public Flot(double num, Location loc) {
        super(loc);
        this.num = num;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(loc), l0);
        mv.visitLdcInsn(Double.valueOf(num));
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
    }
    
    public String toString() {
        return Double.toString(num);
    }
}
