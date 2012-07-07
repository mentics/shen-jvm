package com.stralos.shen.model;

import static com.stralos.shen.model.Loc.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;


public class Str extends Atom {
    private static final long serialVersionUID = -3281833629359320310L;

    private String str;


    public Str(String str) {
        this(str, null);
    }

    public Str(String str, Location loc) {
        super(loc);
        this.str = str;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(loc), l0);
        mv.visitLdcInsn(str);
    }

    public String toString() {
        return '"' + str + '"';
    }
}
