package com.stralos.shen.model;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.model.Loc.*;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;
import com.stralos.shen.Primitives;
import com.stralos.shen.VarInfo;


public class Symbol extends Atom {
    private static final long serialVersionUID = -8583150252617385433L;

    private String label;


    public Symbol(String label) {
        this(label, null);
    }

    public Symbol(String label, Location loc) {
        super(loc);
        this.label = label;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        VarInfo var = context.getBoundSymbol(label);
        if (var != null) {
            var.visitLoad(mv);
        } else {
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(line(loc), l0);
            mv.visitLdcInsn(label);

            visitCreateFileLocation(mv, loc);
            mv.visitMethodInsn(INVOKESTATIC, Primitives.MODEL_PATH, "symbol", "(Ljava/lang/String;L"
                                                                              + LOCATION_PATH + ";)L"
                                                                              + Primitives.SYMBOL_PATH + ";");
        }
    }

    public int hashCode() {
        return label.hashCode();
    }

    public boolean equals(Object o) {
        return o != null && o instanceof Symbol && ((Symbol) o).label.equals(label);
    }

    public String toString() {
        return label;
    }
}
