package com.stralos.shen;

import static com.stralos.shen.model.Model.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.stralos.lang.Lambda;
import com.stralos.lang.Lambda0;
import com.stralos.shen.model.FunctionInfo;
import com.stralos.shen.model.S;
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

    public static void setGlobals(Map<Symbol, Object> symbolAssignments) {
        symbolAssignments.put(symbol("*stoutput*"), System.out);
        symbolAssignments.put(symbol("*stinput*"), System.in);
        symbolAssignments.put(symbol("*home_directory*"), new File(".").getAbsolutePath());
        symbolAssignments.put(symbol("*language*"), "JVM Bytecode");
        symbolAssignments.put(symbol("*port*"), "0.0");
        symbolAssignments.put(symbol("*porters*"), "Joel Shellman");
    }


    DirectClassLoader dcl = new DirectClassLoader(Thread.currentThread().getContextClassLoader());

    private int nextLambdaId;
    public Map<String, byte[]> globalClasses = new HashMap<>();

    private Map<String, FunctionInfo> funcInfo = new HashMap<>();

    private Map<Symbol, Object> symbolAssignments = new HashMap<>();

    private ClassLoader storeCL;


    private Environment() {
        setGlobals(symbolAssignments);
    }

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

    public void addClass(String key, byte[] cl) {
        dcl.addClass(key, cl);
    }

    public void beginSession() {
        storeCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(dcl);
    }

    public void endSession() {
        Thread.currentThread().setContextClassLoader(storeCL);
        storeCL = null;
    }

    public Lambda0 run(String prefix, S s) {
        beginSession();
        try {
            String fullName = prefix + "$" + nextLambdaId();
            dcl.loadClass("com.stralos.asm.ASMUtil")
               .getMethod("run", new Class[] { EvalContext.class, String.class, S.class })
               .invoke(null, newEvalContext(), fullName, s);
            String clName = fullName.replace('/', '.');
            Lambda0 inst = (Lambda0) dcl.loadClass(clName).newInstance();
            return inst;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            endSession();
        }
    }

    // public Lambda0 newInstance(String clName) {
    // try {
    // return (Lambda0) loadClass(clName).newInstance();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    //
    // private Class<?> loadClass(String clName) {
    // try {
    // return dcl.loadClass(clName);
    // } catch (ClassNotFoundException e) {
    // throw new RuntimeException(e);
    // }
    // }
    //

}
