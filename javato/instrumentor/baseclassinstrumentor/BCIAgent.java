package javato.instrumentor.baseclassinstrumentor;

import java.io.*;
import java.lang.reflect.*;
import java.lang.instrument.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.*;

import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Method;

public class BCIAgent
{
    private static Object initVMClasses;
    private static String jarFileName;

    public static Set<String> getInitialVMClasses()
    {
        return (Set<String>) initVMClasses;
    }

    public static void premain(String args, Instrumentation inst) throws Exception
    {
        try
        {
            String[] agentArgs;
            if (args == null)
                agentArgs = new String[] { "" };
            else
                agentArgs = args.split(",");
            if (!agentArgs[0].equals("instrumenting"))
                jarFileName = agentArgs[0];
            BaseClassTransformer rct = null;
            rct = new BaseClassTransformer();
            if (agentArgs[0].equals("instrumenting"))
            {
                initVMClasses = new HashSet<String>();
                for (Class<?> c : inst.getAllLoadedClasses())
                {
                    ((Set<String>) initVMClasses).add(c.getName());
                }
            }
            if (!agentArgs[0].equals("instrumenting"))
            {

                inst.addTransformer(rct);
                Tracer.setLocals(new CounterThreadLocal());
                Tracer.overrideAll(true);
                for (Class<?> c : inst.getAllLoadedClasses())
                {
                    try
                    {
                        if (c.isInterface())
                            continue;
                        if (c.isArray())
                            continue;
                        byte[] bytes = rct.getBytes(c.getName());
                        if (bytes == null)
                        {
                            continue;
                        }
                        inst
                                .redefineClasses(new ClassDefinition[] { new ClassDefinition(c,
                                        bytes) });
                    }
                    catch (Throwable e)
                    {
                        synchronized (System.err)
                        {
                            System.err.println("" + c + " failed...");
                            e.printStackTrace();
                        }
                    }
                }
                Runtime.getRuntime().addShutdownHook(new Thread()
                {
                    public void run()
                    {
                        Tracer.mark();
                        try
                        {
                            PrintStream ps = new PrintStream("bailout.txt");
                            ps.println("Bailouts: " + Tracer.getBailoutCount());
                            ps.close();
                        }
                        catch (Exception e)
                        {
                        }
                        Tracer.unmark();
                    }
                });
                if("true".equals(System.getProperty("bci.observerOn")))
                    Tracer.overrideAll(false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        synchronized (System.out)
        {
            System.out.println("Starting");
        }
        java.util.ArrayList<Integer> al = new java.util.ArrayList<Integer>();
        for (int x = 0; x < 1000; x++)
        {
            al.add(x);
        }
        java.util.TreeMap tm = new java.util.TreeMap();
        System.out.println("Done.");
    }

    public static class BaseClassTransformer implements ClassFileTransformer
    {
        private JarFile jf;

        public BaseClassTransformer()
        {
            try
            {
                if (jarFileName == null)
                    return;
                jf = new JarFile(jarFileName);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public byte[] getBytes(String className)
        {
            try
            {
                Tracer.mark();
                String realName = className.replace(".", "/");
                realName += ".class";
                JarEntry je = jf.getJarEntry(realName);
                InputStream is = jf.getInputStream(je);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[4096];
                while (is.available() > 0)
                {
                    int read = is.read(buff);
                    baos.write(buff, 0, read);
                }
                is.close();
                return baos.toByteArray();
            }
            catch (Exception e)
            {
            }
            finally
            {
                Tracer.unmark();
            }
            return null;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException
        {
            return getBytes(className);
        }
    }
}
