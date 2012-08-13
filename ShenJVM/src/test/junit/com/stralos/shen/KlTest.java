package com.stralos.shen;

import static com.stralos.asm.ASMUtil.log;
import static com.stralos.shen.model.Loc.line;
import static com.stralos.shen.test.TestUtil.env;
import static com.stralos.shen.test.TestUtil.evalSingle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static tomove.Util.streamFromCL;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.junit.Test;

import com.stralos.shen.model.Cons;
import com.stralos.shen.model.Loc;
import com.stralos.shen.model.Model;
import com.stralos.shen.model.S;
import com.stralos.shen.model.SList;
import com.stralos.shen.parser.AST;
import com.stralos.shen.parser.ModelWalker;
import com.stralos.shen.parser.Parser;
import com.stralos.shen.parser.Scanner;
import com.stralos.shen.test.TestUtil;

import fj.data.List;

public class KlTest {
	@Test
	public void testFunctionFunctionShortcut() {
		assertEquals(2l, TestUtil.eval(
				"(defun func (X Y) (+ X Y))\n" + "((func) 1 1)").get(1));
		assertEquals(2l, TestUtil.eval(
				"((func) 1 1)" + "(defun func (X Y) (+ X Y))\n").get(0));
	}
	
	@Test
	public void testVariableScopedIntoLambda() {
		assertEquals(2l, TestUtil.eval( 
				" (let Arity 1\n" + 
				"  (let Free 2\n" + 
				"   (let Variables 3\n" + 
				"    (let Linear 4\n" + 
				"     (let Abstractions 5\n" + 
				"      (let Applications\n" + 
				"       ((lambda X (+ (printOut Variables) X)) Abstractions) 1)\n" + 
				"       (cons Variables (cons Applications ())))))))))"));
	}

	@Test
	public void testDuplicateLetVarNames() {
		assertEquals(
				4l,
				evalSingle("(let Result (let Result (+ 1 1) (+ Result 1)) (+ Result 1))"));
	}

	@Test
	public void testDuplicateLetVarNames2() {
		assertEquals(2l, evalSingle("(let Result 1 (let Result 2 Result))"));
	}

	@Test
	public void testLetResultIf() {
		assertEquals(1l, evalSingle(" (let Result\n" + "  1\n"
				+ "  (if false\n" + "   (let Result 2 2)\n" + "   Result))"));
	}

	@Test
	public void testShenInsertProglogVariablesHelp() {
		assertNull(evalSingle("(defun shen-insert-prolog-variables-help (V1493 V1494 V1495)\n"
				+ " (cond ((= () V1494) V1493)\n"
				+ "  ((and (cons? V1494) (variable? (hd V1494)))\n"
				+ "   (let V (shen-newpv V1495)\n"
				+ "    (let XV/Y (subst V (hd V1494) V1493)\n"
				+ "     (let Z-Y (remove (hd V1494) (tl V1494))\n"
				+ "      (shen-insert-prolog-variables-help XV/Y Z-Y V1495)))))\n"
				+ "  ((cons? V1494) (shen-insert-prolog-variables-help V1493 (tl V1494) V1495))\n"
				+ "  (true (shen-sys-error shen-insert-prolog-variables-help))))\n"));
	}

	@Test
	public void testDefunWithFuncParam() {
		java.util.List<Object> result = TestUtil
				.eval("(defun cnf (L)\n(cn \"str \"\n(L \"arg\")))\n\n(cnf (lambda S\n(cn S \"suffix\")))");
		assertEquals("str argsuffix", result.get(1));
	}

	@Test
	public void testUnitTest() throws Exception {
		List<S> ut = toS("unit-test.kl", new InputStreamReader(
				streamFromCL("com/stralos/shen/unit-test.kl")));
		assertEquals(1, line(ut.index(0)));
		assertEquals(2, line(ut.index(1)));
		assertEquals(3, line(ut.index(2)));
		assertEquals(4, line(ut.index(3)));
		assertEquals(4, line(((SList) ut.index(3)).ss[0]));
		assertEquals(4, line(((SList) ut.index(3)).ss[1]));
		S s2 = ((SList) ut.index(3)).ss[2];
		assertEquals("(Line?)", s2.toString());
		assertEquals(5, line(s2));
		S s3 = ((SList) ut.index(3)).ss[3];
		assertEquals("(ref2 (cn \"line 6\" Line?))", s3.toString());
		assertEquals(6, line(s3));

		List<S> ref = toS("referenced.kl", new InputStreamReader(
				streamFromCL("com/stralos/shen/referenced.kl")));
		for (S s : ut) {
			ShenCompiler.compile(env, s).apply();
		}
		for (S s : ref) {
			ShenCompiler.compile(env, s).apply();
		}
		Object evalSingle = evalSingle("(line4 \"arg0\")");
		assertEquals("from ref line 4",
				((RuntimeException) evalSingle).getMessage());
		// TODO: check line numbers in stack trace
	}

	@Test
	public void testCond() {
		assertEquals(3l, evalSingle("(cond (false 2) (true 3)"));
		assertEquals(2l, evalSingle("(cond ((= () ()) 2) (true 3)"));
	}

	@Test
	public void testCons() {
		assertEquals(Model.cons(null, 2, Cons.NIL), evalSingle("(cons 2 ())"));
	}

	@Test
	public void testDefunShenInitArityTable() {
		TestUtil.eval("(printOut \"blah\"");
		java.util.List<Object> o = TestUtil
				.eval("(defun shen-initialise_arity_table (V1497)\n"
						+
						// "(printOut V1497))" +
						" (cond ((= () V1497) (let X (printOut \"nil case matched\") ()))\n"
						+ "  ((and (cons? V1497) (cons? (tl V1497)))\n"
						+ "   (let DecArity\n"
						+ "    (let X (printOut \"hdtl case matched\") (printOut (list-size V1497)))\n"
						+ "    (shen-initialise_arity_table (tl (tl V1497)))))\n"
						+ "  (true (simple-error shen-initialise_arity_table))))\n"
						+ // shen-sys-error
						"\n"
						+ "(defun arity (V1498)\n"
						+ " (trap-error (get V1498 arity (value shen-*property-vector*)) (lambda E -1)))\n"
						+ "\n" + "(shen-initialise_arity_table\n"
						+ " (cons adjoin\n" + "  (cons 2\n" + "   (cons and\n"
						+ "    (cons 2\n" + "     (cons append\n"
						+ "      (cons 2 ())))))))");
		System.out.println(o);
	}

	@Test
	public void testDefunShenDefine() {
		assertNpe("(if (= Result (fail))\n"
				+ "   (let Result\n"
				+ "    (let Parse_<name> (shen-<name> V368)\n"
				+ "     (if (not (= (fail) Parse_<name>))\n"
				+ "      (let Parse_<rules> (shen-<rules> Parse_<name>)\n"
				+ "       (if (not (= (fail) Parse_<rules>))\n"
				+ "        (shen-reassemble (fst Parse_<rules>)\n"
				+ "         (shen-compile_to_machine_code (snd Parse_<name>) (snd Parse_<rules>)))\n"
				+ "        (fail)))\n" + "      (fail)))\n"
				+ "    (if (= Result (fail)) (fail) Result))\n"
				+ "   Result)\n");
	}

	@Test
	public void testDefunShenCompileToLambda() {
		assertNull(TestUtil
				.evalSingle("(defun shen-compile_to_lambda+ (V399 V400)\n"
						+ " (let Arity (shen-aritycheck V399 V400)\n"
						+ "  (let Free (map (lambda Rule (shen-free_variable_check V399 Rule)) V400)\n"
						+ "    ()))))"));
	}

	@Test
	public void testDefunShenObStr() {
		assertNull(TestUtil
				.evalSingle("(defun shen-ob->str (V1060 V1061)\n"
						+ " (cond ((= () V1061) (if (= V1060 \"R\") \"()\" \"[]\"))\n"
						+ "  ((= V1061 (vector 0)) \"<>\")\n"
						+ "  ((cons? V1061)\n"
						+ "   (shen-cn-all\n"
						+ "    (append (if (= V1060 \"R\") (cons \"(\" ()) (cons \"[\" ()))\n"
						+ "     (append (cons (shen-ob->str V1060 (hd V1061)) ())\n"
						+ "      (append\n"
						+ "       (shen-xmapcan (value *maximum-print-sequence-size*)\n"
						+ "        (lambda Z (cons \" \" (cons (shen-ob->str V1060 Z) ()))) (tl V1061))\n"
						+ "       (if (= V1060 \"R\") (cons \")\" ()) (cons \"]\" ())))))))\n"
						+ "  ((vector? V1061)\n"
						+ "   (let L (shen-vector->list V1061 1)\n"
						+ "    (let E\n"
						+ "     (tlstr\n"
						+ "      (shen-cn-all\n"
						+ "       (shen-xmapcan (- (value *maximum-print-sequence-size*) 1)\n"
						+ "        (lambda Z\n"
						+ "         (cons \" \" (cons (shen-ob->str V1060 (shen-blank-fail Z)) ())))\n"
						+ "        L)))\n"
						+ "     (let V (cn \"<\" (cn E \">\")) V))))\n"
						+ "  ((and (not (string? V1061)) (absvector? V1061))\n"
						+ "   (trap-error (shen-ob->str \"A\" ((<-address V1061 0) V1061))\n"
						+ "    (lambda Ignore\n"
						+ "     (let L (shen-vector->list V1061 0)\n"
						+ "      (let E\n"
						+ "       (tlstr\n"
						+ "        (shen-cn-all\n"
						+ "         (shen-xmapcan (- (value *maximum-print-sequence-size*) 1)\n"
						+ "          (lambda Z (cons \" \" (cons (shen-ob->str V1060 Z) ()))) L)))\n"
						+ "       (let V (cn \"<\" (cn E \">\")) V))))))\n"
						+ "  ((= shen-vector-failure-object V1061) \"...\")\n"
						+ "  (true (if (and (= V1060 \"A\") (string? V1061)) V1061 (str V1061)))))\n"));
	}

	private void assertNpe(String string) {
		assertEquals(NullPointerException.class, TestUtil.evalSingle(string)
				.getClass());
	}

	@Test
	public void testSomething() throws Exception {
		// Reader reader = new
		// StringReader("(defun } (X Y) (if (< X 1) Y (test (- X 1) (+ X Y)))) (test 4 2)");
		// Reader reader = new
		// StringReader("(= (trap-error (/ 1 0) (lambda E -1)) -1)");

		// TestUtil.evalSingle("(lambda Z (f2 A Z))");
		// TestUtil.evalSingle("(let X (shen-aritycheck V399 V400) (let Y (map (lambda Z (shen-free_variable_check V399 Z)) V400) ()))))");

		// TestUtil.evalSingle();
	}

	@Test
	public void testBreakingThing() throws Exception {
		File f = new File("test-core.kl");
		Reader reader = new FileReader(f);
		Object goal = new Parser().parse(new Scanner(reader));
		System.out.println(goal);

		AST.ListOfExpr expr = (AST.ListOfExpr) goal;
		// expr.accept(new PrintWalker());
		ModelWalker mw = new ModelWalker(f.getAbsolutePath());
		expr.accept(mw);
		List<S> ss = mw.getResult();
		Environment env = Environment.theEnvironment();
		for (S s : ss) {
			if (!s.toString().contains("trap-error")) {
				log("evaluating: " + s);
				log(" > " + ShenCompiler.compile(env, s).apply() + "\n");
			}
		}
	}

	@Test
	public void testKl() throws Exception {
		String base = "kl/";
		String[] ordered = new String[] { "sys.kl", "writer.kl", "core.kl",
				"prolog.kl", "yacc.kl" };

		File dir = new File(base);

		for (int i = 0; i < ordered.length; i++) {
			File f = new File(base + ordered[i]);
			System.out.println("Compiling: " + f.getName() + "\n");
			log("Compiling: " + f.getName() + "\n");
			FileReader reader = new FileReader(f);
			try {
				eval(f.getAbsolutePath(), reader);
			} finally {
				reader.close();
			}
		}

		for (File f : dir.listFiles()) {
			for (int i = 0; i < ordered.length; i++) {
				if (f.getName().contains(ordered[i])) {
					continue;
				}
			}
			if (f.isFile() && f.getName().endsWith(".kl")) {
				System.out.println("Compiling: " + f.getName() + "\n");
				log("Compiling: " + f.getName() + "\n");
				FileReader reader = new FileReader(f);
				try {
					eval(f.getAbsolutePath(), reader);
				} finally {
					reader.close();
				}
			}
		}
	}

	public java.util.List<Object> eval(String filename, Reader reader)
			throws IOException {
		List<S> ss = toS(filename, reader);
		java.util.List<Object> result = new ArrayList<Object>();
		Environment env = Environment.theEnvironment();
		for (S s : ss) {
			log("evaluating (" + Loc.path(s) + ":" + Loc.line(s) + "): " + s);
			Object one = ShenCompiler.compile(env, s).apply();
			log(" > " + one + "\n");
			result.add(one);
		}
		return result;
	}

	public List<S> toS(String filename, Reader reader) throws IOException {
		Object goal = new Parser().parse(new Scanner(reader));
		AST.ListOfExpr expr = (AST.ListOfExpr) goal;
		ModelWalker mw = new ModelWalker(filename);
		expr.accept(mw);
		List<S> ss = mw.getResult();
		return ss;
	}
}
