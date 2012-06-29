package com.stralos.shen;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class DirectClassLoader extends ClassLoader {
    private Map<String, byte[]> direct = new HashMap<String, byte[]>();


    public DirectClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void addClass(String key, byte[] cl) {
        direct.put(key, cl);
    }

    @Override
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> found;

        byte[] cls = this.direct.get(name);

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
        } else {
            if (true) {
                return super.loadClass(name, resolve);
            }
            try {
                // found = super.loadClass(name, false);
                // URL findResource = findResource(name.replace('.', '/'));
                // InputStream in = findResource.openStream();
                super.loadClass(name, false);
                InputStream in = getResourceAsStream(name);
                byte[] buff = new byte[10000000];
                int num = in.read(buff);
                // byte[] shorten = new byte[num];
                // System.arraycopy(buff, 0, shorten, 0, num);
                found = defineClass(name, buff, 0, num);

                System.out.println(name + " cl: " + found.getClassLoader());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return found;
    }
}