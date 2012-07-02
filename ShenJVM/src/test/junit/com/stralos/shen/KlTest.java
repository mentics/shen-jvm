package com.stralos.shen;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.Reader;

import org.junit.Test;

import com.stralos.shen.model.S;
import com.stralos.shen.parser.AST;
import com.stralos.shen.parser.ModelWalker;
import com.stralos.shen.parser.Parser;
import com.stralos.shen.parser.Scanner;
import com.stralos.shen.test.TestUtil;

import fj.data.List;


public class KlTest {
    @Test
    public void testDefunShenDefine() {
        // assertNpe("(shen-compile_to_machine_code (snd Parse_<name>) (snd Parse_<rules>))");
        // assertNpe("(shen-reassemble (fst Parse_<rules>)\n"
        // + " (shen-compile_to_machine_code (snd Parse_<name>) (snd Parse_<rules>)))");
        // assertNpe("(if (not (= (fail) Parse_<rules>))\n" + "    (shen-reassemble (fst Parse_<rules>)\n"
        // + "     (shen-compile_to_machine_code (snd Parse_<name>) (snd Parse_<rules>)))\n" + "    (fail)))");

        // assertNpe("(if (= Result (fail)) (let Result (+ 2 2) (+ Result 2)) (Result))");

        // assertNpe("(if true (let Result 9 11) Result)");


        assertNpe("(if (= Result (fail))\n" + "   (let Result\n" + "    (let Parse_<name> (shen-<name> V368)\n"
                  + "     (if (not (= (fail) Parse_<name>))\n"
                  + "      (let Parse_<rules> (shen-<rules> Parse_<name>)\n"
                  + "       (if (not (= (fail) Parse_<rules>))\n" + "        (shen-reassemble (fst Parse_<rules>)\n"
                  + "         (shen-compile_to_machine_code (snd Parse_<name>) (snd Parse_<rules>)))\n"
                  + "        (fail)))\n" + "      (fail)))\n" + "    (if (= Result (fail)) (fail) Result))\n"
                  + "   Result)\n");
    }

    @Test
    public void testDefunShenCompileToLambda() {
        assertNull(TestUtil.evalSingle("(defun shen-compile_to_lambda+ (V399 V400)\n"
                                      + " (let Arity (shen-aritycheck V399 V400)\n"
                                      + "  (let Free (map (lambda Rule (shen-free_variable_check V399 Rule)) V400)\n"
                                      + "    ()))))"));
    }

    private void assertNpe(String string) {
        assertEquals(NullPointerException.class, TestUtil.evalSingle(string).getClass());
    }

    @Test
    public void testSomething() throws Exception {
        // Reader reader = new StringReader("(defun } (X Y) (if (< X 1) Y (test (- X 1) (+ X Y)))) (test 4 2)");
        // Reader reader = new StringReader("(= (trap-error (/ 1 0) (lambda E -1)) -1)");


        // TestUtil.evalSingle("(lambda Z (f2 A Z))");
        // TestUtil.evalSingle("(let X (shen-aritycheck V399 V400) (let Y (map (lambda Z (shen-free_variable_check V399 Z)) V400) ()))))");

        // TestUtil.evalSingle();
    }

    @Test
    public void testBreakingThing() throws Exception {
        Reader reader = new FileReader("testing.kl");
        Object goal = new Parser().parse(new Scanner(reader));
        System.out.println(goal);

        AST.ListOfExpr expr = (AST.ListOfExpr) goal;
        // expr.accept(new PrintWalker());
        ModelWalker mw = new ModelWalker();
        expr.accept(mw);
        List<S> ss = mw.getResult();
        Environment env = Environment.theEnvironment();
        for (S s : ss) {
            if (!s.toString().contains("trap-error")) {
                System.out.println("evaluating: " + s);
                System.out.println(" > " + ShenCompiler.compile(env, s).apply());
            }
        }

    }

    @Test
    public void testKl() throws Exception {
        // TODO: read file from classpath
        // Reader reader = new FileReader("testing.kl");
        Reader reader = new FileReader("/home/taotree/dev/dest/workspace/git-shen-jvm/ShenCompiler/kl/core.kl");
        Object goal = new Parser().parse(new Scanner(reader));
        System.out.println(goal);

        AST.ListOfExpr expr = (AST.ListOfExpr) goal;
        // expr.accept(new PrintWalker());
        ModelWalker mw = new ModelWalker();
        expr.accept(mw);
        List<S> ss = mw.getResult();
        Environment env = Environment.theEnvironment();
        for (S s : ss) {
            // if (!s.toString().contains("trap-error")) {
            System.out.println("evaluating: " + s);
            System.out.println(" > " + ShenCompiler.compile(env, s).apply());
            // }
        }
    }
}
