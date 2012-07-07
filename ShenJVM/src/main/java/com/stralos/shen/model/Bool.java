package com.stralos.shen.model;

import static com.stralos.shen.model.Loc.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.Serializable;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;

public class Bool extends Atom implements Serializable {
    private static final long serialVersionUID = -2420139665474971094L;

    private boolean value;

    public Bool(boolean value) {
        this(value, null);
    }
    
    public Bool(boolean value, Location loc) {
        super(loc);
        this.value = value;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(loc), l0);
        mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", value ? "TRUE" : "FALSE", "Ljava/lang/Boolean;");
    }
    
    public String toString() {
        return Boolean.toString(value);
    }
}
