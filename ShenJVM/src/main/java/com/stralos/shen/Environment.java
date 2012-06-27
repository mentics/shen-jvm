package com.stralos.shen;

import java.util.HashMap;
import java.util.Map;

import com.stralos.lang.Lambda;
import com.stralos.shen.model.FunctionInfo;
import com.stralos.shen.model.Symbol;


public class Environment {
    public static final String ENV_PATH = "com/stralos/shen/Environment";

    public static Map<String, Lambda> functions = new HashMap<>();

    public static Environment ENV = new Environment();


    /**
     * TODO: right now, only one per classloader allowed, but maybe change that?
     */
    public static Environment theEnvironment() {
        return ENV;
    }


    private int nextLambdaId;
    public Map<String, byte[]> globalClasses = new HashMap<>();

    private Map<String, FunctionInfo> funcInfo = new HashMap<>();

    private Map<Symbol, Object> symbolAssignments = new HashMap<>();


    private Environment() {}

    public int nextLambdaId() {
        return nextLambdaId++;
    }

    public EvalContext newEvalContext() {
        return new EvalContext(this);
    }

    public void putFunction(String funcName, FunctionInfo f) {
        funcInfo.put(funcName, f);
    }

    public FunctionInfo getFunction(String funcName) {
        return funcInfo.get(funcName);
    }

    public void assign(Object symbol, Object value) {
        symbolAssignments.put((Symbol) symbol, value);
    }

    public Object get(Object symbol) {
        return symbolAssignments.get(symbol);
    }
}
