package com.stralos.shen.parser;
public abstract class AST {
	public static abstract class Node {
		Node parent;
		Node prev;
		Node next;
		abstract void accept(Walker walker);
		abstract Node dup();
		static String asString(Object object) {
			return object == null ? "" : " " + object.toString();
		}
	}
	public static class Term extends Node implements beaver.Term {
		static final String[] keywords = { "'<EOF>'", "'('", "')'" };
		static final String[] tokens = { "BOOL", "STRING", "_INT", "_FLOAT", "SYMBOL" };
		int    id;
		int    line;
		int    column;
		Object text;
		public Term(int id, int line, int column, Object text) {
			this.id = id;
			this.line = line;
			this.column = column;
			this.text = text;
		}
		private Term(Term term) {
			this.id = term.id;
			this.line = term.line;
			this.column = term.column;
			this.text = term.text;
		}
		public void setId(int newId) {
			id = newId;
		}
		public int getId() {
			return id;
		}
		public Object getText() {
			return text;
		}
		public int getLine() {
			return line;
		}
		public int getColumn() {
			return column;
		}
		void accept(Walker walker) {
			walker.visit(this);
		}
		Node dup() {
			return new Term(this);
		}
		public String toString() {
			if (id >= 0) {
				if (id < keywords.length) {
					return keywords[id];
				}
				int ix = id - keywords.length;
				if (ix < tokens.length) {
					return tokens[ix];
				}
			}
			return text.toString();
		}
	}
	public static abstract class NodeList extends Node {
		Node first;
		Node last;
		int  size;
		NodeList() {}
		NodeList(Node node) {
			node.parent = this;
			last = first = node;
			size = 1;
		}
		protected NodeList(NodeList list) {
			Node elem = list.first;
			if (elem != null) {
				last = first = elem.dup();
				last.parent = this;
				last.prev = last.next = null;
				size = 1;
				for (elem = elem.next; elem != null; elem = elem.next) {
					add(elem.dup());
				}
			}
		}
		public void add(Node node) {
			node.parent = this;
			node.next = null;
			if ((node.prev = last) == null) {
				last = first = node;
			} else {
				last = last.next = node;
			}
			size++;
		}
		public void remove(Node node) {
			if (node.parent != this) {
				throw new IllegalArgumentException("wrong list");
			}
			if (node.prev != null) {
				first = node.next;
			} else {
				node.prev.next = node.next;
			}
			if (node.next == null) {
				last = node.prev;
			} else {
				node.next.prev = node.prev;
			}
			size--;
		}
		public void replace(Node node, Node newNode) {
			if (node.parent != this) {
				throw new IllegalArgumentException("wrong list");
			}
			newNode.parent = this;
			newNode.prev = node.prev;
			newNode.next = node.next;
			if (newNode.prev == null) {
				first = newNode;
			} else {
				newNode.prev.next = newNode;
			}
			if (newNode.next == null) {
				last = newNode;
			} else {
				newNode.next.prev = newNode;
			}
		}
		public String toString() {
			if (first != null) {
				String str = first.toString();
				for (Node e = first.next; e != null; e = e.next) {
					str += ", " + e;
				}
				return str;
			}
			return "";
		}
	}
	public static class ListOfExpr extends NodeList {
		private ListOfExpr(ListOfExpr list) {
			super(list);
		}
		public ListOfExpr() {
			super();
		}
		public ListOfExpr(Expr elem) {
			super(elem);
		}
		Node dup() {
			return new ListOfExpr(this);
		}
		public void accept(Walker walker) {
			walker.visit(this);
		}
		public String toString() {
			return "(ListOfExpr " + super.toString() + ")";
		}
	}
	public static class Expr extends Node {
		Atom atom;
		ExprList exprList;
		public Expr(Atom atom) {
			(this.atom = atom).parent = this;
		}
		public Expr(ExprList exprList) {
			(this.exprList = exprList).parent = this;
		}
		private Expr(Expr node) {
			(this.atom = (Atom) node.atom.dup()).parent = this;
			(this.exprList = (ExprList) node.exprList.dup()).parent = this;
		}
		Node dup() {
			return new Expr(this);
		}
		void accept(Walker walker) {
			walker.visit(this);
		}
		public String toString() {
			return "(Expr" + " " + atom + " " + exprList + ")";
		}
	}
	public static class ExprList extends NodeList {
		private ExprList(ExprList list) {
			super(list);
		}
		public ExprList() {
			super();
		}
		public ExprList(Expr elem) {
			super(elem);
		}
		Node dup() {
			return new ExprList(this);
		}
		void accept(Walker walker) {
			walker.visit(this);
		}
		public String toString() {
			return "(ExprList " + super.toString() + ")";
		}
	}
	public static abstract class Atom extends Node {
		public static class Bool extends Atom {
			Term bool;
			public Bool(Term bool) {
				(this.bool = bool).parent = this;
			}
			private Bool(Bool node) {
				(this.bool = (Term) node.bool.dup()).parent = this;
			}
			Node dup() {
				return new Bool(this);
			}
			void accept(Walker walker) {
				walker.visit(this);
			}
			public String toString() {
				return "(Atom.Bool" + " " + bool + ")";
			}
		}
		public static class Str extends Atom {
			Term string;
			public Str(Term string) {
				(this.string = string).parent = this;
			}
			private Str(Str node) {
				(this.string = (Term) node.string.dup()).parent = this;
			}
			Node dup() {
				return new Str(this);
			}
			void accept(Walker walker) {
				walker.visit(this);
			}
			public String toString() {
				return "(Atom.Str" + " " + string + ")";
			}
		}
		public static class _Int extends Atom {
			Term _int;
			public _Int(Term _int) {
				(this._int = _int).parent = this;
			}
			private _Int(_Int node) {
				(this._int = (Term) node._int.dup()).parent = this;
			}
			Node dup() {
				return new _Int(this);
			}
			void accept(Walker walker) {
				walker.visit(this);
			}
			public String toString() {
				return "(Atom._Int" + " " + _int + ")";
			}
		}
		public static class _Float extends Atom {
			Term _float;
			public _Float(Term _float) {
				(this._float = _float).parent = this;
			}
			private _Float(_Float node) {
				(this._float = (Term) node._float.dup()).parent = this;
			}
			Node dup() {
				return new _Float(this);
			}
			void accept(Walker walker) {
				walker.visit(this);
			}
			public String toString() {
				return "(Atom._Float" + " " + _float + ")";
			}
		}
		public static class Symbol extends Atom {
			Term symbol;
			public Symbol(Term symbol) {
				(this.symbol = symbol).parent = this;
			}
			private Symbol(Symbol node) {
				(this.symbol = (Term) node.symbol.dup()).parent = this;
			}
			Node dup() {
				return new Symbol(this);
			}
			void accept(Walker walker) {
				walker.visit(this);
			}
			public String toString() {
				return "(Atom.Symbol" + " " + symbol + ")";
			}
		}
	}
	public static class Walker {
		void visit(Term node) {}
		boolean enter(ListOfExpr node) {
			return true;
		}
		void leave(ListOfExpr node) {}
		void visit(ListOfExpr node) {
			if (enter(node)) {
				for (Node e = node.first; e != null; e = e.next) {
					e.accept(this);
				}
				leave(node);
			}
		}
		boolean enter(Expr node) {
			return true;
		}
		void leave(Expr node) {}
		void visit(Expr node) {
			if (enter(node)) {
				node.atom.accept(this);
				node.exprList.accept(this);
				leave(node);
			}
		}
		boolean enter(ExprList node) {
			return true;
		}
		void leave(ExprList node) {}
		void visit(ExprList node) {
			if (enter(node)) {
				for (Node e = node.first; e != null; e = e.next) {
					e.accept(this);
				}
				leave(node);
			}
		}
		boolean enter(Atom.Bool node) {
			return true;
		}
		void leave(Atom.Bool node) {}
		void visit(Atom.Bool node) {
			if (enter(node)) {
				visit(node.bool);
				leave(node);
			}
		}
		boolean enter(Atom.Str node) {
			return true;
		}
		void leave(Atom.Str node) {}
		void visit(Atom.Str node) {
			if (enter(node)) {
				visit(node.string);
				leave(node);
			}
		}
		boolean enter(Atom._Int node) {
			return true;
		}
		void leave(Atom._Int node) {}
		void visit(Atom._Int node) {
			if (enter(node)) {
				visit(node._int);
				leave(node);
			}
		}
		boolean enter(Atom._Float node) {
			return true;
		}
		void leave(Atom._Float node) {}
		void visit(Atom._Float node) {
			if (enter(node)) {
				visit(node._float);
				leave(node);
			}
		}
		boolean enter(Atom.Symbol node) {
			return true;
		}
		void leave(Atom.Symbol node) {}
		void visit(Atom.Symbol node) {
			if (enter(node)) {
				visit(node.symbol);
				leave(node);
			}
		}
	}
}
