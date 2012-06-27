package com.stralos.shen.model;

import static com.stralos.asm.ASMUtil.*;
import static com.stralos.shen.Primitives.*;
import static com.stralos.shen.ShenCompiler.*;
import static com.stralos.shen.model.Model.*;
import static org.objectweb.asm.Opcodes.*;
import static tomove.ArrayUtil.*;

import java.util.Collection;
import java.util.Map;

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

    public void visit(EvalContext context, MethodVisitor mv) {
        if (ss[0] instanceof Symbol) {
            if (!handleSpecialForm(context, mv, ss) && !handleUserFunction(context, mv)
                && !handleBuiltInFunction(context, mv)) {
                // Couldn't find anything so throw error
                throw new RuntimeException("No function defined for symbol: " + ss[0]);
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
        FunctionInfo f = context.getFunction(funcName);
        if (f != null) {
            // User defined function
            String[] params = f.params;
            S[] args = tail(ss);
            String lambdaType = Primitives.LAMBDA_PATH_BASE + params.length;

            loadGlobalFunctions(mv);
            mv.visitLdcInsn(funcName);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, lambdaType);
            visitArgs(context, mv, args);
            mv.visitMethodInsn(INVOKEVIRTUAL, lambdaType, Primitives.LAMBDA_METHOD_NAME, signatureOfArity(args.length));
            return true;
        } else {
            return false;
        }
    }

    private boolean handleBuiltInFunction(EvalContext context, MethodVisitor mv) {
        String funcName = ss[0].toString();
        try {
            Primitives.class.getField(toIdentifier(funcName));
            // Built in function
            S[] args = tail(ss);
            mv.visitFieldInsn(GETSTATIC, PRIMITIVES_PATH, toIdentifier(funcName), "L" + LAMBDA_PATH_BASE + ";");// "L" +
                                                                                                                // lambdaType
                                                                                                                // +
                                                                                                                // ";");

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

        String newLambdaName = context.newLambdaName();
        context.putClasses(createLambdaClass(context.newChildContext(), new VarInfo[0], newLambdaName, body, paramNames));

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

        context.putFunction(funcName, new FunctionInfo(paramNames));
    }

    private void handleLambda(EvalContext context, MethodVisitor mv, S[] params) {
        String className = context.newLambdaName();

        // Capture the lexical scoped variables
        Collection<VarInfo> vars = context.getBoundSymbols().values();

        Map<String, byte[]> newClasses = createLambdaClass(context.newChildContext(),
                                                           vars.toArray(new VarInfo[vars.size()]),
                                                           className,
                                                           params[1],
                                                           toStringArray(params[0]));
        context.putClasses(newClasses);
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);

        for (VarInfo var : vars) {
            mv.visitVarInsn(ALOAD, var.index);
        }
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", constructorOfArity(vars.size()));
    }

    private void handleLet(EvalContext context, MethodVisitor mv, S[] params) {
        String varName = params[0].toString();
        S value = params[1];
        S body = params[2];
        Label begin = new Label();
        Label end = new Label();

        int varOffset = context.getVarOffset();
        context.push(1, new VarInfo(varOffset, varName, begin, end));
        value.visit(context, mv);
        mv.visitVarInsn(ASTORE, varOffset);
        mv.visitLabel(begin);
        body.visit(context, mv);
        mv.visitLabel(end);
        context.pop(1, varName);
    }

    private void handleEvalKl(EvalContext context, MethodVisitor mv, S[] params) {
        params[0].visit(context, mv);
        mv.visitTypeInsn(CHECKCAST, LLIST_PATH);
        mv.visitMethodInsn(INVOKESTATIC,
                           Primitives.COMPILER_PATH,
                           Primitives.COMPILER_METHOD_NAME,
                           COMPILER_METHOD_SIGNATURE);
        // At this point, we have a lambda that will return a LList
        mv.visitMethodInsn(INVOKEVIRTUAL,
                           Primitives.LAMBDA_PATH_BASE + 0,
                           Primitives.LAMBDA_METHOD_NAME,
                           signatureOfArity(0));
        mv.visitTypeInsn(CHECKCAST, LLIST_PATH);
        // Now we have the LList, so we convert it to an SList
        mv.visitMethodInsn(INVOKESTATIC,
                           Primitives.MODEL_PATH,
                           "slist",
                           "(Lcom/stralos/shen/model/LList;)Lcom/stralos/shen/model/S;");
        // And run compile on it--this is the actual "eval-kl" part of the process
        mv.visitMethodInsn(INVOKESTATIC,
                           Primitives.COMPILER_PATH,
                           Primitives.COMPILER_METHOD_NAME,
                           COMPILER_METHOD_SIGNATURE);
        // And evaluate the lambda returned by compile so we have the actual return value on the stack
        mv.visitMethodInsn(INVOKEVIRTUAL,
                           Primitives.LAMBDA_PATH_BASE + 0,
                           Primitives.LAMBDA_METHOD_NAME,
                           signatureOfArity(0));
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
    private void handleTrapError(EvalContext context, MethodVisitor mv, S[] params) {
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
        mv.visitLabel(l0);
        mv.visitLineNumber(14, l0);

        params[0].visit(context, mv);

        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l3);
        mv.visitLabel(l2);
        mv.visitLineNumber(15, l2);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitLabel(l4);
        mv.visitLineNumber(16, l4);

        int varOffset = context.getVarOffset();
        context.push(1, new VarInfo(varOffset, "e", l4, l3));
        params[1].visit(context, mv);
        context.pop(1, "e");

        mv.visitLabel(l3);
        mv.visitLineNumber(18, l3);
        Label l5 = new Label();
        mv.visitLabel(l5);
    }

    private void handleIf(EvalContext context, MethodVisitor mv, S[] params) {
        Label l0 = new Label();
        mv.visitLabel(l0);

        params[0].visit(context, mv);

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
        if (params.length % 2 != 0) {
            throw new RuntimeException("Invalid number of params to cond.");
        }
        for (int i = 0; i < params.length; i += 2) {
            Label skip = new Label();
            S cond = params[i];
            S ifTrue = params[i + 1];

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
