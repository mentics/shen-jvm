package com.stralos.shen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stralos.shen.model.FunctionInfo;


public class EvalContext {

    // Instance Fields //

    private Environment env;

    // private Map<String, byte[]> classes = new HashMap<>();
    private Map<String, VarInfo> boundSymbols = new HashMap<>();
    private List<VarInfo> localVars = new ArrayList<>();
    private int varOffset = 0;


    // Constructors //

    public EvalContext(Environment env) {
        this.env = env;
    }

    // Public Methods //

    public VarInfo getBoundSymbol(String label) {
        return boundSymbols.get(label);
    }

    public List<VarInfo> getMethodLocalVars() {
        return localVars;
    }
    
    public VarInfo[] getScopedVars() {
        // TODO: clean up
        List<VarInfo> vars = new ArrayList<>();
        for (VarInfo value : boundSymbols.values()) {
            if (value.capture) {
                vars.add(value);
            }
        }
        return vars.toArray(new VarInfo[vars.size()]);
    }

    public String newLambdaName() {
        return Primitives.NEW_LAMBDA_PATH_BASE + "$" + env.nextLambdaId();
    }
    
    
    // Method Local Var Handling //

    public int getVarOffset() {
        return varOffset;
    }
    
    public void skipLocalVarThis() {
        varOffset++;
    }

    public void bindLocalVar(VarInfo varInfo) {
        varOffset++;
        boundSymbols.put(varInfo.name, varInfo);
        localVars.add(varInfo);
    }

    public void newLambdaField(VarInfo... vars) {
        for (VarInfo var : vars) {
            boundSymbols.put(var.name, var);
        }
    }

    public void unbindLocalVar(String varName) {
        // Remove from boundSymbols but leave in localVars so it will call visit on the local var at end of method
        varOffset--;
        boundSymbols.remove(varName);
    }

    // Other //
    
    public void addClass(String name, byte[] byteArray) {
        // classes.put(name, byteArray);
        env.addClass(name, byteArray);
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
