package com.stralos.shen.test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.stralos.shen.Environment;
import com.stralos.shen.ShenCompiler;
import com.stralos.shen.model.Flot;
import com.stralos.shen.model.Int;
import com.stralos.shen.model.Location;
import com.stralos.shen.model.Model;
import com.stralos.shen.model.S;
import com.stralos.shen.model.Str;
import com.stralos.shen.model.Symbol;
import com.stralos.shen.parser.AST;
import com.stralos.shen.parser.ModelWalker;
import com.stralos.shen.parser.Parser;
import com.stralos.shen.parser.Scanner;


public class TestUtil {
    public static final Environment env = Environment.theEnvironment();


    public static List<Object> eval(String code) {
        List<Object> ret = new ArrayList<>();
        try {
            Object goal = new Parser().parse(new Scanner(new StringReader(code)));

            AST.ListOfExpr expr = (AST.ListOfExpr) goal;
            ModelWalker mw = new ModelWalker("FromString");
            expr.accept(mw);
            fj.data.List<S> ss = mw.getResult();

            for (S s : ss) {
                try {
                    ret.add(ShenCompiler.compile(env, s).apply());
                } catch (Throwable t) {
                    t.printStackTrace();
                    ret.add(t);
                }
            }
        } catch (Throwable e) {
            throw new Error(e);
        }
        return ret;
    }

    public static Object evalSingle(String code) {
        return eval(code).get(0);
    }

    public static Symbol symbol(String s) {
        return Model.symbol(s, Location.UNKNOWN);
    }

    public static Str string(String s) {
        return Model.string(s, Location.UNKNOWN);
    }

    public static Flot flot(double s) {
        return Model.flot(s, Location.UNKNOWN);
    }

    public static Int integer(long s) {
        return Model.integer(s, Location.UNKNOWN);
    }
}
