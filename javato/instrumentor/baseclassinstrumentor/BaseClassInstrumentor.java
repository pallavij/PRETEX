package javato.instrumentor.baseclassinstrumentor;

import javato.instrumentor.RecursiveVisitor;
import javato.instrumentor.TransformClass;
import javato.instrumentor.Visitor;
import javato.observer.Observer;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.util.Chain;
import soot.util.JasminOutputStream;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class BaseClassInstrumentor {
    private static Map<String, String> jarContaining;
    private static Class<? extends Observer> observerClass;
    private static String observerClassName;
    private static String outputFileName;
    private static List<Class<? extends Visitor>> visitorClasses = new ArrayList<Class<? extends Visitor>>();
    private static List<String> visitorClassNames = new ArrayList<String>();

    public static String getBootClassPath() {
        java.lang.management.RuntimeMXBean mx = java.lang.management.ManagementFactory
                .getRuntimeMXBean();
        return mx.getBootClassPath();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Scene.v().setSootClassPath(getBootClassPath());
        for (String str : BCIAgent.getInitialVMClasses()) {
            if (str.startsWith("[") || str.startsWith("sun."))
                continue;
            System.out.println(str);
        }
        handleOptions(args);
        File tmpJarFolder = new File(System.getProperty("java.io.tmpdir") + File.separator
                + "javato_instrumentor_tmpjars_" + observerClassName + "_"
                + visitorClassNames.get(0) + visitorClassNames.size()
                + UUID.randomUUID().toString());
        if (!tmpJarFolder.mkdirs() && !tmpJarFolder.exists()) {
            System.err.println("ERROR: Cannot write to temporary directory: "
                    + System.getProperty("java.io.tmpdir"));
            System.exit(1);
        }
        tmpJarFolder.deleteOnExit();
        System.out
                .println("Instrumenting Java base class libraries.  This will take quite a long time.  Please be patient.");
        jarContaining = new HashMap<String, String>();
        Visitor.setObserverClass(observerClassName);
        RecursiveVisitor vv = new RecursiveVisitor(null);
        Visitor last = vv;
        for (int x = visitorClasses.size() - 1; x >= 0; x--) {
            Class<? extends Visitor> visitorClass = visitorClasses.get(x);
            try {
                last = visitorClass.getConstructor(Visitor.class).newInstance(last);
            }
            catch (Exception e) {
                System.err
                        .println("Unable to instantiate visitor.  Visitor must have a constructor\nof the form \"public <MyVisitorClass>(Visitor nextVisitor)\"");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        ExcludingVisitor ev = new ExcludingVisitor(last);
        vv.setNextVisitor(ev);
        TransformClass processor = new TransformClass();
        ArrayList<String> jarnames = performTransformation(tmpJarFolder, ev, processor);
        System.out.println(jarnames);
        mergeJarFiles(jarnames, outputFileName);
        recursiveDelete(tmpJarFolder);
        System.out.println("Done.");
    }

    private static Map<String, Set<String>> filterPackages(Map<String, Set<String>> input,
                                                           int filterLevel) {
        Map<String, Set<String>> names = new HashMap<String, Set<String>>();
        for (String str : input.keySet()) {
            // if (str.startsWith("sun.")
            /*
             * || !(str.startsWith("java.lang") || str .startsWith("java.util"))
             */// )
            // continue;
            int count = 0;
            for (int x = 0; x < str.length(); x++) {
                if (str.charAt(x) == '.') {
                    count++;
                }
            }
            if (count < filterLevel) {
                if (!names.containsKey(str))
                    names.put(str, new HashSet<String>());
                names.get(str).addAll(input.get(str));
            }
        }
        return names;
    }

    public static Map<String, Set<String>> getAllPackagesIn(String fileName) {
        Map<String, Set<String>> names = new HashMap<String, Set<String>>();
        if (!new File(fileName).exists()) {
            return names;
        }
        if (fileName.endsWith(".jar")) {
            try {
                JarFile jf = new JarFile(fileName);
                JarEntry je;
                Enumeration<JarEntry> en = jf.entries();
                while (en.hasMoreElements()) {
                    je = en.nextElement();
                    if (je.toString().endsWith(".class")) {
                        String result = je.toString().substring(0, je.toString().length() - 6);
                        String className = result = result.replace('/', '.');
                        result = result.substring(0, result.lastIndexOf('.'));
                        if (!names.containsKey(result))
                            names.put(result, new HashSet<String>());
                        names.get(result).add(className);
                    }
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            File top = new File(fileName);
            if (!top.isDirectory()) {
                throw new RuntimeException(
                        "Can only find packages in jars or directories of class files");
            }
            Stack<File> stack = new Stack<File>();
            stack.push(top);
            while (!stack.isEmpty()) {
                File cur = stack.pop();
                File[] children = cur.listFiles();
                for (File c : children) {
                    if (c.isDirectory()) {
                        stack.push(c);
                    } else if (c.getName().endsWith(".class")) {
                        String result = c.getParentFile().toString().substring(
                                top.toString().length());
                        result = result.replace('\\', '.');
                        String className = c.toString().substring(top.toString().length());
                        className = className.replace('\\', '.');
                        if (!names.containsKey(result))
                            names.put(result, new HashSet<String>());
                        names.get(result).add(className);
                    }
                }

            }
        }
        return names;
    }

    private static void handleOption(String flag, String value) throws Exception {
        // Visitor
        if (flag.equals("-vis")) {
            visitorClassNames.add(value);
            try {
                visitorClasses.add(Class.forName(value).asSubclass(Visitor.class));
            }
            catch (Exception e) {
                System.err
                        .println("Cannot find visitor class or visitor class does not extend javato.instrumentor.Visitor: "
                                + value);
                e.printStackTrace(System.err);
                throw e;
            }
        }
        // Observer
        else if (flag.equals("-obs")) {
            observerClassName = value;
            try {
                observerClass = Class.forName(observerClassName).asSubclass(Observer.class);
            }
            catch (Exception e) {
                System.err
                        .println("Cannot find observer class or observer class does not extend javato.observer.Observer: "
                                + observerClassName);
                e.printStackTrace(System.err);
                throw e;
            }
        } else if (flag.equals("-o")) {
            outputFileName = value;
            if (!outputFileName.toLowerCase().endsWith(".jar")) {
                System.err.println("Output file name must end with \".jar\"");
                throw new Exception();
            }
        } else if (flag.equals("-ex")) {
            FileInputStream fis;
            Exclusions.loadExclusions(fis = new FileInputStream(value));
            fis.close();
        } else {
            throw new Exception("Unknown flag: " + flag);
        }
    }

    private static void handleOptions(String[] args) {
        try {
            for (int x = 0; x < args.length; x += 2) {
                if (args[x].startsWith("-")) {
                    handleOption(args[x], args[x + 1]);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error processing arguments");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Map<String, String> listClassesInJar(String filename) {
        Map<String, String> classNames = new HashMap<String, String>();
        try {
            JarFile jf = new JarFile(filename);
            Enumeration<JarEntry> enumeration = jf.entries();
            for (JarEntry entry = enumeration.nextElement(); enumeration.hasMoreElements(); entry = enumeration
                    .nextElement()) {
                if (entry.getName().endsWith(".class")) {
                    String name = entry.getName().replace('/', '.').replace('\\', '.');
                    name = name.substring(0, name.length() - 6);
                    classNames.put(name, entry.getName());
                }
            }
        }
        catch (Exception e) {
        }
        return classNames;
    }

    private static SootClass makeWrappingClass(byte[] bytes, String className) {
        SootClass obj = Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.String");
        String realClassName = className.replace(".", "__");
        realClassName = realClassName.replace("$", "___");
        realClassName = "javato.instrumented." + realClassName;
        SootClass sc = new SootClass(realClassName, Modifier.PUBLIC);
        sc.setSuperclass(obj);
        Scene.v().addClass(sc);
        SootMethod m = new SootMethod("get", Arrays.asList(new Type[]{}), ArrayType.v(
                ByteType.v(), 1), Modifier.PUBLIC | Modifier.STATIC);
        JimpleBody body = Jimple.v().newBody(m);
        m.setActiveBody(body);
        Local stringLocal, byteLocal;
        body.getLocals().add(
                stringLocal = Jimple.v().newLocal("str", RefType.v("java.lang.String")));
        body.getLocals().add(byteLocal = Jimple.v().newLocal("data", ArrayType.v(ByteType.v(), 1)));
        Chain units = body.getUnits();
        String val = java.nio.ByteBuffer.wrap(bytes).asCharBuffer().toString();
        int start = 0;
        final int chunkSize = 1024 * 10;
        units.add(Jimple.v().newAssignStmt(stringLocal, StringConstant.v("")));
        SootMethod strcat = Scene.v().getMethod(
                "<java.lang.String: java.lang.String concat(java.lang.String)>");
        while (start < val.length()) {
            String cur = val.substring(start, Math.min(start + chunkSize, val.length()));
            start += chunkSize;
            units.add(Jimple.v().newAssignStmt(
                    stringLocal,
                    Jimple.v().newVirtualInvokeExpr(stringLocal, strcat.makeRef(),
                            StringConstant.v(cur))));
        }

        units.add(Jimple.v().newAssignStmt(
                byteLocal,
                Jimple.v().newVirtualInvokeExpr(stringLocal,
                        Scene.v().getMethod("<java.lang.String: byte[] getBytes()>").makeRef())));
        units.add(Jimple.v().newReturnStmt(byteLocal));
        sc.addMethod(m);
        return sc;
    }

    private static SootClass makeProvisioningClass(Collection<SootClass> classes) {
        SootClass obj = Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.String");
        SootClass iFace = Scene.v().loadClassAndSupport(
                "javato.instrumentor.baseclassinstrumentor.ClassDataProvisioner");
        String realClassName = "javato.instrumented.Provisioner";
        SootClass sc = new SootClass(realClassName, Modifier.PUBLIC);
        sc.setSuperclass(obj);
        sc.addInterface(iFace);
        Scene.v().addClass(sc);
        SootMethod m = new SootMethod("get", Arrays.asList(new Type[]{RefType
                .v("java.lang.String")}), ArrayType.v(ByteType.v(), 1), Modifier.PUBLIC);
        SootMethod constructor = new SootMethod("<init>", Arrays.asList(new Type[]{}), VoidType
                .v(), Modifier.PUBLIC);
        constructor.setActiveBody(Jimple.v().newBody(constructor));
        constructor.getActiveBody().getUnits().addFirst(Jimple.v().newReturnVoidStmt());
        sc.addMethod(constructor);
        JimpleBody body = Jimple.v().newBody(m);
        m.setActiveBody(body);
        Local clName, bool, data;
        body.getLocals().add(
                clName = Jimple.v().newLocal("className", RefType.v("java.lang.String")));
        body.getLocals().add(bool = Jimple.v().newLocal("tmpBool", BooleanType.v()));
        body.getLocals().add(data = Jimple.v().newLocal("data", ArrayType.v(ByteType.v(), 1)));
        Chain units = body.getUnits();
        units.add(Jimple.v().newIdentityStmt(clName,
                Jimple.v().newParameterRef(RefType.v("java.lang.String"), 0)));
        Unit last;
        units.addLast(last = Jimple.v().newReturnStmt(NullConstant.v()));

        SootMethod strEqls = Scene.v().getMethod(
                "<java.lang.String: boolean equals(java.lang.Object)>");

        for (SootClass c : classes) {
            String name = c.getName();
            name = name.substring("javato.instrumented.".length());
            name = name.replace("___", "$").replace("__", ".");

            Stmt doCompare = Jimple.v().newAssignStmt(
                    bool,
                    Jimple.v().newVirtualInvokeExpr(clName, strEqls.makeRef(),
                            StringConstant.v(name)));
            SootMethod method = c.getMethod("byte[] get()");
            Stmt getData = Jimple.v().newAssignStmt(data,
                    Jimple.v().newStaticInvokeExpr(method.makeRef()));
            Stmt ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(bool, IntConstant.v(0)), last);
            Stmt retStmt = Jimple.v().newReturnStmt(data);
            units.addFirst(retStmt);
            units.addFirst(getData);
            units.addFirst(ifStmt);
            units.addFirst(doCompare);
            last = doCompare;
        }
        sc.addMethod(m);
        return sc;
    }

    private static void makeJarFromProvisions(SootClass provisioner,
                                              Collection<SootClass> underlings, String jarName) {
        try {
            ArrayList<SootClass> fullList = new ArrayList<SootClass>(underlings);
            fullList.add(provisioner);
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarName));
            for (SootClass sc : fullList) {
                String name = SourceLocator.v().getFileNameFor(sc, Options.output_format_class);
                name = name.replace('\\', '/').substring(
                        SourceLocator.v().getOutputDir().length() + 1);
                JasminOutputStream jasOut = new JasminOutputStream(jos);
                jos.putNextEntry(new JarEntry(name));
                PrintWriter pw = new PrintWriter(jasOut);
                JasminClass jmc = new JasminClass(sc);
                jmc.print(pw);
                pw.flush();
                jos.closeEntry();
            }
            jos.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void mergeJarFiles(Collection<String> filenames, String outputFile) {
        ArrayList<SootClass> containingClasses = new ArrayList<SootClass>();
        try {
            JarOutputStream jos = null;
            try {
                Set<String> entries = new HashSet<String>();
                jos = new JarOutputStream(new FileOutputStream(outputFile));
                for (String cur : filenames) {
                    if (!new File(cur).exists()) {
                        System.out.println("Could not find jar: " + cur);
                        continue;
                    }
                    JarInputStream jis = null;
                    try {
                        jis = new JarInputStream(new FileInputStream(cur));
                        JarEntry ze;
                        Set<String> origVMClasses = BCIAgent.getInitialVMClasses();
                        while ((ze = jis.getNextJarEntry()) != null) {
                            // System.out.println(ze.getName());
                            String classFileName = ze.getName().replace('\\', '/');
                            String className = null;
                            if (classFileName.endsWith(".class")) {
                                className = classFileName.substring(0,
                                        classFileName.length() - ".class".length()).replace("/",
                                        ".");
                            }

                            boolean shouldContinue = entries.add(classFileName)
                                    && !Exclusions.isExcludedClass(className);
                            if (shouldContinue) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                if (className != null
                                        && Exclusions
                                        .isClassAttributeModificationExcluded(className)
                                        && jarContaining.containsKey(classFileName)) {
                                    // System.out.println("Fixing class: "
                                    // + className);
                                    byte[] bytes = revertFlags(jarContaining.get(classFileName),
                                            cur, classFileName, ze.getName());
                                    if (schemaChanged(jarContaining.get(classFileName), cur,
                                            classFileName, ze.getName()))
                                        continue;
                                    ze = new JarEntry(classFileName);
                                    jos.putNextEntry(ze);
                                    jos.write(bytes);
                                    baos.write(bytes);
                                    jos.closeEntry();
                                } else {
                                    ze = new JarEntry(classFileName);
                                    jos.putNextEntry(ze);
                                    int read;
                                    byte[] bytes = new byte[1024];
                                    while (jis.available() > 0) {
                                        read = jis.read(bytes);
                                        if (read < 0) {
                                            break;
                                        }
                                        jos.write(bytes, 0, read);
                                        baos.write(bytes, 0, read);
                                    }
                                    jis.closeEntry();
                                    jos.closeEntry();
                                }
                                // containingClasses.add(makeWrappingClass(baos
                                // .toByteArray(), className));
                            }
                        }
                    }
                    catch (Exception e) {
                        throw e;
                    }
                    finally {
                        if (jis != null) {
                            jis.close();
                        }
                    }
                }
            }
            catch (Exception e) {
                throw e;
            }
            finally {
                if (jos != null) {
                    jos.close();
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        // SootClass provisioner = makeProvisioningClass(containingClasses);
        // makeJarFromProvisions(provisioner, containingClasses, "1" +
        // outputFile);
    }

    private static boolean shouldProcess(Set<String> classNames) {
        for (String clss : classNames)
            if (Exclusions.isIncludedByConfig(clss))
                return true;
        return false;
    }

    private static ArrayList<String> performTransformation(File tmpJarFolder, Visitor visitor,
                                                           TransformClass processor) {
        ArrayList<String> arglist = new ArrayList<String>();
        arglist.add("-cp");
        arglist.add(getBootClassPath());
        for (String str : ("-keep-line-number -output-jar -x javato").split(" ")) {
            arglist.add(str);
        }
        ArrayList<String> jarnames = new ArrayList<String>();
        ArrayList<String> origJarnames = new ArrayList<String>();
        for (String str : getBootClassPath().split("" + java.io.File.pathSeparatorChar)) {
            origJarnames.add(str);
            arglist.add("-process-dir");
            arglist.add(str);
            Map<String, String> classFilesInJar = listClassesInJar(str);
            for (String entry : classFilesInJar.values()) {
                jarContaining.put(entry, str);
            }
            System.out.println("Discovered packages:");
            Map<String, Set<String>> packClasses = getAllPackagesIn(str);
            Map<String, Set<String>> filteredPackages = filterPackages(packClasses, 2);
            for (String pkg : filteredPackages.keySet()) {
                if (!shouldProcess(filteredPackages.get(pkg)))
                    continue;
                System.out.println(pkg);
            }
            int count = 0;
            for (String pkg : filteredPackages.keySet()) {
                count++;
                if (!shouldProcess(filteredPackages.get(pkg))) {
                    System.out.println("Skipping package: " + pkg + " in " + str + " (" + count
                            + " of " + filteredPackages.keySet().size() + ")");
                    continue;
                }
                System.out.println("Processing package: " + pkg + " in " + str + " (" + count
                        + " of " + filteredPackages.keySet().size() + ")");
                ArrayList<String> temp = new ArrayList<String>(arglist);
                temp.add("-d");
                String name = tmpJarFolder.getPath() + File.separator + "javato_" + pkg + ".jar";
                jarnames.add(name);
                temp.add(name);
                temp.add("-i");

                temp.add(pkg);
                temp.add("-x");
                temp.add("");
                for (String otherPkg : filteredPackages.keySet()) {
                    if (!otherPkg.equals(pkg)) {
                        temp.add("-x");
                        temp.add(otherPkg);
                    }
                }
                for (String pkg2 : packClasses.keySet()) {
                    if (pkg2.startsWith(pkg)) {
                        if (!shouldProcess(packClasses.get(pkg2)))
                            temp.add("-x");
                        else
                            temp.add("-i");
                        temp.add(pkg2);
                    }
                }
                System.out.println(temp);

                processor.processAllAtOnce(temp.toArray(new String[0]), visitor);

                G.reset();

                System.gc();
            }
            arglist.remove(arglist.size() - 1);
            arglist.remove(arglist.size() - 1);

        }
        return jarnames;
    }

    private static void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                recursiveDelete(child);
            }
        }
        f.delete();
    }

    private static void revertFlags(JavaClass jcOld, JavaClass jcInst) {
        HashMap<String, Integer> origMapping = new HashMap<String, Integer>();
        for (Method m : jcOld.getMethods()) {
            origMapping.put(m.getName() + m.getSignature(), m.getAccessFlags());
        }
        for (Field f : jcOld.getFields()) {
            origMapping.put(f.getName() + f.getSignature(), f.getAccessFlags());
        }
        for (Method m : jcInst.getMethods()) {
            if (origMapping.containsKey(m.getName() + m.getSignature())) {
                m.setAccessFlags(origMapping.get(m.getName() + m.getSignature()).intValue());
            }
        }
        for (Field f : jcInst.getFields()) {
            if (origMapping.containsKey(f.getName() + f.getSignature())) {
                f.setAccessFlags(origMapping.get(f.getName() + f.getSignature()).intValue());
            }
        }
        jcInst.setAccessFlags(jcOld.getAccessFlags());
    }

    private static boolean schemaChanged(String oldJar, String newJar, String classNameOld,
                                         String classNameNew) {
        try {
            ClassParser cpOld = new ClassParser(oldJar, classNameOld);
            ClassParser cpNew = new ClassParser(newJar, classNameNew);
            JavaClass jcInst = cpNew.parse();
            JavaClass jcOld = cpOld.parse();
            // TODO: Add code to check schema
            return false;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] revertFlags(String oldJar, String newJar, String classNameOld,
                                      String classNameNew) {
        try {
            ClassParser cpOld = new ClassParser(oldJar, classNameOld);
            ClassParser cpNew = new ClassParser(newJar, classNameNew);
            JavaClass jcInst = cpNew.parse();
            revertFlags(cpOld.parse(), jcInst);
            return jcInst.getBytes();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
