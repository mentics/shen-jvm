package com.stralos.shen.model;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.Primitives.*;
import static com.stralos.shen.model.Loc.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;
import static tomove.ArrayUtil.*;

import java.util.Arrays;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.stralos.asm.ASMUtil;
import com.stralos.shen.EvalContext;
import com.stralos.shen.Primitives;
import com.stralos.shen.VarInfo;


public class SList implements S {
    private static final long serialVersionUID = 8688805225174793587L;

    public S[] ss;


    public SList(S... ss) {
        this.ss = ss;
    }

    public Location getLocation() {
        return ss.length > 0 ? ss[0].getLocation() : Location.UNKNOWN;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('(');
        if (ss.length > 0) {
            b.append(ss[0].toString());
            for (int i = 1; i < ss.length; i++) {
                b.append(' ');
                b.append(ss[i].toString());
            }
        }
        b.append(')');
        return b.toString();
    }

    public void visit(EvalContext context, MethodVisitor mv) {
        if (ss[0] instanceof Symbol) {
            if (!handleSpecialForm(context, mv, ss) && !handleBuiltInFunction(context, mv)
                && !handleLocalSymbol(context, mv)) {
                // Couldn't find anything so default to it being a not-defined-yet user function
                // throw new RuntimeException("No function defined for symbol: " + ss[0]);
                handleUserFunction(context, mv);
            }
        } else if (ss[0] instanceof SList) {
            S[] args = tail(ss);
            ss[0].visit(context, mv);
            visitLoc(mv, ss[0]);
            ASMUtil.invokeToLambda(mv);
            visitLoc(mv, ss[0]);
            String lambdaType = LAMBDA_PATH_BASE + args.length;
            mv.visitTypeInsn(CHECKCAST, lambdaType);
            visitArgs(context, mv, args);
            visitLoc(mv, ss[0]);
            ASMUtil.trace(mv, "calling first-pos-slist: " + ss[0].toString());
            mv.visitMethodInsn(INVOKEVIRTUAL, lambdaType, LAMBDA_METHOD_NAME, signatureOfArity(args.length));
        }
    }

    private boolean handleLocalSymbol(EvalContext context, MethodVisitor mv) {
        VarInfo boundSymbol = context.getBoundSymbol(ss[0].toString());
        if (boundSymbol != null) {
            S[] args = tail(ss);
            String lambdaType = Primitives.LAMBDA_PATH_BASE + args.length;

            visitLoc(mv, ss[0]);
            mv.visitVarInsn(ALOAD, boundSymbol.index);

            mv.visitTypeInsn(CHECKCAST, lambdaType);
            visitArgs(context, mv, args);

            visitLoc(mv, ss[0]);
            ASMUtil.trace(mv, "calling local symbol: " + ss[0]);
            mv.visitMethodInsn(INVOKEVIRTUAL, lambdaType, Primitives.LAMBDA_METHOD_NAME, signatureOfArity(args.length));
            return true;
        }
        return false;
    }

    private boolean handleUserFunction(EvalContext context, MethodVisitor mv) {
        String funcName = ss[0].toString();
        S[] args = tail(ss);
        String lambdaType = Primitives.LAMBDA_PATH_BASE + args.length;

        visitLoc(mv, ss[0]);
        ASMUtil.invokeGetUserFunc(mv, funcName);
        visitLoc(mv, ss[0]);

        mv.visitTypeInsn(CHECKCAST, lambdaType);
        visitArgs(context, mv, args);

        visitLoc(mv, ss[0]);
        ASMUtil.trace(mv, "calling user function: " + ss[0]);
        mv.visitMethodInsn(INVOKEVIRTUAL, lambdaType, Primitives.LAMBDA_METHOD_NAME, signatureOfArity(args.length));
        return true;
    }

    private boolean handleBuiltInFunction(EvalContext context, MethodVisitor mv) {
        String funcName = ss[0].toString();
        try {
            Primitives.class.getField(toIdentifier(funcName));
            // Built in function
            S[] args = tail(ss);
            mv.visitFieldInsn(GETSTATIC, PRIMITIVES_PATH, toIdentifier(funcName), "L" + LAMBDA_PATH_BASE + ";");

            if (args.length > 0) {
                String lambdaType = LAMBDA_PATH_BASE + args.length;
                mv.visitTypeInsn(CHECKCAST, lambdaType);
                visitArgs(context, mv, args);
                ASMUtil.trace(mv, "calling built in function: " + ss[0]);
                mv.visitMethodInsn(INVOKEVIRTUAL,
                                   lambdaType,
                                   Primitives.LAMBDA_METHOD_NAME,
                                   signatureOfArity(args.length));
            } else {
                ASMUtil.trace(mv, "returning lambda as lambda: " + ss[0]);
            }
            return true;
        } catch (NoSuchFieldException | SecurityException e) {
            return false;
        }
    }

    // Special Forms //

    /**
     * These are special forms and functions that do not follow applicative evaluation for their parameters.
     */
    private boolean handleSpecialForm(EvalContext context, MethodVisitor mv, S[] s) {
        boolean handled = true;
        S[] args = tail(s);
        switch (s[0].toString()) {
        case "defun":
            handleDefun(context, mv, args);
            break;
        case "lambda":
            handleLambda(context, mv, args);
            break;
        case "let":
            handleLet(context, mv, args);
            break;
        case "eval-kl":
//        case "eval-without-macros": // backward compat
            handleEvalKl(context, mv, args);
            break;
        case "freeze":
            handleFreeze(context, mv, args);
            break;
        case "trap-error":
            handleTrapError(context, mv, args);
            break;
        case "if":
            handleIf(context, mv, args);
            break;
        case "and":
            handleAnd(context, mv, args);
            break;
        case "or":
            handleOr(context, mv, args);
            break;
        case "cond":
            handleCond(context, mv, args);
            break;
        default:
            handled = false;
        }
        if (handled) {
            ASMUtil.trace(mv, "handled special form: " + Arrays.toString(ss));
        }
        return handled;
    }


    private void handleDefun(EvalContext context, MethodVisitor mv, S[] args) {
        String funcName = args[0].toString();
        String[] paramNames = toStringArray(args[1]);
        S body = args[2];

        visitLoc(mv, ss[0]);
        loadGlobalFunctions(mv);

        context.putFunction(funcName, new FunctionInfo(paramNames));

        String newLambdaName = context.newLambdaName(args[0].toString());
        createLambdaClass(context.newChildContext(), new VarInfo[0], newLambdaName, body, paramNames);

        mv.visitLdcInsn(funcName);

        visitLoc(mv, ss[3]);
        mv.visitTypeInsn(NEW, newLambdaName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, newLambdaName, "<init>", constructorOfArity(0));
        visitLoc(mv, ss[1]);
        mv.visitMethodInsn(INVOKEINTERFACE,
                           "java/util/Map",
                           "put",
                           "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    }

    private void handleLambda(EvalContext context, MethodVisitor mv, S[] params) {
        String className = context.newLambdaName();

        // Capture the lexical scoped variables
        VarInfo[] vars = context.getScopedVars();

        visitLoc(mv, line(ss[0]));
        createLambdaClass(context.newChildContext(), vars, className, params[1], toStringArray(params[0]));
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        for (VarInfo var : vars) {
            var.visitLoad(mv);
        }
        visitLoc(mv, line(ss[0]));
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", constructorOfArity(vars.length));
    }

    private void handleLet(EvalContext context, MethodVisitor mv, S[] params) {
        String varName = params[0].toString();
        S value = params[1];
        S body = params[2];
        Label begin = new Label();
        Label end = new Label();

        visitLoc(mv, line(ss[0]));
        value.visit(context, mv);
        visitLoc(mv, line(ss[0]));

        int varOffset = context.getVarOffset();
        mv.visitVarInsn(ASTORE, varOffset);
        mv.visitLabel(begin);

        context.bindLocalVar(new VarInfo(varOffset, varName, context.uniqueValidFieldName(varName), begin, end));
        body.visit(context, mv);

        mv.visitLabel(end);
        context.unbindLocalVar(varName);
    }

    private void handleEvalKl(EvalContext context, MethodVisitor mv, S[] params) {
        visitLoc(mv, line(ss[0]));
        params[0].visit(context, mv);
        visitLoc(mv, line(ss[0]));
        mv.visitMethodInsn(INVOKESTATIC, Primitives.PRIMITIVES_PATH, "evalKl", signatureOfArity(1));
    }

    /**
     * A --> (lazy A)
     * creates a continuation
     */
    private void handleFreeze(EvalContext context, MethodVisitor mv, S[] params) {
        handleLambda(context, mv, new S[] { LList.NIL, params[0] });
    }

    /**
     * A --> (exception --> A) --> A
     * trap-error has to be handled specially
     * evaluates its first argument A; if it is not an exception returns the normal form, returns A else applies its
     * second argument to the exception
     */
    private void handleTrapError(EvalContext context, MethodVisitor mv, S[] args) {
        int varOffset = context.getVarOffset();

        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        Label l5 = new Label();

        context.bindLocalVar(new VarInfo(varOffset, "v1", "v1", l1, l5, false, "java/lang/Object"));
        context.bindLocalVar(new VarInfo(varOffset + 1, "e", "e", l4, l3, false, "java/lang/Throwable"));

        visitLoc(mv, line(ss[0]));
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
        mv.visitLabel(l0);
        mv.visitLineNumber(line(args[0]), l0);

        args[0].visit(context, mv); // mv.visitVarInsn(ALOAD, 0); // execute block
        visitLoc(mv, line(args[0]));

        mv.visitVarInsn(ASTORE, varOffset); // store v, the result

        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l2);
        mv.visitLineNumber(line(args[1]), l2);
        mv.visitVarInsn(ASTORE, varOffset + 1); // e
        mv.visitLabel(l4);

        args[1].visit(context, mv); // mv.visitVarInsn(ALOAD, 1); // catch block
        visitLoc(mv, line(args[1]));
        mv.visitTypeInsn(CHECKCAST, LAMBDA_PATH_BASE + 1);
        mv.visitVarInsn(ALOAD, varOffset + 1); // e

        mv.visitMethodInsn(INVOKEVIRTUAL,
                           LAMBDA_PATH_BASE + 1,
                           LAMBDA_METHOD_NAME,
                           "(Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, varOffset); // store v, the result

        mv.visitLabel(l3);
        mv.visitLineNumber(line(ss[0]), l3);
        mv.visitVarInsn(ALOAD, varOffset); // load v, the result
        mv.visitLabel(l5);
    }

    private void handleIf(EvalContext context, MethodVisitor mv, S[] args) {
        Label l0 = new Label();
        mv.visitLabel(l0);

        args[0].visit(context, mv);
        visitLoc(mv, line(args[0]));
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");

        Label l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(line(args[1]), l3);

        args[1].visit(context, mv);

        Label l4 = new Label();
        mv.visitLineNumber(line(args[1]), l4);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l2);
        mv.visitLineNumber(line(args[2]), l2);

        args[2].visit(context, mv);

        mv.visitLabel(l4);
    }

    private void handleAnd(EvalContext context, MethodVisitor mv, S[] args) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(args[0]), l0);

        args[0].visit(context, mv);
        visitLoc(mv, line(args[0]));

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);

        visitLoc(mv, line(args[1]));
        args[1].visit(context, mv);
        visitLoc(mv, line(args[1]));

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        mv.visitJumpInsn(IFEQ, l1);
        // TODO: line numbers in here?
        mv.visitInsn(ICONST_1);
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
    }

    private void handleOr(EvalContext context, MethodVisitor mv, S[] args) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(line(args[0]), l0);

        args[0].visit(context, mv);
        visitLoc(mv, line(args[0]));

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);

        visitLoc(mv, line(args[1]));
        args[1].visit(context, mv);
        visitLoc(mv, line(args[1]));

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        mv.visitJumpInsn(IFNE, l1);
        // TODO: line numbers in here?
        mv.visitInsn(ICONST_0);
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
    }

    private void handleCond(EvalContext context, MethodVisitor mv, S[] args) {
        visitLoc(mv, line(ss[0]));
        Label end = new Label();
        for (int i = 0; i < args.length; i++) {
            if (!(args[i] instanceof SList)) {
                throw new RuntimeException("Invalid cond form: " + args);
            }
            S[] childrenss = ((SList) args[i]).ss;
            Label skip = new Label();
            S cond = childrenss[0];
            S ifTrue = childrenss[1];

            visitLoc(mv, line(cond));
            cond.visit(context, mv);
            visitLoc(mv, line(cond));

            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            mv.visitJumpInsn(IFEQ, skip);

            visitLoc(mv, line(ifTrue));
            ifTrue.visit(context, mv);
            visitLoc(mv, line(ifTrue));
            mv.visitJumpInsn(GOTO, end);

            mv.visitLabel(skip);
            mv.visitLineNumber(line(ss[0]), skip);
        }
        ASMUtil.throwExc(mv, "No clause returned true in cond.");
        mv.visitLabel(end);
    }
}
