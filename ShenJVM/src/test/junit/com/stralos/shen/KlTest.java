package com.stralos.shen;

import static com.stralos.shen.test.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public void testCond() {
//        assertEquals(3l, evalSingle("(cond (false 2) (true 3)"));
        assertEquals(2l, evalSingle("(cond ((= () ()) 2) (true 3)"));
    }
    
    @Test
    public void testCons() {
        assertEquals(2l, evalSingle("(cons 2 ())"));
    }
    
    @Test
    public void testDefunShenInitArityTable() {
        TestUtil.eval("(printOut \"blah\"");
        java.util.List<Object> o = TestUtil.eval("(defun shen-initialise_arity_table (V1497)\n" +
//    "(printOut V1497))" +
        		" (cond ((= () V1497) (let X (printOut \"nil case matched\") ()))\n" + 
        		"  ((and (cons? V1497) (cons? (tl V1497)))\n" +
        		"   (let DecArity\n" + 
        		"    (let X (printOut \"hdtl case matched\") (printOut (list-size V1497)))\n" +
        		"    (shen-initialise_arity_table (tl (tl V1497)))))\n" +
        		"  (true (simple-error shen-initialise_arity_table))))\n" + // shen-sys-error 
        		"\n" + 
        		"(defun arity (V1498)\n" + 
        		" (trap-error (get V1498 arity (value shen-*property-vector*)) (lambda E -1)))\n" + 
        		"\n" + 
        		"(shen-initialise_arity_table\n" + 
        		" (cons adjoin\n" + 
        		"  (cons 2\n" + 
        		"   (cons and\n" + 
        		"    (cons 2\n" + 
        		"     (cons append\n" + 
        		"      (cons 2 ())))))))");
        System.out.println(o);
    }
    
    @Test
    public void testDefunShenDefine() {
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
    
    @Test
    public void testDefunShenObStr() {
        assertNull(TestUtil.evalSingle("(defun shen-ob->str (V1060 V1061)\n" + 
        		" (cond ((= () V1061) (if (= V1060 \"R\") \"()\" \"[]\"))\n" + 
        		"  ((= V1061 (vector 0)) \"<>\")\n" + 
        		"  ((cons? V1061)\n" + 
        		"   (shen-cn-all\n" + 
        		"    (append (if (= V1060 \"R\") (cons \"(\" ()) (cons \"[\" ()))\n" + 
        		"     (append (cons (shen-ob->str V1060 (hd V1061)) ())\n" + 
        		"      (append\n" + 
        		"       (shen-xmapcan (value *maximum-print-sequence-size*)\n" + 
        		"        (lambda Z (cons \" \" (cons (shen-ob->str V1060 Z) ()))) (tl V1061))\n" + 
        		"       (if (= V1060 \"R\") (cons \")\" ()) (cons \"]\" ())))))))\n" + 
        		"  ((vector? V1061)\n" + 
        		"   (let L (shen-vector->list V1061 1)\n" + 
        		"    (let E\n" + 
        		"     (tlstr\n" + 
        		"      (shen-cn-all\n" + 
        		"       (shen-xmapcan (- (value *maximum-print-sequence-size*) 1)\n" + 
        		"        (lambda Z\n" + 
        		"         (cons \" \" (cons (shen-ob->str V1060 (shen-blank-fail Z)) ())))\n" + 
        		"        L)))\n" + 
        		"     (let V (cn \"<\" (cn E \">\")) V))))\n" + 
        		"  ((and (not (string? V1061)) (absvector? V1061))\n" + 
        		"   (trap-error (shen-ob->str \"A\" ((<-address V1061 0) V1061))\n" + 
        		"    (lambda Ignore\n" + 
        		"     (let L (shen-vector->list V1061 0)\n" + 
        		"      (let E\n" + 
        		"       (tlstr\n" + 
        		"        (shen-cn-all\n" + 
        		"         (shen-xmapcan (- (value *maximum-print-sequence-size*) 1)\n" + 
        		"          (lambda Z (cons \" \" (cons (shen-ob->str V1060 Z) ()))) L)))\n" + 
        		"       (let V (cn \"<\" (cn E \">\")) V))))))\n" + 
        		"  ((= shen-vector-failure-object V1061) \"...\")\n" + 
        		"  (true (if (and (= V1060 \"A\") (string? V1061)) V1061 (str V1061)))))\n"));
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
        String base = "/home/taotree/dev/dest/workspace/git-shen-jvm/ShenCompiler/kl/";
        String[] ordered = new String[] { "sys.kl", "writer.kl", "core.kl" };
        
        File dir = new File(base);
        
        for (int i=0; i<ordered.length; i++) {
            File f = new File(base+ordered[i]);
            System.out.println("Compiling: "+f.getName());
            FileReader reader = new FileReader(f);
            try {
                eval(reader);
            } finally {
                reader.close();
            }
        }
        
        for (File f : dir.listFiles()) {
            for (int i=0; i<ordered.length; i++) {
                if (f.getName().contains(ordered[i])) {
                    continue;
                }
            }
            if (f.isFile() && f.getName().endsWith(".kl")) {
                System.out.println("Compiling: "+f.getName());
                FileReader reader = new FileReader(f);
                try {
                    eval(reader);
                } finally {
                    reader.close();
                }
            }
        }
    }

    private void eval(Reader reader) throws IOException {
        Object goal = new Parser().parse(new Scanner(reader));
//        System.out.println(goal);

        AST.ListOfExpr expr = (AST.ListOfExpr) goal;
        // expr.accept(new PrintWalker());
        ModelWalker mw = new ModelWalker();
        expr.accept(mw);
        List<S> ss = mw.getResult();
        Environment env = Environment.theEnvironment();
        for (S s : ss) {
            System.out.println("evaluating: " + s);
            Object result = ShenCompiler.compile(env, s).apply();
            System.out.println(" > " + result);
        }
    }
}
