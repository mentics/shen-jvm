package com.stralos.shen.model;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.model.Loc.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.Iterator;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.asm.ASMUtil;
import com.stralos.shen.EvalContext;
import com.stralos.shen.parser.FileLocation;

import fj.data.List;


public class LList implements S {
    private static final String LLIST_PATH = "com/stralos/shen/model/LList";

    private static final String FILE_LOCATION_PATH = "com/stralos/shen/parser/FileLocation";

    private static final long serialVersionUID = 8688805225174793587L;

    public static final LList NIL = new LList(new FileLocation("LList.java", 21, 31));


    public static LList list(Object... os) {
        return new LList(Location.UNKNOWN, os);
    }

    public static LList list(Location loc, Object... os) {
        return new LList(loc, os);
    }


    private final List<Object> list;

    private Location loc;


    public LList(Location loc, Object... ss) {
        if (loc == null) {
            new Throwable().printStackTrace();
        }
        this.loc = loc;
        list = List.list(ss);
    }

    public LList(Location loc, List<Object> list) {
        if (loc == null) {
            new Throwable().printStackTrace();
        }
        this.loc = loc;
        this.list = list;
    }

    public Location getLocation() {
        return loc;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        int len = list.length();

        if (this == NIL) {
            mv.visitFieldInsn(GETSTATIC, LLIST_PATH, "NIL", "Lcom/stralos/shen/model/LList;");
            return;
        }

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(loc), l0);


        // FileLocation for the LList
        mv.visitTypeInsn(NEW, FILE_LOCATION_PATH);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(path(loc));
        mv.visitIntInsn(BIPUSH, line(loc));
        mv.visitIntInsn(BIPUSH, column(loc));
        mv.visitMethodInsn(INVOKESPECIAL, FILE_LOCATION_PATH, "<init>", "(Ljava/lang/String;II)V");

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
        mv.visitMethodInsn(INVOKESTATIC,
                           LLIST_PATH,
                           "list",
                           "(Lcom/stralos/shen/model/Location;[Ljava/lang/Object;)Lcom/stralos/shen/model/LList;");

    }

    public List<Object> toList() {
        return list;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LList) {
            LList o = (LList) other;
            return this.list != null && this.list.equals(o.list);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String res = "";
        if (list.length() > 0) {
            StringBuilder in = new StringBuilder();
            Iterator<Object> it = list.iterator();
            in.append(it.next().toString());
            while (it.hasNext()) {
                in.append(", ");
                in.append(it.next().toString());
            }
            res = in.toString();
        }
        return "[" + res + "]";
    }

    public Object tail() {
        if (list.length() == 1) {
            return LList.NIL;
        } else {
            return Model.list(loc, list.tail());
        }
    }

    public Object head() {
        return list.head();
    }
}
