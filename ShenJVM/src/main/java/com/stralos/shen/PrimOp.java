package com.stralos.shen;

import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.model.S;

public abstract class PrimOp {
    public final String[] params;

    PrimOp(String[] params) {
        this.params = params;
    }

    public abstract void visit(EvalContext context, MethodVisitor mv, S[] params);
}
