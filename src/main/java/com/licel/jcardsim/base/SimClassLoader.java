package com.licel.jcardsim.base;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;

public class SimClassLoader extends ClassLoader
{
    private ChildClassLoader childClassLoader;

    public SimClassLoader(ClassLoader parent)
    {
        super(parent);
        childClassLoader = null;
    }

    public SimClassLoader(List<URL> classpath)
    {
        super(Thread.currentThread().getContextClassLoader());
        URL[] urls = classpath.toArray(new URL[classpath.size()]);
        childClassLoader = new ChildClassLoader(urls, new DetectClass(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        Class<?> cls = loadClassSub(name, resolve);
        if (cls != null){
            return cls;
        }

        return super.loadClass(name, resolve);
    }

    protected Class<?> loadClassSub(String name, boolean resolve) {
        if (!name.startsWith("javacard.")) {
            return null;
        }

        System.err.println("Using custom classloader");
        try {
            if (childClassLoader != null) {
                return childClassLoader.findClass(name);

            } else {
                String pathJar = SimClassLoader.tryFindPathJar(null);
                if (pathJar == null) {
                    return null;
                }

                final URL[] urls = new URL[]{ new URL("file:" + pathJar) };
                final ChildClassLoader cldr = new ChildClassLoader(urls, new DetectClass(this.getParent()));
                return cldr.findClass(name);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
        } catch (MalformedURLException e) {
            e.printStackTrace(System.err);
        }

        return null;
    }

    private static class ChildClassLoader extends URLClassLoader
    {
        private DetectClass realParent;
        public ChildClassLoader( URL[] urls, DetectClass realParent )
        {
            super(urls, null);
            this.realParent = realParent;
        }
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException
        {
            try
            {
                Class<?> loaded = super.findLoadedClass(name);
                if( loaded != null )
                    return loaded;
                return super.findClass(name);
            }
            catch( ClassNotFoundException e )
            {
                return realParent.loadClass(name);
            }
        }
    }

    private static class DetectClass extends ClassLoader
    {
        public DetectClass(ClassLoader parent)
        {
            super(parent);
        }
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException
        {
            return super.findClass(name);
        }
    }

    /**
     * If the provided class has been loaded from a jar file that is on the local file system, will find the absolute path to that jar file.
     *
     * @param context The jar file that contained the class file that represents this class will be found. Specify {@code null} to let {@code LiveInjector}
     *                find its own jar.
     * @throws IllegalStateException If the specified class was loaded from a directory or in some other way (such as via HTTP, from a database, or some
     *                               other custom classloading device).
     */
    public static String findPathJar(Class<?> context) throws IllegalStateException {
        if (context == null) context = SimulatorRuntime.class;
        String rawName = context.getName();
        String classFileName;
        /* rawName is something like package.name.ContainingClass$ClassName. We need to turn this into ContainingClass$ClassName.class. */ {
            int idx = rawName.lastIndexOf('.');
            classFileName = (idx == -1 ? rawName : rawName.substring(idx+1)) + ".class";
        }

        String uri = context.getResource(classFileName).toString();
        if (uri.startsWith("file:")) throw new IllegalStateException("This class has been loaded from a directory and not from a jar file: " + uri);
        if (!uri.startsWith("jar:file:")) {
            int idx = uri.indexOf(':');
            String protocol = idx == -1 ? "(unknown)" : uri.substring(0, idx);
            throw new IllegalStateException("This class has been loaded remotely via the " + protocol +
                    " protocol. Only loading from a jar on the local file system is supported.");
        }

        int idx = uri.indexOf('!');
        //As far as I know, the if statement below can't ever trigger, so it's more of a sanity check thing.
        if (idx == -1) throw new IllegalStateException("You appear to have loaded this class from a local jar file, but I can't make sense of the URL!");

        try {
            String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx), Charset.defaultCharset().name());
            return new File(fileName).getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
            throw new InternalError("default charset doesn't exist. Your VM is broken.");
        }
    }

    public static String tryFindPathJar(Class<?> context){
        try {
            return findPathJar(context);
        } catch (Exception e){
            return null;
        }
    }
}
