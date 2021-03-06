package com.stralos.shen;

import static com.stralos.shen.ShenCompiler.compile;
import static com.stralos.shen.model.Location.UNKNOWN;
import static com.stralos.shen.model.Model.cons;
import static com.stralos.shen.model.Model.slist;
import static com.stralos.shen.test.TestUtil.flot;
import static com.stralos.shen.test.TestUtil.integer;
import static com.stralos.shen.test.TestUtil.string;
import static com.stralos.shen.test.TestUtil.symbol;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.stralos.lang.Lambda1;
import com.stralos.shen.model.Cons;

import fj.F;


public class CompilerTest {
    @Test
    public void test() {
        assertEquals(11l, compile(slist(symbol("+"), integer(5), integer(6))).apply());
    }

    @Test
    public void testIf() {
        assertEquals(1l,
                     compile(slist(symbol("if"), slist(symbol("<"), integer(1), integer(2)), integer(1), integer(2))).apply());
    }

    @Test
    public void testRecursive() {
        Environment env = Environment.theEnvironment();
        compile(env,
                slist(symbol("defun"),
                      symbol("test"),
                      slist(symbol("X"), symbol("Y")),
                      slist(symbol("if"),
                            slist(symbol("<"), symbol("X"), integer(1)),
                            symbol("Y"),
                            slist(symbol("test"),
                                  slist(symbol("-"), symbol("X"), integer(1)),
                                  slist(symbol("+"), symbol("Y"), symbol("X")))))).apply();

        assertEquals(12l, compile(env, slist(symbol("test"), integer(4), integer(2))).apply());
    }

    @Test
    public void testTrapError() {
        Object a = compile(slist(symbol("trap-error"),
                                 slist(symbol("/"), integer(1), integer(0)),
                                 slist(symbol("lambda"), symbol("E"), integer(-1)))).apply();
        System.out.println(a);
        // assertTrue();
    }

    @Test
    public void testClassLoaderIssue() {
        assertTrue((Boolean) compile(slist(symbol("="),
                                           slist(symbol("trap-error"),
                                                 slist(symbol("/"), integer(1), integer(0)),
                                                 slist(symbol("lambda"), symbol("E"), integer(-1))),
                                           integer(-1))).apply());
        // (test-is (= (trap-error (/ 1 0) (lambda E -1)) -1))
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
        Cons l = (Cons) compile(makeCons(symbol("+"), integer(5), integer(6))).apply();
        assertEquals(3, toList(l).size());

        Cons l2 = (Cons) compile(makeCons(symbol("+"), slist(symbol("+"), flot(5.5d), flot(1.1d)), integer(6))).apply();
        assertArrayEquals(new Object[] { symbol("+"), 6.6d, 6l }, toList(l2).toArray());
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
        assertEquals(6.6d, compile(slist(symbol("eval-kl"), makeCons(symbol("+"), flot(5.5d), flot(1.1d)))).apply());

        assertEquals(18.1d,
                     compile(slist(symbol("eval-kl"),
                             makeCons(symbol("+"),
                                        slist(symbol("+"), slist(symbol("*"), flot(5.5d), flot(2.0d)), flot(1.1d)),
                                        integer(6)))).apply());
    }

    @Test
    public void testSimpleEvalKL() {
        assertEquals(5l, compile(slist(symbol("eval-kl"), integer(5))).apply());
    }
    
    public static Cons makeCons(Object... os) {
        Cons c = cons(UNKNOWN, os[os.length-1], Cons.NIL);
        for (int i=os.length-1; i>0; i--) {
            c = cons(UNKNOWN, os[i], c);
        }
        return c;
    }
    
    public static List<Object> toList(Cons c) {
        return c.forEach(new F<Object,Object>() {
            public Object f(Object x) {
                return x;
            }
        }); 
    }
}
