package com.stralos.shen;

import java.io.FileOutputStream;
import java.util.Map;

public class DirectClassLoader extends ClassLoader {
    private Map<String, byte[]> direct;
    private Environment env;

    public DirectClassLoader(Environment env, Map<String, byte[]> direct) {
        this.env = env;
        this.direct = direct;
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        Class<?> found;

        byte[] cls = this.direct.get(name);
        if (cls == null) {
            cls = env.globalClasses.get(name);
        }

        if (cls != null) {
            // System.err.println("================");
            // ClassReader cr = new ClassReader(cls);
            // cr.accept(new TraceClassVisitor(new PrintWriter(System.err)), 0);
            // System.err.println("================");

            try {
                FileOutputStream fos = new FileOutputStream("tmp/" + name + ".class");
                fos.write(cls);
                fos.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            found = defineClass(name, cls, 0, cls.length);
            env.globalClasses.put(name, cls);
            // GlobalContext.globalClasses.put(name, found);
        } else {
            found = super.findClass(name);
        }
        return found;
    }
}