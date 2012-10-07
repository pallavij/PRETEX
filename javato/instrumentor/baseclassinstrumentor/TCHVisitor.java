package javato.instrumentor.baseclassinstrumentor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import java.util.HashSet;

import javato.instrumentor.Visitor;
import javato.instrumentor.contexts.InvokeContext;
import javato.instrumentor.contexts.LocalContext;
import javato.instrumentor.contexts.LocalOrConstantContext;
import javato.instrumentor.contexts.RefContext;
import javato.instrumentor.contexts.TypeContext;
import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.ConcreteRef;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.util.Chain;
import soot.util.NumberedString;

public class TCHVisitor extends Visitor
{
    private Map<String, String> classMapping;

    private Map<String, String> unrootedClassMapping;
    private Map<String, String> invUnrootedClassMapping;

    private Set<SootMethod> entryMethods;

    private Map<String, SootMethod> entryWrappers;

    private Set<Value> thisRefs;

    private String prefix;

    public TCHVisitor(String prefix, Map<String, String> classMapping,
            Map<String, String> unrootedClassMapping, Set<SootMethod> entryMethods,
            Map<String, SootMethod> wrappers, Visitor nextVisitor)
    {
        super(nextVisitor);
        thisRefs = new HashSet<Value>();
        this.classMapping = classMapping;
        this.prefix = prefix;
        this.entryMethods = entryMethods;
        this.entryWrappers = wrappers;
        this.unrootedClassMapping = unrootedClassMapping;
        invUnrootedClassMapping = new HashMap<String, String>();
        for (String key : unrootedClassMapping.keySet())
            invUnrootedClassMapping.put(unrootedClassMapping.get(key), key);
    }

    private SootField findField(SootClass sc, String name)
    {
        SootClass origSc = sc;
        while (sc != null)
        {
            if (sc.declaresFieldByName(name))
                return sc.getFieldByName(name);
            sc = sc.hasSuperclass() ? sc.getSuperclass() : null;
        }
        sc = origSc;
        if (sc.hasOuterClass())
            return findField(sc.getOuterClass(), name);
        return null;
    }

    private SootMethod findMethod(SootClass sc, NumberedString subSig)
    {
        SootClass origSc = sc;
        while (sc != null)
        {
            for (SootMethod sm : (Collection<SootMethod>) sc.getMethods())
            {
                if (sm.getSubSignature().equals(subSig.toString()))
                    return sm;
            }
            if (sc.declaresMethod(subSig.toString()))
                return sc.getMethod(subSig.toString());
            sc = sc.hasSuperclass() ? sc.getSuperclass() : null;
        }
        sc = origSc;
        for (SootClass intfc : (Collection<SootClass>) sc.getInterfaces())
        {
            SootMethod meth = findMethod(intfc, subSig);
            if (meth != null)
                return meth;
        }
        if (sc.hasOuterClass())
            return findMethod(sc.getOuterClass(), subSig);
        return null;
    }

    private SootClass transform(SootClass in)
    {
        if (in.getName().substring(in.getName().lastIndexOf('.')).startsWith(prefix))
            return in;
        if (!classMapping.containsKey(in.getName()))
            return in;
        return Scene.v().getSootClass(classMapping.get(in.getName()));
    }

    private void transformType(Type t)
    {
        if (t instanceof RefType)
        {
            ((RefType) t).setSootClass(transform(((RefType) t).getSootClass()));
            ((RefType) t).setClassName(((RefType) t).getSootClass().getName());
        }
        if (t instanceof ArrayType)
            transformType(((ArrayType) t).getElementType());
    }

    @Override
    public void visitCaughtExceptionRef(SootMethod sm, Chain units, IdentityStmt s,
            CaughtExceptionRef caughtExceptionRef)
    {
        transformType(caughtExceptionRef.getType());
        super.visitCaughtExceptionRef(sm, units, s, caughtExceptionRef);
    }

    public void visitClass(SootClass sc)
    {
        if (unrootedClassMapping.containsKey(sc.getName()))
        {
            // sc.setSuperclass(Scene.v().getSootClass(unrootedClassMapping.get(sc.getName())));
            return;
        }
        if (sc.hasSuperclass())
        {
            if (invUnrootedClassMapping.containsKey(sc.getSuperclass().getName()))
            {
                sc.setSuperclass(Scene.v().getSootClass(
                        invUnrootedClassMapping.get(sc.getSuperclass().getName())));
            }
            else
                sc.setSuperclass(transform(sc.getSuperclass()));
        }
        if (sc.hasOuterClass())
            sc.setOuterClass(transform(sc.getOuterClass()));
        for (Iterator it = sc.getInterfaces().snapshotIterator(); it.hasNext();)
        {
            SootClass intfc = (SootClass) it.next();
            sc.removeInterface(intfc);
            sc.addInterface(transform(intfc));
        }
    }

    @Override
    public void visitConcreteRef(SootMethod sm, Chain units, Stmt s, ConcreteRef concreteRef,
            RefContext context)
    {
        transformType(concreteRef.getType());
        super.visitConcreteRef(sm, units, s, concreteRef, context);
    }

    public void visitField(SootField sf)
    {
        transformType(sf.getType());
    }

    @Override
    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s,
            InstanceFieldRef instanceFieldRef, RefContext context)
    {
        SootClass fieldType;
        if (unrootedClassMapping.containsKey(sm.getDeclaringClass().getName())
                && instanceFieldRef.getFieldRef().declaringClass().equals(
                        sm.getDeclaringClass().getSuperclass()))
            fieldType = sm.getDeclaringClass();
        else

            fieldType = transform(instanceFieldRef.getFieldRef().declaringClass());
        SootField newField = findField(fieldType, instanceFieldRef.getFieldRef().name());

        instanceFieldRef.setFieldRef(newField.makeRef());
        transformType(instanceFieldRef.getType());
        super.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
    }

    private boolean isThisConstructor(InstanceInvokeExpr expr)
    {
        if (!(expr instanceof SpecialInvokeExpr))
            return false;
        if (!expr.getMethodRef().name().equals("<init>"))
            return false;
        if (thisRefs.contains(expr.getBase()))
            return true;
        return false;
    }

    @Override
    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s,
            InstanceInvokeExpr invokeExpr, InvokeContext context)
    {
        super.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);
        try
        {
            if (entryMethods.contains(invokeExpr.getMethodRef().getSignature()))
            {
                invokeExpr.setMethodRef(entryWrappers.get(invokeExpr.getMethodRef().getSignature())
                        .makeRef());
                transformType(invokeExpr.getType());
                return;
            }
        }
        catch (Exception e)
        {
        }
        SootClass methType;
        if (unrootedClassMapping.containsKey(sm.getDeclaringClass().getName())
                && !isThisConstructor(invokeExpr)
                && invokeExpr.getMethodRef().declaringClass().equals(
                        sm.getDeclaringClass().getSuperclass()))
        {
            methType = sm.getDeclaringClass();
        }
        else

            methType = transform(invokeExpr.getMethodRef().declaringClass());
        SootMethod newMethod = findMethod(methType, invokeExpr.getMethodRef().getSubSignature());
        invokeExpr.setMethodRef(newMethod.makeRef());
        transformType(invokeExpr.getType());
    }

    @Override
    public void visitInstanceOfExpr(SootMethod sm, Chain units, Stmt s,
            InstanceOfExpr instanceOfExpr)
    {
        transformType(instanceOfExpr.getCheckType());
        transformType(instanceOfExpr.getType());
        super.visitInstanceOfExpr(sm, units, s, instanceOfExpr);
    }

    @Override
    public void visitLocal(SootMethod sm, Chain units, Stmt s, Local local, LocalContext context)
    {
        transformType(local.getType());
        super.visitLocal(sm, units, s, local, context);
    }

    private InvokeStmt findConstructorCall(Chain units)
    {
        Iterator i = units.iterator();
        Value thisVal = null;
        while (i.hasNext())
        {
            Object next = i.next();
            if (next instanceof IdentityStmt)
            {
                IdentityStmt asgn = (IdentityStmt) next;
                if (asgn.getRightOp() instanceof ThisRef)
                    thisVal = asgn.getLeftOp();
            }
            if (next instanceof InvokeStmt)
            {
                InvokeStmt stmt = (InvokeStmt) next;
                if (stmt.getInvokeExpr() instanceof SpecialInvokeExpr)
                {
                    SpecialInvokeExpr sie = (SpecialInvokeExpr) stmt.getInvokeExpr();
                    if (sie.getBase().equivTo(thisVal))
                    {
                        return stmt;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void visitMethodBegin(SootMethod sm, Chain units)
    {
        transformType(sm.getReturnType());
        thisRefs.clear();
        List l = new ArrayList();
        for (Type rt : (Collection<Type>) sm.getParameterTypes())
            transformType(rt);
        // sm.setParameterTypes(l);
        l = new ArrayList();
        for (SootClass sc : (Collection<SootClass>) sm.getExceptions())
            l.add(transform(sc));
        sm.setExceptions(l);
        Chain origUnits = units;

        if (sm.getName().equals("<init>")
                && unrootedClassMapping.containsKey(sm.getDeclaringClass().getName())
                && units != null)
        {
            InvokeStmt stmt = findConstructorCall(units);
            SpecialInvokeExpr invoke = (SpecialInvokeExpr) stmt.getInvokeExpr();
            if (invoke.getMethod().getDeclaringClass().equals(
                    sm.getDeclaringClass().getSuperclass().getSuperclass()))
            {
                SootMethod superConstructor = sm.getDeclaringClass().getSuperclass().getMethod(
                        sm.getSubSignature());
                ArrayList args = new ArrayList();
                int count = 0;
                for (Type t : (List<Type>) sm.getParameterTypes())
                {
                    Local loc = Jimple.v().newLocal("tch_paramVal_" + count, t);
                    sm.getActiveBody().getLocals().add(loc);
                    units.addFirst(Jimple.v().newIdentityStmt(loc,
                            Jimple.v().newParameterRef(t, count)));
                    args.add(loc);
                    count++;
                }
                stmt.setInvokeExpr(Jimple.v().newSpecialInvokeExpr((Local) invoke.getBase(),
                        superConstructor.makeRef(), args));
                System.out.println(stmt);
                System.out.println("Adjusting a constructor in " + sm.getSignature() + " "
                        + sm.getDeclaringClass().getName());
            }
            else if (invoke.getMethod().getDeclaringClass().equals(
                    sm.getDeclaringClass().getSuperclass()))
            {
                SootMethod myConstructor = sm.getDeclaringClass().getMethod(
                        invoke.getMethodRef().getSubSignature());
                invoke.setMethodRef(myConstructor.makeRef());
                System.out.println(stmt);
                System.out.println("Adjusting a constructor in " + sm.getSignature() + " "
                        + sm.getDeclaringClass().getName());
            }

        }

        super.visitMethodBegin(sm, origUnits);
    }

    private ValueBox findContainingValueBox(Unit u, Value v)
    {
        for(ValueBox vb : (List<ValueBox>)u.getUseBoxes())
        {
            if(vb.getValue().equivTo(v))
                return vb;
        }
        for(UnitBox child : (List<UnitBox>)u.getUnitBoxes())
        {
            ValueBox vb = findContainingValueBox(child.getUnit(), v);
            if(vb != null)
                return vb;
        }
        return null;
    }
    
    @Override
    public void visitConstant(SootMethod sm, Chain units, Stmt s, Constant constant,
            LocalOrConstantContext context)
    {
        if (constant instanceof ClassConstant)
        {
            ClassConstant c = (ClassConstant) constant;
            if (this.classMapping.containsKey(c.getValue().replace('/', '.')))
            {
                ValueBox vb = findContainingValueBox(s, c);
                vb.setValue(ClassConstant.v(classMapping.get(c.getValue().replace('/', '.')).replace('.', '/')));
            }
        }
        super.visitConstant(sm, units, s, constant, context);
    }

    @Override
    public void visitNewArrayExpr(SootMethod sm, Chain units, Stmt s, NewArrayExpr newArrayExpr)
    {
        transformType(newArrayExpr.getBaseType());
        transformType(newArrayExpr.getType());
        super.visitNewArrayExpr(sm, units, s, newArrayExpr);
    }

    @Override
    public void visitNewExpr(SootMethod sm, Chain units, Stmt s, NewExpr newExpr)
    {
        String typeName = newExpr.getBaseType().getSootClass().getName();
        if (invUnrootedClassMapping.containsKey(typeName))
        {
            RefType t = newExpr.getBaseType();
            RefType dup = RefType.v(Scene.v().getSootClass(invUnrootedClassMapping.get(typeName)));
            newExpr.setBaseType(dup);
        }
        else
        {
            transformType(newExpr.getBaseType());
            transformType(newExpr.getType());
        }
        super.visitNewExpr(sm, units, s, newExpr);
    }

    @Override
    public void visitNewMultiArrayExpr(SootMethod sm, Chain units, Stmt s,
            NewMultiArrayExpr newMultiArrayExpr)
    {
        transformType(newMultiArrayExpr.getType());
        transformType(newMultiArrayExpr.getBaseType());
        super.visitNewMultiArrayExpr(sm, units, s, newMultiArrayExpr);
    }

    @Override
    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s,
            StaticFieldRef staticFieldRef, RefContext context)
    {
        SootClass fieldType = transform(staticFieldRef.getFieldRef().declaringClass());
        SootField newField = findField(fieldType, staticFieldRef.getFieldRef().name());
        staticFieldRef.setFieldRef(newField.makeRef());
        transformType(staticFieldRef.getType());
        super.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
    }

    @Override
    public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s,
            StaticInvokeExpr invokeExpr, InvokeContext context)
    {
        super.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);
        try
        {
            if (entryWrappers.containsKey(invokeExpr.getMethodRef().getSignature()))
            {
                invokeExpr.setMethodRef(entryWrappers.get(invokeExpr.getMethodRef().getSignature())
                        .makeRef());
                transformType(invokeExpr.getType());
                return;
            }
        }
        catch (Exception e)
        {
        }
        SootClass methType = transform(invokeExpr.getMethodRef().declaringClass());
        SootMethod newMethod = findMethod(methType, invokeExpr.getMethodRef().getSubSignature());
        invokeExpr.setMethodRef(newMethod.makeRef());
        transformType(invokeExpr.getType());
    }

    @Override
    public void visitType(SootMethod sm, Chain units, Stmt s, Type castType, TypeContext context)
    {
        transformType(castType);
        super.visitType(sm, units, s, castType, context);
    }

    @Override
    public void visitThisRef(SootMethod sm, Chain units, IdentityStmt s, ThisRef thisRef)
    {
        thisRefs.add(s.getLeftOp());
        if (unrootedClassMapping.containsKey(sm.getDeclaringClass().getName()))
        {
            Local l = (Local) s.getLeftOp();
            l.setType(sm.getDeclaringClass().getType());
            IdentityStmt replacement = Jimple.v().newIdentityStmt(s.getLeftOp(),
                    Jimple.v().newThisRef(RefType.v(sm.getDeclaringClass())));
            units.swapWith(s, replacement);
        }
        super.visitThisRef(sm, units, s, thisRef);
    }
}
