package com.stralos.shen.model;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.Environment.*;
import static com.stralos.shen.ShenCompiler.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import tomove.ArrayUtil;

import com.stralos.shen.EvalContext;
import com.stralos.shen.PrimOp;
import com.stralos.shen.Primitives;
import com.stralos.shen.VarInfo;

public class SList implements S {
    private static final long serialVersionUID = 8688805225174793587L;

    S[] ss;

    public SList(S... ss) {
        this.ss = ss;
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        if (ss[0] instanceof Symbol) {
            if (!handleSpecialForm(context, mv) && !handleUserFunction(context, mv)
                    && !handleBuiltInFunction(context, mv)) {
                // Couldn't find anything so throw error
                throw new RuntimeException("No function defined for symbol: " + ss[0]);
            }
        } else if (ss[0] instanceof SList) {
            throw new UnsupportedOperationException();
        }
    }

    private boolean handleSpecialForm(EvalContext context, MethodVisitor mv) {
        boolean handled = false;
        switch (ss[0].toString()) {
        case "defun":
            handled = true;
            handleDefun(context, mv);
            break;
        }
        return handled;
    }

    private boolean handleUserFunction(EvalContext context, MethodVisitor mv) {
        FunctionInfo f = context.getFunction(ss[0].toString());
        if (f != null) {
            // User defined function
            if (f.params.length > ss.length - 1) {
                // Partial application
                // TODO
                throw new UnsupportedOperationException();
            } else {
                // Full application
                String lambdaType = LAMBDA_PATH_BASE + f.params.length;

                mv.visitFieldInsn(GETSTATIC, ENV_PATH, "functions", "Ljava/util/Map;");
                mv.visitLdcInsn(ss[0].toString());
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, lambdaType);

                for (int i = ss.length - 1; i > 0; i--) {
                    ss[i].visit(context, mv);
                }

                mv.visitMethodInsn(INVOKEVIRTUAL, lambdaType, LAMBDA_METHOD_NAME, signatureOfArity(f.params.length));
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean handleBuiltInFunction(EvalContext context, MethodVisitor mv) {
        PrimOp p = Primitives.get(((Symbol) ss[0]).toString());
        if (p != null) {
            // Built in function
            if (p.params.length > ss.length - 1) {
                // Partial application
                throw new UnsupportedOperationException();
            } else {
                // Full application
                p.visit(context, mv, ArrayUtil.tail(ss));
            }
            return true;
        } else {
            return false;
        }
    }

    private void handleDefun(EvalContext context, MethodVisitor mv) {
        String funcName = ss[1].toString();
        String[] paramNames = toStringArray(ss[2]);
        S body = ss[3];

        loadGlobalFunctions(mv);

        String newLambdaName = context.newLambdaName();
        context.putClasses(createLambdaClass(context.newChildContext(), new VarInfo[0], newLambdaName, body, paramNames));

        mv.visitLdcInsn(funcName);

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, newLambdaName);
        mv.visitInsn(DUP);

        mv.visitMethodInsn(INVOKESPECIAL, newLambdaName, "<init>", constructorOfArity(0));

        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        
        context.putFunction(funcName, new FunctionInfo(paramNames));
    }
}
