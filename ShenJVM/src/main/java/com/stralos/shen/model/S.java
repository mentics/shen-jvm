package com.stralos.shen.model;

import java.io.Serializable;

import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;

public interface S extends Serializable {
    void visit(EvalContext context, MethodVisitor mv);

    Location getLocation();
}
