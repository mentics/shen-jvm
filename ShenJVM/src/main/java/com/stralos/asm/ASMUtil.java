package com.stralos.asm;

import static com.stralos.shen.Environment.ENV_PATH;
import static com.stralos.shen.Primitives.LAMBDA_METHOD_NAME;
import static com.stralos.shen.Primitives.LAMBDA_PATH_BASE;
import static com.stralos.shen.model.Loc.column;
import static com.stralos.shen.model.Loc.line;
import static com.stralos.shen.model.Loc.path;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.CheckClassAdapter;

import com.stralos.lang.Lambda;
import com.stralos.shen.Environment;
import com.stralos.shen.EvalContext;
import com.stralos.shen.FieldInfo;
import com.stralos.shen.Primitives;
import com.stralos.shen.VarInfo;
import com.stralos.shen.model.Loc;
import com.stralos.shen.model.Location;
import com.stralos.shen.model.S;
import com.stralos.shen.model.Symbol;

public class ASMUtil {
	private static final String ASMUTIL = ASMUtil.class.getName().replace('.',
			'/');
	public static final String FILE_LOCATION_PATH = "com/stralos/shen/parser/FileLocation";
	public static final String LOCATION_PATH = "com/stralos/shen/model/Location";

	public static void run(EvalContext context, String fullName, S s) {
		createLambdaClass(context, new VarInfo[0], fullName, s, new String[0]);
	}

	public static String constructorOfArity(int arity) {
		StringBuilder b = new StringBuilder(arity * 20);
		b.append('(');
		for (int i = 0; i < arity; i++) {
			b.append("Ljava/lang/Object;");
		}
		b.append(")V");
		return b.toString();
	}

	public static String signatureOfArity(int arity) {
		// It's a little extra big. 20 was higher than needful
		StringBuilder b = new StringBuilder(arity * 20);
		b.append('(');
		for (int i = 0; i < arity; i++) {
			b.append("Ljava/lang/Object;");
		}
		b.append(")Ljava/lang/Object;");
		return b.toString();
	}

	public static void loadGlobalFunctions(MethodVisitor mv) {
		mv.visitFieldInsn(GETSTATIC, ENV_PATH, "functions", "Ljava/util/Map;");
	}

	/**
	 * @param context
	 *            for just this lambda class. Don't use the same as the parent.
	 */
	public static void createLambdaClass(EvalContext context, VarInfo[] vars,
			String className, S body, String[] params) {
		ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, className, null,
				Primitives.LAMBDA_PATH_BASE + params.length, null);
		cv.visitSource(findSourcePath(body), null);

		for (int i = 0; i < vars.length; i++) {
			FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL
					+ ACC_SYNTHETIC, vars[i].valid, "Ljava/lang/Object;", null,
					null);
			fv.visitEnd();
		}

		MethodVisitor mv;

		{
			mv = cv.visitMethod(ACC_PUBLIC, "<init>",
					constructorOfArity(vars.length), null, null);
			mv.visitCode();
			Label begin = ASMUtil.visitLoc(mv, Loc.line(body));
			for (int i = 0; i < vars.length; i++) {
				mv.visitVarInsn(ALOAD, 0); // load "this"
				mv.visitVarInsn(ALOAD, i + 1); // load the value
				mv.visitFieldInsn(PUTFIELD, className, vars[i].valid, "L"
						+ vars[i].typePath + ";");
//				context.newLambdaField(new FieldInfo(className, "val$"
//						+ vars[i].name, "val$" + vars[i].valid));
				context.newLambdaField(new FieldInfo(className, vars[i].name, vars[i].valid));
			}
			Label construct = new Label();
			mv.visitLabel(construct);
			// mv.visitLineNumber(2, begin);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, Primitives.LAMBDA_PATH_BASE
					+ params.length, "<init>", "()V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			// mv.visitLineNumber(3, l1);
			mv.visitInsn(RETURN);
			Label end = new Label();
			mv.visitLabel(end);

			mv.visitLocalVariable("this", "L" + className + ";", null, begin,
					end, 0);
			for (int i = 0; i < vars.length; i++) {
				mv.visitLocalVariable("val" + i, "L" + vars[i].typePath + ";",
						null, begin, end, i + 1);
			}
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, Primitives.LAMBDA_METHOD_NAME,
					signatureOfArity(params.length), null, null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			context.skipLocalVarThis(); // for "this"
			int varOffset = context.getVarOffset();
			for (int i = 0; i < params.length; i++) {
				context.bindLocalVar(new VarInfo(i + varOffset, params[i],
						context.uniqueValidFieldName(params[i]), l0, l1));
			}
			mv.visitLabel(l0);

			body.visit(context, mv);

			mv.visitInsn(ARETURN);
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1,
					0);
			for (VarInfo var : context.getMethodLocalVars()) {
				// TODO: make less ugly
				if (!(var instanceof FieldInfo)) {
					// System.out.println("local var for: " + className + ", " +
					// var);
					mv.visitLocalVariable(var.valid, "L" + var.typePath + ";",
							null, var.beginLabel, var.endLabel, var.index);
				}
			}

			mv.visitMaxs(0, 0);
			// Don't need context.pop because end of method
			mv.visitEnd();
		}
		cv.visitEnd();

		byte[] byteArray = cv.toByteArray();
		ClassReader cr = new ClassReader(byteArray);
		try {
			System.err.println("Checking: "+className);
			cr.accept(new CheckClassAdapter(new ClassWriter(0), true), 0);
		} catch (Exception ae) {
			try {
				FileOutputStream out = new FileOutputStream("BadClass.class");
				out.write(byteArray);
				out.close();
				throw ae;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		context.addClass(className.replace('/', '.'), cv.toByteArray());
		// return context.getClasses();
	}

	private static String findSourcePath(S s) {
		return path(s.getLocation());
	}

	/**
	 * Converts all characters that are invalid in a Java identifier to _
	 */
	public static String toIdentifier(String string) {
		// TODO: do more efficiently

		String ret;
		switch (string) {
		case "=":
			ret = "equal";
			break;
		case "+":
			ret = "plus";
			break;
		case "-":
			ret = "minus";
			break;
		case "*":
			ret = "multiply";
			break;
		case "/":
			ret = "divide";
			break;
		case "<":
			ret = "lessThan";
			break;
		case ">":
			ret = "greaterThan";
			break;
		case "<=":
			ret = "lessThanOrEqual";
			break;
		case ">=":
			ret = "greaterThanOrEqual";
			break;
		case "if":
			ret = "if_";
			break;
		default:
			ret = toValidJava(string);
		}
		return ret;
	}

	public static String fromIdentifier(String string) {
		// TODO: do more efficiently

		String ret;
		switch (string) {
		case "equal":
			ret = "=";
			break;
		case "plus":
			ret = "+";
			break;
		case "minus":
			ret = "-";
			break;
		case "multiply":
			ret = "*";
			break;
		case "divide":
			ret = "/";
			break;
		case "lessThan":
			ret = "<";
			break;
		case "greaterThan":
			ret = ">";
			break;
		case "lessThanOrEqual":
			ret = "<=";
			break;
		case "greaterThanOrEqual":
			ret = ">=";
			break;
		case "if_":
			ret = "if";
			break;
		default:
			// throw new
			// RuntimeException("Invalid parameter to fromIdentifier: " +
			// string);
			ret = string;
		}
		return ret;
	}

	public static String toValidJava(String string) {
		String ret;
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				b.append('_');
			} else {
				b.append(c);
			}
		}
		ret = b.toString();
		return ret;
	}

	public static void visitArgs(EvalContext context, MethodVisitor mv, S[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i].visit(context, mv);
			// mv.visitInsn(DUP);
			// mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
			// "()Ljava/lang/String;");
			//
			// mv.visitLdcInsn(args[i].toString() + " = ");
			// mv.visitMethodInsn(INVOKESTATIC, ASMUTIL, "log",
			// "(Ljava/lang/String;)V");
			// mv.visitMethodInsn(INVOKESTATIC, ASMUTIL, "log",
			// "(Ljava/lang/String;)V");
		}
		// if (args.length > 0) {
		// mv.visitLdcInsn("\n");
		// mv.visitMethodInsn(INVOKESTATIC, ASMUTIL, "log",
		// "(Ljava/lang/String;)V");
		// }
	}

	public static void invokeToLambda(MethodVisitor mv) {
		mv.visitMethodInsn(INVOKESTATIC, ASMUTIL, "toLambda",
				"(Ljava/lang/Object;)Lcom/stralos/lang/Lambda;");
	}

	public static void invokeLambda(MethodVisitor mv, int arity) {
		mv.visitMethodInsn(INVOKEVIRTUAL, LAMBDA_PATH_BASE + arity,
				LAMBDA_METHOD_NAME, signatureOfArity(arity));
	}

	/**
	 * Either a lambda already or a symbol that should be associated with a
	 * function so should be in global functions or Primitives.
	 */
	public static Lambda toLambda(Object o) {
		Lambda result;
		if (o instanceof Lambda) {
			result = (Lambda) o;
		} else if (o instanceof Symbol) {
			result = Environment.functions.get(o.toString());
			if (result == null) {
				throw new RuntimeException(
						"Unknown symbol returned by first position s-expression: "
								+ o);
			}
		} else {
			throw new RuntimeException("Invalid type in first position: "
					+ o.getClass());
		}
		return result;
	}

	public static void throwExc(MethodVisitor mv, String msg) {
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn(msg);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException",
				"<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
	}

	public static Object getUserFunc(Object name) {
		Lambda lambda = Environment.functions.get(name);
		if (lambda == null) {
			throw new RuntimeException("Function " + name + " not found.");
		} else {
			return lambda;
		}
	}

	public static void invokeGetUserFunc(MethodVisitor mv, String funcName) {
		mv.visitLdcInsn(funcName);
		mv.visitMethodInsn(INVOKESTATIC, ASMUTIL, "getUserFunc",
				signatureOfArity(1));
	}

	@Deprecated
	public static Label visitLoc(MethodVisitor mv, int line) {
		Label invoke = new Label();
		mv.visitLabel(invoke);
		mv.visitLineNumber(line, invoke);
		return invoke;
	}

	public static Label visitLoc(MethodVisitor mv, S s) {
		Label invoke = new Label();
		mv.visitLabel(invoke);
		mv.visitLineNumber(line(s), invoke);
		return invoke;
	}

	public static void trace(MethodVisitor mv, String msg) {
		mv.visitLdcInsn(msg + "\n");
		mv.visitMethodInsn(INVOKESTATIC, ASMUTIL, "log",
				"(Ljava/lang/String;)V");
	}

	public static void visitCreateFileLocation(MethodVisitor mv, Location l) {
		mv.visitTypeInsn(NEW, FILE_LOCATION_PATH);
		mv.visitInsn(DUP);
		mv.visitLdcInsn(path(l));
		mv.visitIntInsn(BIPUSH, line(l));
		mv.visitIntInsn(BIPUSH, column(l));
		mv.visitMethodInsn(INVOKESPECIAL, FILE_LOCATION_PATH, "<init>",
				"(Ljava/lang/String;II)V");
	}

	public static Writer writer;
	static {
		try {
			writer = new FileWriter("trace.log", false);
			// writer = new PrintWriter(System.err);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void log(String s) {
		try {
			writer.append(s);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
