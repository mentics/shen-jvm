package com.stralos.shen;

import java.io.FileReader;
import java.io.Reader;

import org.junit.Test;

import com.stralos.shen.model.S;
import com.stralos.shen.parser.AST;
import com.stralos.shen.parser.ModelWalker;
import com.stralos.shen.parser.Parser;
import com.stralos.shen.parser.Scanner;

import fj.data.List;


public class KlTest {

    @Test
    public void testKl() throws Exception {
        // TODO: read file from classpath
        Reader reader = new FileReader("test.kl");
        Object goal = new Parser().parse(new Scanner(reader));
        System.out.println(goal);

        AST.ListOfExpr expr = (AST.ListOfExpr) goal;
        // expr.accept(new PrintWalker());
        ModelWalker mw = new ModelWalker();
        expr.accept(mw);
        List<S> ss = mw.getResult();
        Environment env = Environment.theEnvironment();
        for (S s : ss) {
            if (!s.toString().contains("eval-kl")) {
                System.out.println("evaluating: " + s);
                System.out.println(" > " + ShenCompiler.compile(env, s).apply());
            }
        }
    }
}
