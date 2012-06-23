package com.stralos.shen;

import static com.stralos.shen.ShenCompiler.*;

import com.stralos.shen.model.S;

public class ASMTestUtil {
    public static Object testIt(S s) {
        return compile(new Environment(), s).apply();
    }
}
