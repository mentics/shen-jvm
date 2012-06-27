package com.stralos.shen;

import static com.stralos.shen.ShenCompiler.*;
import static com.stralos.shen.model.Model.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.stralos.lang.Lambda1;
import com.stralos.shen.model.LList;


public class CompilerTest {
    @Test
    public void test() {
        assertEquals(11l, compile(slist(symbol("+"), integer(5), integer(6))).apply());
    }

    @Test
    public void testLambda() {
        Lambda1 l = (Lambda1) compile(slist(symbol("lambda"), symbol("X"), slist(symbol("+"), symbol("X"), integer(6)))).apply();
        assertEquals(11l, l.apply(5l));
    }

    @Test
    public void testDefun() {
        Environment env = Environment.theEnvironment();
        compile(env,
                slist(symbol("defun"),
                      symbol("test"),
                      slist(symbol("X"), symbol("Y")),
                      slist(symbol("+"), symbol("X"), symbol("Y")))).apply();
        // Test user partial and lambda in first position
        assertEquals(9d, compile(env, slist(slist(symbol("test"), flot(6d)), integer(3l))).apply());
    }

    @Test
    public void testLet() {
        assertEquals(13d,
                     compile(slist(symbol("let"),
                                   symbol("MyVar"),
                                   slist(symbol("+"), integer(5l), flot(6d)),
                                   slist(symbol("+"), integer(2l), symbol("MyVar")))).apply());
    }

    @Test
    public void testBuiltinPartial() {
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

    @Test
    public void testSymbolReturnedInFirstPosition() {
        assertEquals("ab",
                     compile(slist(slist(symbol("intern"), slist(symbol("cn"), string("c"), string("n"))),
                                   string("a"),
                                   string("b"))).apply());
    }

    @Test
    public void testEvalKL() {
        assertEquals(6.6d, compile(slist(symbol("eval-kl"), list(symbol("+"), flot(5.5d), flot(1.1d)))).apply());

        assertEquals(12.6d,
                     compile(slist(symbol("eval-kl"),
                                   list(symbol("+"), slist(symbol("+"), flot(5.5d), flot(1.1d)), integer(6)))).apply());
    }
}
