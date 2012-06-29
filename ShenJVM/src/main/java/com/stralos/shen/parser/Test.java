package com.stralos.shen.parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.stralos.shen.Environment;
import com.stralos.shen.ShenCompiler;
import com.stralos.shen.model.S;

import fj.data.List;


public class Test {
    public static void main(String[] args) throws IOException {
        // Reader reader = new
        // StringReader("(defun test (X Y) (* X Y)) \n\n\n (+ ((lambda X (+ X 6)) 3) 4) (test 11 22) 2 3 \"test\"");
        // Reader reader = new StringReader("(pr \"testing\n\" (value *stoutput*))");
        Reader reader = new FileReader("test.kl");
        Object goal = new Parser().parse(new Scanner(reader));
        System.out.println(goal);

        AST.ListOfExpr expr = (AST.ListOfExpr) goal;
        expr.accept(new PrintWalker());
        ModelWalker mw = new ModelWalker();
        expr.accept(mw);
        List<S> ss = mw.getResult();
        Environment env = Environment.theEnvironment();
        for (S s : ss) {
            System.out.println("evaluated: " + ShenCompiler.compile(env, s).apply());
        }
    }
}
