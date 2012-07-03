package com.stralos.shen.model;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.Primitives.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;
import static tomove.ArrayUtil.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.stralos.asm.ASMUtil;
import com.stralos.shen.EvalContext;
import com.stralos.shen.Primitives;
import com.stralos.shen.VarInfo;


public class SList implements S {
    private static final long serialVersionUID = 8688805225174793587L;

    S[] ss;


    public SList(S... ss) {
        this.ss = ss;
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
            if (!handleSpecialForm(context, mv, ss) && !handleBuiltInFunction(context, mv)) {
                // Couldn't find anything so default to it being a not-defined-yet user function
                // throw new RuntimeException("No function defined for symbol: " + ss[0]);
                handleUserFunction(context, mv);
            }
        } else if (ss[0] instanceof SList) {
            S[] args = tail(ss);
            ss[0].visit(context, mv);
            ASMUtil.invokeToLambda(mv);
            String lambdaType = LAMBDA_PATH_BASE + args.length;
            mv.visitTypeInsn(CHECKCAST, lambdaType);
            visitArgs(context, mv, args);
            mv.visitMethodInsn(INVOKEVIRTUAL, lambdaType, LAMBDA_METHOD_NAME, signatureOfArity(args.length));
        }
    }

    private boolean handleUserFunction(EvalContext context, MethodVisitor mv) {
        String funcName = ss[0].toString();
        S[] args = tail(ss);
        String lambdaType = Primitives.LAMBDA_PATH_BASE + args.length;

        ASMUtil.invokeUserFunc(mv, funcName);

//        loadGlobalFunctions(mv);
//        mv.visitLdcInsn(funcName);
//        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, lambdaType);
        visitArgs(context, mv, args);
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
                mv.visitMethodInsn(INVOKEVIRTUAL,
                                   lambdaType,
                                   Primitives.LAMBDA_METHOD_NAME,
                                   signatureOfArity(args.length));
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
        S[] params = tail(s);
        switch (s[0].toString()) {
        case "defun":
            handleDefun(context, mv, params);
            break;
        case "lambda":
            handleLambda(context, mv, params);
            break;
        case "let":
            handleLet(context, mv, params);
            break;
        case "eval-kl":
            handleEvalKl(context, mv, params);
            break;
        case "freeze":
            handleFreeze(context, mv, params);
            break;
        case "trap-error":
            handleTrapError(context, mv, params);
            break;
        case "if":
            handleIf(context, mv, params);
            break;
        case "and":
            handleAnd(context, mv, params);
            break;
        case "or":
            handleOr(context, mv, params);
            break;
        case "cond":
            handleCond(context, mv, params);
            break;
        default:
            handled = false;
        }
        return handled;
    }


    private void handleDefun(EvalContext context, MethodVisitor mv, S[] params) {
        String funcName = params[0].toString();
        String[] paramNames = toStringArray(params[1]);
        S body = params[2];

        loadGlobalFunctions(mv);

        context.putFunction(funcName, new FunctionInfo(paramNames));

        String newLambdaName = context.newLambdaName();
        createLambdaClass(context.newChildContext(), new VarInfo[0], newLambdaName, body, paramNames);

        mv.visitLdcInsn(funcName);

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, newLambdaName);
        mv.visitInsn(DUP);

        mv.visitMethodInsn(INVOKESPECIAL, newLambdaName, "<init>", constructorOfArity(0));

        mv.visitMethodInsn(INVOKEINTERFACE,
                           "java/util/Map",
                           "put",
                           "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    }

    private void handleLambda(EvalContext context, MethodVisitor mv, S[] params) {
        String className = context.newLambdaName();

        // Capture the lexical scoped variables
        VarInfo[] vars = context.getScopedVars();

        createLambdaClass(context.newChildContext(), vars, className, params[1], toStringArray(params[0]));
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);

        for (VarInfo var : vars) {
            var.visitLoad(mv);
        }
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", constructorOfArity(vars.length));
    }

    private void handleLet(EvalContext context, MethodVisitor mv, S[] params) {
        String varName = params[0].toString();
        S value = params[1];
        S body = params[2];
        Label begin = new Label();
        Label end = new Label();

        value.visit(context, mv);

        int varOffset = context.getVarOffset();
        mv.visitVarInsn(ASTORE, varOffset);
        mv.visitLabel(begin);

        context.bindLocalVar(new VarInfo(varOffset, varName, begin, end));
        body.visit(context, mv);

        mv.visitLabel(end);
        context.unbindLocalVar(varName);
    }

    private void handleEvalKl(EvalContext context, MethodVisitor mv, S[] params) {
        params[0].visit(context, mv);
        mv.visitMethodInsn(INVOKESTATIC, Primitives.PRIMITIVES_PATH, "evalKl", signatureOfArity(1));
    }

    /**
     * A --> (lazy A)
     * creates a continuation
     */
    private void handleFreeze(EvalContext context, MethodVisitor mv, S[] params) {
        handleLambda(context, mv, new S[] { Model.nil, params[0] });
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

        // mv.visitLocalVariable("v", "Ljava/lang/Object;", null, l1, l2, 2); // 2 == varOffset + 0
        context.bindLocalVar(new VarInfo(varOffset, "v1", l1, l5, false, "java/lang/Object"));
        // context.push(1, new VarInfo(varOffset, "v1", l1, l2, false, "java/lang/Object"));

        // mv.visitLocalVariable("v", "Ljava/lang/Object;", null, l3, l5, 2); // 2 == varOffset + 0
        // context.push(1, new VarInfo(varOffset, "v2", l3, l5, false, "java/lang/Object"));

        // mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l4, l3, 3); // 3 == varOffset+1
        context.bindLocalVar(new VarInfo(varOffset + 1, "e", l4, l3, false, "java/lang/Throwable"));

        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
        mv.visitLabel(l0);
        mv.visitLineNumber(50, l0);

        args[0].visit(context, mv); // mv.visitVarInsn(ALOAD, 0); // execute block
        mv.visitVarInsn(ASTORE, varOffset); // store v, the result

        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l2);
        mv.visitLineNumber(51, l2);
        // mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
        mv.visitVarInsn(ASTORE, varOffset + 1); // e
        mv.visitLabel(l4);
        mv.visitLineNumber(52, l4);

        args[1].visit(context, mv); // mv.visitVarInsn(ALOAD, 1); // catch block
        mv.visitTypeInsn(CHECKCAST, LAMBDA_PATH_BASE + 1);
        mv.visitVarInsn(ALOAD, varOffset + 1); // e

        mv.visitMethodInsn(INVOKEVIRTUAL,
                           LAMBDA_PATH_BASE + 1,
                           LAMBDA_METHOD_NAME,
                           "(Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, varOffset); // store v, the result

        mv.visitLabel(l3);
        mv.visitLineNumber(54, l3);
        // mv.visitFrame(F_APPEND, 1, new Object[] { "java/lang/Object" }, 0, null);
        mv.visitVarInsn(ALOAD, varOffset); // load v, the result
        // mv.visitInsn(ARETURN);
        mv.visitLabel(l5);
        // mv.visitLocalVariable("test", "Ljava/lang/Object;", null, l0, l5, 0);
        // mv.visitLocalVariable("handler", "Lcom/stralos/lang/Lambda1;", null, l0, l5, 1);

        // context.pop(0, "v1");
        // context.pop(1, "v2");
        // context.pop(1, "e");


        //
        //
        // Label l0 = new Label();
        // Label l1 = new Label();
        // Label l2 = new Label();
        // mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
        // mv.visitLabel(l0);
        // mv.visitLineNumber(49, l0);
        //
        // args[0].visit(context, mv);
        //
        // mv.visitLabel(l1);
        // Label end = new Label();
        // mv.visitJumpInsn(GOTO, end);
        // mv.visitLabel(l2);
        // mv.visitLineNumber(50, l2);
        // mv.visitVarInsn(ASTORE, varOffset);
        // Label l4 = new Label();
        // mv.visitLabel(l4);
        // mv.visitLineNumber(51, l4);
        //
        // context.push(1, new VarInfo(varOffset, "e", l4, end, "java/lang/Throwable"));
        //
        // // mv.visitVarInsn(ALOAD, 1);
        // args[1].visit(context, mv);
        // // mv.visitTypeInsn(CHECKCAST, LAMBDA_PATH_BASE+1);
        // mv.visitVarInsn(ALOAD, varOffset);
        // ASMUtil.invokeLambda(mv, 1);
        // // mv.visitMethodInsn(INVOKEVIRTUAL, "com/stralos/lang/Lambda1", "apply",
        // "(Ljava/lang/Object;)Ljava/lang/Object;");
        // // mv.visitInsn(POP);
        //
        // context.pop(1, "e");
        //
        // mv.visitLabel(end);
        // mv.visitLineNumber(53, end);
        // Label l5 = new Label();
        // mv.visitLabel(l5);
        // // mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l4, end, varOffset);
        //
        //
        //


        //
        //
        // Label l0 = new Label();
        // Label l1 = new Label();
        // Label l2 = new Label();
        // Label l3 = new Label();
        // Label l4 = new Label();
        // mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
        // mv.visitLabel(l0);
        // mv.visitLineNumber(14, l0);
        //
        // args[0].visit(context, mv);
        //
        // mv.visitLabel(l1);
        // mv.visitJumpInsn(GOTO, l3);
        // mv.visitLabel(l2);
        // mv.visitLineNumber(15, l2);
        // mv.visitVarInsn(ASTORE, varOffset); // e?
        // mv.visitLabel(l4);
        // mv.visitLineNumber(16, l4);
        //
        // context.push(1, new VarInfo(varOffset, "e", l4, l3, "java/lang/Throwable"));
        // args[1].visit(context, mv);
        // mv.visitVarInsn(ALOAD, varOffset); // TODO: necessary?
        // invokeLambda(mv, 1);
        // context.pop(1, "e");
        //
        // mv.visitLabel(l3);
        // mv.visitLineNumber(18, l3);
        // Label l5 = new Label();
        // mv.visitLabel(l5);
    }

    private void handleIf(EvalContext context, MethodVisitor mv, S[] params) {
        Label l0 = new Label();
        mv.visitLabel(l0);

        params[0].visit(context, mv);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");

        Label l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);

        params[1].visit(context, mv);

        Label l4 = new Label();
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l2);

        params[2].visit(context, mv);

        mv.visitLabel(l4);
    }

    private void handleAnd(EvalContext context, MethodVisitor mv, S[] params) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(31, l0);

        params[0].visit(context, mv);

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);

        params[1].visit(context, mv);

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitInsn(ICONST_1);
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
    }

    private void handleOr(EvalContext context, MethodVisitor mv, S[] params) {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(37, l0);

        params[0].visit(context, mv);

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);

        params[1].visit(context, mv);

        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        mv.visitJumpInsn(IFNE, l1);
        mv.visitInsn(ICONST_0);
        Label l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l1);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(l2);
        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { Opcodes.INTEGER });
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
    }

    private void handleCond(EvalContext context, MethodVisitor mv, S[] params) {
        Label end = new Label();
        for (int i = 0; i < params.length; i++) {
            if (!(params[i] instanceof SList)) {
                throw new RuntimeException("Invalid cond form: " + params);
            }
            S[] ss = ((SList) params[i]).ss;
            Label skip = new Label();
            S cond = ss[0];
            S ifTrue = ss[1];

            cond.visit(context, mv);
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            mv.visitJumpInsn(IFEQ, skip);

            ifTrue.visit(context, mv);
            mv.visitJumpInsn(GOTO, end);

            mv.visitLabel(skip);
        }
        ASMUtil.throwExc(mv, "No clause returned true in cond.");
        mv.visitLabel(end);
    }
}
