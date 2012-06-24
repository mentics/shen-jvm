package com.stralos.shen;

import static com.stralos.shen.ShenCompiler.*;
import static com.stralos.shen.model.Model.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.stralos.lang.Lambda1;
import com.stralos.shen.model.LList;
import com.stralos.shen.model.Model;

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

    @Test
    public void testPartial() {
        Lambda1 l = (Lambda1) compile(slist(symbol("+"), integer(6l))).apply();
        assertEquals(11l, l.apply(5l));
    }

    @Test
    public void testList() {
        LList l = (LList) compile(list(symbol("+"), integer(5), integer(6))).apply();
        assertEquals(3, l.toList().length());

        LList l2 = (LList) compile(list(symbol("+"), slist(symbol("+"), flot(5.5d), flot(1.1d)), integer(6))).apply();
        assertArrayEquals(new Object[] { symbol("+"), 6.6d, 6l }, l2.toList().toArray().array());
    }

    // @Test
    // public void testEvalKL() {
    // compile(slist())
    // }
}
