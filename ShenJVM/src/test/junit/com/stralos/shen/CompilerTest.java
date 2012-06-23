package com.stralos.shen;

import static com.stralos.shen.ShenCompiler.*;
import static com.stralos.shen.model.Model.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.stralos.lang.Lambda1;

public class CompilerTest {
    @Test
    public void test() {
        assertEquals(11l, compile(slist(symbol("+"), integer(5), integer(6))).apply());
    }

    @Test
    public void testLambda() {
        Lambda1 l = (Lambda1) compile(slist(symbol("lambda"), symbol("X"), slist(symbol("+"), symbol("X"), integer(6))))
                .apply();
        assertEquals(11l, l.apply(5l));
    }

    @Test
    public void testDefun() {
        Environment env = new Environment();
        compile(
                env,
                slist(symbol("defun"), symbol("test"), slist(symbol("X"), symbol("Y")),
                        slist(symbol("+"), symbol("X"), symbol("Y")))).apply();
        assertEquals(11.0d, compile(env, slist(symbol("test"), flot(6d), flot(5d))).apply());
    }
}
