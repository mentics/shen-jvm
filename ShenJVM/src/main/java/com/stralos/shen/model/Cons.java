package com.stralos.shen.model;

import static com.stralos.shen.model.Loc.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.asm.ASMUtil;
import com.stralos.lang.Lang;
import com.stralos.shen.EvalContext;
import com.stralos.shen.parser.FileLocation;

import fj.F;


public class Cons implements S {
    private static final long serialVersionUID = 8688805225174793587L;

    private static final String CONS_PATH = "com/stralos/shen/model/Cons";

    public static final Cons NIL = new Cons(new FileLocation("Cons.java", 27, 31), null, null);


    public final Object head;

    public final Object tail;

    private Location loc;


    public Cons(Location loc, Object head, Object tail) {
        if (loc == null) {
            new Throwable().printStackTrace();
        }
        this.head = head;
        this.tail = tail;
        this.loc = loc;
    }

    public Location getLocation() {
        return loc;
    }

    public void visit(EvalContext context, MethodVisitor mv) {

        if (this == NIL) {
            mv.visitFieldInsn(GETSTATIC, CONS_PATH, "NIL", "Lcom/stralos/shen/model/Cons;");
            return;
        }

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(loc), l0);

        // FileLocation for the cons
        ASMUtil.visitCreateFileLocation(mv, loc);

        // TODO: symbols must stay just a symbol? or does it "just work"?
        toS(head).visit(context, mv);
        toS(tail).visit(context, mv);

        mv.visitTypeInsn(NEW, CONS_PATH);
        mv.visitMethodInsn(INVOKESPECIAL,
                           CONS_PATH,
                           "<init>",
                           "(Lcom/stralos/shen/model/Location;Ljava/lang/Object;Ljava/lang/Object;)V");


        // int len = list.length();
        // mv.visitIntInsn(BIPUSH, len);
        // mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        // int i = 0;
        // for (Object o : list) {
        // mv.visitInsn(DUP);
        // mv.visitIntInsn(BIPUSH, i++);
        // // TODO: symbols must stay just a symbol? or does it "just work"?
        // toS(o).visit(context, mv);
        // mv.visitInsn(AASTORE);
        // }
        // mv.visitMethodInsn(INVOKESTATIC,
        // LLIST_PATH,
        // "list",
        // "(Lcom/stralos/shen/model/Location;[Ljava/lang/Object;)Lcom/stralos/shen/model/LList;");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof Cons) {
            Cons o = (Cons) other;
            return Lang.equals(this.head, this.head) && Lang.equals(this.tail, o.tail);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (this == NIL) {
            return "[]";
        } else if (tail instanceof Cons) {
            return "[" + head.toString() + " " + tail.toString() + "]";
        } else {
            return "[" + head.toString() + " | " + tail.toString() + "]";
        }
        // String res = "";
        // if (list.length() > 0) {
        // StringBuilder in = new StringBuilder();
        // Iterator<Object> it = list.iterator();
        // in.append(it.next().toString());
        // while (it.hasNext()) {
        // in.append(", ");
        // in.append(it.next().toString());
        // }
        // res = in.toString();
        // }
        // return "[" + res + "]";
    }

    public Object tail() {
        if (head == null && tail == null) {
            return Cons.NIL;
        } else {
            return tail;
        }
    }

    public Object head() {
        return head;
    }

    public <A> List<A> forEach(F<Object, A> f) {
        List<A> result = new ArrayList<>();
        if (head == null) {
            return result;
        }
        Object iterate = this;
        do {
            Object o = iterate instanceof Cons ? ((Cons) iterate).head : iterate;
            result.add(f.f(o));
            if (iterate instanceof Cons) {
                iterate = ((Cons) iterate).tail;
            } else {
                break;
            }
        } while (iterate != Cons.NIL);
        return result;
    }
}
