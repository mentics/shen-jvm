package com.stralos.shen.parser;

public abstract class BaseScanner extends beaver.CharScanner {
	protected BaseScanner(java.io.Reader sourceReader) throws java.io.IOException {
		super(sourceReader);
	}
	public beaver.Term makeTerm(int id, int line, int column, Object text) {
		return new AST.Term(id, line, column, text);
	}
}
