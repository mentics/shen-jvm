package com.stralos.shen;

import java.util.HashMap;
import java.util.Map;

import com.stralos.shen.model.FunctionInfo;

public class EvalContext {

    // Instance Fields //

    private Environment env;

    private Map<String, byte[]> classes = new HashMap<>();
    private Map<String, VarInfo> boundSymbols = new HashMap<>();
    private int varOffset = 0;

    // Constructors //

    public EvalContext(Environment env) {
        this.env = env;
    }

    // Public Methods //

    public Map<String, byte[]> getClasses() {
        return classes;
    }

    public VarInfo getBoundSymbol(String label) {
        return boundSymbols.get(label);
    }

    // Local Methods //

    public String newLambdaName() {
        return Primitives.NEW_LAMBDA_PATH_BASE + "$" + env.nextLambdaId();
    }

    public Map<String, VarInfo> getBoundSymbols() {
        return boundSymbols;
    }

    public void push(int add) {
        varOffset += add;
    }

    public void push(int add, VarInfo varInfo) {
        push(add);
        boundSymbols.put(varInfo.name, varInfo);
    }

    public void push(VarInfo... vars) {
        for (VarInfo var : vars) {
            boundSymbols.put(var.name, var);
        }
    }

    public void pop(int remove, String varName) {
        varOffset -= remove;
        boundSymbols.remove(varName);
    }

    public void putClass(String name, byte[] byteArray) {
        classes.put(name, byteArray);
    }

    public void putClasses(Map<String, byte[]> newClasses) {
        classes.putAll(newClasses);
    }

    public int getVarOffset() {
        return varOffset;
    }

    public EvalContext newChildContext() {
        return env.newEvalContext();
    }

    public void putFunction(String funcName, FunctionInfo f) {
        env.putFunction(funcName, f);
    }

    public FunctionInfo getFunction(String funcName) {
        return env.getFunction(funcName);
    }
}
