package com.stralos.shen.parser;
import com.stralos.shen.parser.AST.*;
public abstract class BaseParser extends beaver.Parser {
	protected ListOfExpr createListOfExpr() {
		return new ListOfExpr();
	}
	protected void extendListOfExpr(ListOfExpr list, Expr item) {
		list.add(item);
	}
	protected Expr createExpr(Atom atom) {
		return new Expr(atom);
	}
	protected Expr createExpr(ExprList exprList) {
		return new Expr(exprList);
	}
	protected ExprList createExprList() {
		return new ExprList();
	}
	protected void extendExprList(ExprList list, Expr item) {
		list.add(item);
	}
	protected Atom createBoolAtom(Term bool) {
		return new Atom.Bool(bool);
	}
	protected Atom createStrAtom(Term string) {
		return new Atom.Str(string);
	}
	protected Atom create_IntAtom(Term _int) {
		return new Atom._Int(_int);
	}
	protected Atom create_FloatAtom(Term _float) {
		return new Atom._Float(_float);
	}
	protected Atom createSymbolAtom(Term symbol) {
		return new Atom.Symbol(symbol);
	}
}
