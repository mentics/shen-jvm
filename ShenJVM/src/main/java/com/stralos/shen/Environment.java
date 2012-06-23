package com.stralos.shen;

import java.util.HashMap;
import java.util.Map;

import com.stralos.lang.Lambda;
import com.stralos.shen.model.FunctionInfo;

public class Environment {
    public static final String ENV_PATH = "com/stralos/shen/Environment";
    
    private int nextLambdaId;
    public Map<String, byte[]> globalClasses = new HashMap<>();
    
    public static Map<String, Object> symbolAssignments = new HashMap<>();

    public static Map<String, Lambda> functions = new HashMap<>();

    private Map<String, FunctionInfo> funcInfo= new HashMap<>();


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
}
