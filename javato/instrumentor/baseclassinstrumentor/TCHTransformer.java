package javato.instrumentor.baseclassinstrumentor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.Collections;

import javato.instrumentor.RecursiveVisitor;
import javato.instrumentor.Visitor;
import soot.ArrayType;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.LineNumberAdder;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.options.Options;
import soot.util.Chain;
import soot.util.JasminOutputStream;

public class TCHTransformer
{
    private static SootClass classFromType(Type t)
    {
        if (t instanceof RefType)
            return ((RefType) t).getSootClass();
        if (t instanceof ArrayType)
            return classFromType(((ArrayType) t).getElementType());
        return null;
    }

    private static boolean isSubclassOf(SootClass a, SootClass b)
    {
        while (a != null)
        {
            if (a.equals(b))
                return true;
            a = a.hasSuperclass() ? a.getSuperclass() : null;
        }
        return false;
    }

    private Map<String, String> classTransformations;

    private Map<String, String> unrootedClassTransformations;

    private Set<SootMethod> entryMethods;

    private Map<String, SootMethod> entryMethodWrappers;

    private Map<SootClass, Set<SootClass>> packProtectedDependencies;
    private Map<SootClass, Set<SootClass>> castedClasses;

    private Set<String> entryPoints;

    private String prefix;

    private Set<String> rootClasses;

    private Set<SootClass> rootedClasses;

    private Set<SootClass> unrootedClasses;

    private boolean shouldTCH = true;

    static final String defaultPrefix = "javato_tch_";

    public TCHTransformer()
    {
        entryPoints = new HashSet<String>();
        rootClasses = new HashSet<String>();
        rootClasses.add("java.lang.Object");
        rootClasses.add("java.lang.Throwable");
        rootClasses.add("java.io.Serializable");
        setPrefix(defaultPrefix);
    }

    public void addEntryPoint(String clName)
    {
        entryPoints.add(clName);
    }

    private void chooseRootedClasses(Collection<SootClass> classList)
    {
        SootClass runtimeException = Scene.v().loadClassAndSupport("java.lang.RuntimeException");
        rootedClasses = new HashSet<SootClass>();
        unrootedClasses = new HashSet<SootClass>();
        packProtectedDependencies = new HashMap<SootClass, Set<SootClass>>();
        castedClasses = new HashMap<SootClass, Set<SootClass>>();
        findEntryMethods();
        if (this.shouldTCH)
        {
            for (SootClass cls : classList)
            {
                System.out.println("Examining... " + cls.getName());
                RecursiveVisitor rv = new RecursiveVisitor(null);
                DependencyFinder df = new DependencyFinder(rv);
                rv.setNextVisitor(df);
                visit(df, cls);
                packProtectedDependencies.put(cls, df.getPackageProtectedDependencies());
                castedClasses.put(cls, df.getCastedClasses());
            }
            for (SootMethod meth : this.entryMethods)
                rootMethod(meth);
            for (String str : rootClasses)
                if (Scene.v().containsClass(str))
                    rootClass(Scene.v().getSootClass(str));
            if (this.shouldTCH)
            {
                for (SootClass cls : classList)
                {
                    if (rootedClasses.contains(cls))
                        continue;
                    if (isSubclassOf(cls, runtimeException))
                    {
                        rootClass(cls);
                        continue;
                    }
                    for (SootMethod meth : (Collection<SootMethod>) cls.getMethods())
                    {
                        if (meth.isNative())
                        {
                            rootClass(cls);
                            break;
                        }
                    }
                }
            }
            if (getPrefix().contains("."))
                rootPackProtected(classList);
        }

        for (SootClass sc : rootedClasses)
        {
            System.out.println(sc);
            if (canUnroot(sc))
            {
                System.out.println(sc + " can be unrooted!");
                unrootedClasses.add(sc);
            }
        }
        try
        {
            // System.in.read();
        }
        catch (Exception e)
        {
        }
    }

    private void rootPackProtected(Collection<SootClass> classList)
    {
        boolean changed = true;
        while (changed)
        {
            changed = false;
            for (SootClass sc : classList)
            {
                if (rootedClasses.contains(sc))
                    continue;
                for (SootClass prot : packProtectedDependencies.get(sc))
                {
                    if (rootedClasses.contains(prot))
                    {
                        changed = true;
                        rootClass(sc);
                        System.out.println("Had to root " + sc
                                + " because it used package-private features of rooted classes.");
                        break;
                    }
                }
            }
        }
    }

    private boolean rootedWithDependencies(SootClass sc)
    {
        if (!packProtectedDependencies.containsKey(sc))
            return false;
        for (SootClass prot : packProtectedDependencies.get(sc))
            if (rootedClasses.contains(prot))
                return true;
        return false;
    }

    private boolean canUnroot(SootClass cls)
    {
        if (true)
            return false; // Avoid unrooting until all issues can be resolved
        if (Modifier.isFinal(cls.getModifiers()))
            return false;
        if (rootedWithDependencies(cls))
            return false;
        if (isSubclassOf(cls, Scene.v().getSootClass("java.lang.Exception")))
            return false;
        for (Object meth : cls.getMethods())
        {
            SootMethod sm = (SootMethod) meth;
            if (Modifier.isFinal(sm.getModifiers()) || isPackageProtected(sm.getModifiers())
                    && (sm.isPublic() || sm.isProtected()) || sm.isNative())
                return false;
        }
        for (Object field : cls.getFields())
        {
            SootField f = (SootField) field;
            if ((f.isPublic() || f.isProtected()) && !Modifier.isFinal(f.getModifiers())
                    && !f.isStatic())
                return false;
        }
        return true;
    }

    private boolean isPackageProtected(int modifiers)
    {
        return !Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers)
                && !Modifier.isProtected(modifiers);
    }

    private Collection<SootMethod> findEntryMethods()
    {
        entryMethods = new HashSet<SootMethod>();
        for (String clName : entryPoints)
        {
            try
            {
                Collection<Method> marked = findMarkedMethods(clName);
                for (Method m : marked)
                {
                    System.out.println("Found entry point method: " + m.toGenericString());
                    SootMethod meth = sootMethodFromReflectedMethod(m);
                    entryMethods.add(meth);
                }
            }
            catch (ClassNotFoundException ex)
            {
                System.out.println("Could not find class: " + clName);
            }
        }
        return entryMethods;
    }

    private Collection<Method> findMarkedMethods(String clName) throws ClassNotFoundException
    {
        Method[] methods = Class.forName(clName).getMethods();
        Set<Method> marked = new HashSet<Method>();
        Set<Method> signature = new HashSet<Method>();
        for (Method m : methods)
        {
            if (m.isAnnotationPresent(CalledByInstrumentation.class))
                marked.add(m);
            if (Modifier.isStatic(m.getModifiers()) && m.getReturnType().equals(Void.TYPE))
                signature.add(m);
        }
        if (marked.size() > 0)
            return marked;
        return signature;
    }

    private String getPrefix()
    {
        return prefix;
    }

    private void visit(Visitor v, SootClass sc)
    {
        for (SootMethod sm : (Collection<SootMethod>) new ArrayList(sc.getMethods()))
        {
            try
            {
                sm.retrieveActiveBody();
            }
            catch (Exception e)
            {
            }
            v.visitMethodBegin(sm, sm.hasActiveBody() ? sm.getActiveBody().getUnits() : null);
            if (!sm.isNative() && !sm.isAbstract() && sm.hasActiveBody())
            {
                Chain units = sm.getActiveBody().getUnits();
                Iterator stmtIt = units.snapshotIterator();
                while (stmtIt.hasNext())
                {
                    Stmt s = (Stmt) stmtIt.next();
                    v.visitStmt(sm, units, s);
                }
            }
            v.visitMethodEnd(sm, null);
            if (!sm.isNative() && !sm.isAbstract() && sm.hasActiveBody())
            {
                try
                {
                    sm.getActiveBody().validate();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void linkTCHClasses(Collection<SootClass> produced)
    {
        System.out.println("Linking TCH Classes");
        wrapEntryMethods();
        RecursiveVisitor rv = new RecursiveVisitor(null);
        TCHVisitor tchv = new TCHVisitor(prefix, classTransformations,
                unrootedClassTransformations, entryMethods, entryMethodWrappers, rv);
        rv.setNextVisitor(tchv);
        for (SootClass sc : produced)
        {
            System.out.println("Transforming... " + sc);
            tchv.visitClass(sc);
            for (SootField sf : (Collection<SootField>) sc.getFields())
                tchv.visitField(sf);
            for (SootMethod sm : (Collection<SootMethod>) new ArrayList(sc.getMethods()))
            {
                String cName = sm.getDeclaringClass().getName();
                try
                {
                    sm.retrieveActiveBody();
                }
                catch (Exception e)
                {
                }
                tchv
                        .visitMethodBegin(sm, sm.hasActiveBody() ? sm.getActiveBody().getUnits()
                                : null);
                if (!sm.isNative() && !sm.isAbstract() && sm.hasActiveBody())
                {
                    Chain units = sm.getActiveBody().getUnits();
                    Iterator stmtIt = units.snapshotIterator();
                    while (stmtIt.hasNext())
                    {
                        Stmt s = (Stmt) stmtIt.next();
                        tchv.visitStmt(sm, units, s);
                    }
                }
                tchv.visitMethodEnd(sm, null);
                if (!sm.isNative() && !sm.isAbstract() && sm.hasActiveBody())
                {
                    try
                    {
                        sm.getActiveBody().validate();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Done Linking TCH Classes");
    }

    public void process(String outputFileName, boolean shouldTCH)
    {
        this.shouldTCH = shouldTCH;
        soot.options.Options.v().set_full_resolver(true);
        soot.options.Options.v().set_keep_line_number(true);
        Scene.v().setSootClassPath(
                System.getProperty("sun.boot.class.path") + File.pathSeparator
                        + System.getProperty("java.class.path"));
        Collection<SootClass> dependentClasses = getDependentClasses();
        chooseRootedClasses(dependentClasses);
        Collection<SootClass> produced = produceTCHClasses(Scene.v().getClasses(), shouldTCH);
        linkTCHClasses(produced);
        produceJar(outputFileName, produced);
        System.out.println("Done producing TCH");
    }

    public Collection<SootClass> getDependentClasses()
    {
        if (!shouldTCH)
        {
            for (String s : this.entryPoints)
            {
                SootClass sc = Scene.v().loadClassAndSupport(s);
                for (SootMethod sm : (Collection<SootMethod>) sc.getMethods())
                    sm.retrieveActiveBody();
            }
            return (Collection<SootClass>) Scene.v().getClasses();
        }
        Set<SootClass> classes = new HashSet<SootClass>();
        Stack<SootClass> stack = new Stack<SootClass>();
        for (String s : this.entryPoints)
        {
            SootClass sc = Scene.v().loadClassAndSupport(s);
            classes.add(sc);
            for (SootMethod sm : (Collection<SootMethod>) sc.getMethods())
            {
                sm.retrieveActiveBody();
            }
            stack.add(sc);
        }
        while (!stack.isEmpty())
        {
            SootClass curr = stack.pop();
            System.out.println("Exploring... " + curr);
            if (curr.hasSuperclass() && classes.add(curr.getSuperclass()))
                stack.add(curr.getSuperclass());
            for (SootField sf : (Collection<SootField>) curr.getFields())
            {
                Type t = sf.getType();
                if (t instanceof RefType)
                {
                    RefType rt = (RefType) t;
                    if (classes.add(rt.getSootClass()))
                        stack.add(rt.getSootClass());
                }
            }
            for (SootMethod sm : (Collection<SootMethod>) curr.getMethods())
            {
                Type t = sm.getReturnType();
                if (t instanceof RefType)
                {
                    RefType rt = (RefType) t;
                    if (classes.add(rt.getSootClass()))
                        stack.add(rt.getSootClass());
                }
                for (Type pt : (Collection<Type>) sm.getParameterTypes())
                {
                    if (pt instanceof RefType)
                    {
                        RefType rt = (RefType) pt;
                        if (classes.add(rt.getSootClass()))
                            stack.add(rt.getSootClass());
                    }
                }
                if (sm.isAbstract() || sm.isNative())
                    continue;
                Body b = sm.retrieveActiveBody();
                Collection<ValueBox> boxes = (Collection<ValueBox>) b.getUseAndDefBoxes();
                for (ValueBox vb : boxes)
                {
                    Type vbt = vb.getValue().getType();
                    if (vbt instanceof RefType)
                    {
                        RefType rt = (RefType) vbt;
                        if (classes.add(rt.getSootClass()))
                            stack.add(rt.getSootClass());
                    }
                    if (vb.getValue() instanceof InvokeExpr)
                    {
                        SootClass cl = ((InvokeExpr) vb.getValue()).getMethodRef().declaringClass();
                        if (classes.add(cl))
                            stack.add(cl);
                    }
                    if (vb.getValue() instanceof FieldRef)
                    {
                        SootClass cl = ((FieldRef) vb.getValue()).getFieldRef().declaringClass();
                        if (classes.add(cl))
                            stack.add(cl);
                    }
                }
            }
        }
        return classes;
    }

    public void produceJar(String fileName, Collection<SootClass> classes)
    {
        System.out.println("Writing to Jar");
        try
        {
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(fileName));
            for (SootClass sc : classes)
            {
                System.out.println("Writing... " + sc);
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Done writing to Jar");
    }

    private Set<String> libraryPackages;

    private boolean isLibraryClass(SootClass sc)
    {
        if (libraryPackages == null)
        {
            libraryPackages = new HashSet<String>();
            String[] paths = BaseClassInstrumentor.getBootClassPath().split(File.pathSeparator);
            for (String str : paths)
                libraryPackages.addAll(BaseClassInstrumentor.getAllPackagesIn(str).keySet());
        }
        return libraryPackages.contains(sc.getPackageName());
    }

    private String getPackageName(String str)
    {
        if (!str.contains("."))
            return "";
        return str.substring(0, str.lastIndexOf("."));
    }

    private Collection<SootClass> produceTCHClasses(Collection<SootClass> basesOrig,
            boolean shouldTranslate)
    {
        Set<String> entryPacks = new HashSet<String>();
        for (String str : entryPoints)
            entryPacks.add(getPackageName(str));
        ArrayList<SootClass> bases = new ArrayList<SootClass>(basesOrig);
        Collections.sort(bases, new Comparator<SootClass>()
        {
            public int compare(SootClass a, SootClass b)
            {
                return a.getName().compareTo(b.getName());
            }
        });
        System.out.println("Producing TCH classes");
        classTransformations = new HashMap<String, String>();
        unrootedClassTransformations = new HashMap<String, String>();
        Set<SootClass> newClasses = new HashSet<SootClass>();
        for (SootClass sc : bases)
        {
            SootClass produced = null;
            if (Exclusions.isExcludedTCHClass(sc.getName()))
            {
                classTransformations.put(sc.getName(), sc.getName());
                continue;
            }
            if (shouldTranslate)
            {
                if (entryPoints.contains(sc.getName()) || !isLibraryClass(sc) // entryPacks.contains(sc.getPackageName())
                /*
                 * (sc.getName().contains("$") && !classTransformations.get(
                 * sc.getName().substring(0,
                 * sc.getName().indexOf("$"))).startsWith( getPrefix()))
                 */)
                {
                    classTransformations.put(sc.getName(), sc.getName());
                    newClasses.add(produced = sc);
                }
                else if (rootedClasses.contains(sc))
                {
                    if (unrootedClasses.contains(sc))
                    {
                        unrootedClassTransformations.put(transformName(sc.getName()), sc.getName());
                        produced = produceTCHClass(newClasses, sc);
                        produced.setSuperclass(sc);
                    }
                    classTransformations.put(sc.getName(), sc.getName());
                }
                else
                {
                    produced = produceTCHClass(newClasses, sc);
                }
            }
            else
            {
                if (!isLibraryClass(sc))
                {
                    classTransformations.put(sc.getName(), sc.getName());
                    newClasses.add(produced = sc);
                }
            }
            if (produced != null)
                System.out.println("Producing... " + produced);
        }
        System.out.println("Done producing TCH classes");
        return newClasses;
    }

    private String transformName(String orig)
    {
        int dotIndex = orig.lastIndexOf('.');
        return orig.substring(0, dotIndex + 1) + getPrefix() + orig.substring(dotIndex + 1);
    }

    private SootClass produceTCHClass(Set<SootClass> newClasses, SootClass sc)
    {
        SootClass produced;
        classTransformations.put(sc.getName(), transformName(sc.getName()));
        SootClass nCls = new SootClass(classTransformations.get(sc.getName()), sc.getModifiers());
        nCls.setSuperclass(sc.hasSuperclass() ? sc.getSuperclass() : Scene.v().loadClassAndSupport(
                "java.lang.Object"));
        for (SootClass iface : (Collection<SootClass>) sc.getInterfaces())
            nCls.addInterface(iface);
        if (sc.hasOuterClass())
            nCls.setOuterClass(sc.getOuterClass());
        for (SootField field : (Collection<SootField>) sc.getFields())
            nCls.addField(new SootField(field.getName(), field.getType(), field.getModifiers()));
        for (SootMethod meth : (Collection<SootMethod>) sc.getMethods())
        {
            SootMethod product;
            nCls.addMethod(product = new SootMethod(meth.getName(), meth.getParameterTypes(), meth
                    .getReturnType(), meth.getModifiers(), meth.getExceptions()));
            if (!meth.isAbstract() && !meth.isNative())
                product.setActiveBody((Body) meth.retrieveActiveBody().clone());
        }
        newClasses.add(produced = nCls);
        Scene.v().addClass(nCls);
        return produced;
    }

    private void rootClass(SootClass cls)
    {
        if (cls == null)
            return;
        if (rootedClasses.contains(cls))
            return;
        rootedClasses.add(cls);
        if (cls.hasSuperclass())
            rootClass(cls.getSuperclass());
        for (SootClass inter : (Collection<SootClass>) cls.getInterfaces())
            rootClass(inter);
        for (SootMethod meth : (Collection<SootMethod>) cls.getMethods())
            rootMethod(meth);
        for (SootField field : (Collection<SootField>) cls.getFields())
            if (!field.isPrivate() && !field.isStatic())
                rootClass(classFromType(field.getType()));
        if (castedClasses.containsKey(cls))
            for (SootClass sc : castedClasses.get(cls))
                rootClass(sc);
    }

    private void rootMethod(SootMethod sm)
    {
        SootClass returnType = classFromType(sm.getReturnType());
        if (returnType != null)
            rootClass(returnType);
        for (Object typ : sm.getParameterTypes())
        {
            SootClass paramType = classFromType((Type) typ);
            if (paramType != null)
                rootClass(paramType);
        }
        for (Object typ : sm.getExceptions())
        {
            SootClass thrownType = (SootClass) typ;
            if (thrownType != null)
                rootClass(thrownType);
        }
    }

    private void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    private SootMethod sootMethodFromReflectedMethod(Method m)
    {
        String sig = "<" + m.getDeclaringClass().getName() + ": " + m.getReturnType().getName()
                + " " + m.getName() + "(";
        for (int x = 0; x < m.getParameterTypes().length; x++)
        {
            sig += m.getParameterTypes()[x].getName();
            if (x < m.getParameterTypes().length - 1)
                sig += ",";
        }
        sig += ")>";
        return Scene.v().getMethod(sig);
    }

    private void wrapEntryMethods()
    {
        entryMethodWrappers = new HashMap<String, SootMethod>();
        SootClass tracer = Scene.v().loadClassAndSupport(
                "javato.instrumentor.baseclassinstrumentor.Tracer");
        for (SootMethod sm : entryMethods)
        {
            Chain units;
            String name = sm.getName();
            sm.setName("javato_instrumented_" + sm.getName());
            SootMethod produced;
            sm.getDeclaringClass().addMethod(
                    produced = new SootMethod(name, sm.getParameterTypes(), sm.getReturnType(), sm
                            .getModifiers(), sm.getExceptions()));
            produced.setActiveBody(Jimple.v().newBody(produced));

            units = produced.getActiveBody().getUnits();
            SootMethod cfo = tracer.getMethodByName("calledFromObserver");
            SootMethod mark = tracer.getMethodByName("mark");
            SootMethod unmark = tracer.getMethodByName("unmark");
            Local l1 = Jimple.v().newLocal("javato_calledFromObserver", IntType.v());
            Stmt next = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mark.makeRef()));
            produced.getActiveBody().getLocals().add(l1);
            units.add(Jimple.v().newAssignStmt(l1, Jimple.v().newStaticInvokeExpr(cfo.makeRef())));
            units.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(l1, IntConstant.v(0)), next));
            units.add(Jimple.v().newReturnVoidStmt());
            units.add(next);
            Local thisL = Jimple.v().newLocal("thisLocal", sm.getDeclaringClass().getType());
            Local caught = Jimple.v().newLocal("caught",
                    Scene.v().loadClassAndSupport("java.lang.Throwable").getType());
            produced.getActiveBody().getLocals().add(thisL);
            produced.getActiveBody().getLocals().add(caught);
            List params = new ArrayList();
            for (int x = 0; x < produced.getParameterCount(); x++)
            {
                Local cur = Jimple.v().newLocal("paramLocal" + x, produced.getParameterType(x));
                produced.getActiveBody().getLocals().add(cur);
                units.add(Jimple.v().newIdentityStmt(cur,
                        Jimple.v().newParameterRef(produced.getParameterType(x), x)));
                params.add(cur);
            }
            if (produced.isStatic())
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newStaticInvokeExpr(sm.makeRef(), params)));
            else
            {
                units.add(Jimple.v().newIdentityStmt(thisL,
                        Jimple.v().newThisRef(produced.getDeclaringClass().getType())));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newInterfaceInvokeExpr(thisL, sm.makeRef(), params)));
            }
            units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(unmark.makeRef())));
            units.add(Jimple.v().newReturnVoidStmt());
            Stmt end = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(unmark.makeRef()));
            produced.getActiveBody().getTraps().add(
                    Jimple.v().newTrap(Scene.v().loadClassAndSupport("java.lang.Throwable"),
                            (Unit) units.getFirst(), (Unit) units.getLast(), end));
            units.add(end);
            units.add(Jimple.v().newIdentityStmt(caught, Jimple.v().newCaughtExceptionRef()));
            units.add(Jimple.v().newThrowStmt(caught));
            entryMethodWrappers.put(produced.getSignature(), sm);
        }
    }
}
