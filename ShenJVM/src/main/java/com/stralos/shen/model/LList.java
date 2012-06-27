package com.stralos.shen.model;

import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.shen.EvalContext;

import fj.data.List;

public class LList implements S {
    private static final long serialVersionUID = 8688805225174793587L;

    public static LList list(Object... os) {
        return new LList(os);
    }

    private final List<Object> list;

    public LList(Object... ss) {
        list = List.list(ss);
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        int len = list.length();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(37, l0);
        mv.visitIntInsn(BIPUSH, len);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        int i = 0;
        for (Object o : list) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i++);
            // TODO: symbols must stay just a symbol? or does it "just work"?
            toS(o).visit(context, mv);
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKESTATIC, "com/stralos/shen/model/LList", "list",
                "([Ljava/lang/Object;)Lcom/stralos/shen/model/LList;");
    }

    public List<Object> toList() {
        return list;
    }
}
