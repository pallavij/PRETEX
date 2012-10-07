package javato.instrumentor.baseclassinstrumentor;

import javato.instrumentor.Visitor;
import javato.instrumentor.contexts.InvokeContext;
import javato.instrumentor.contexts.RefContext;

import java.util.*;
import soot.*;
import soot.jimple.CastExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;

public class DependencyFinder extends Visitor
{
    private HashSet<SootClass> packProtectedCalls;
    private HashSet<SootClass> castedClasses;

    public HashSet<SootClass> getPackageProtectedDependencies()
    {
        return packProtectedCalls;
    }
    public HashSet<SootClass> getCastedClasses()
    {
        return castedClasses;
    }

    public DependencyFinder(Visitor nextVisitor)
    {
        super(nextVisitor);
        packProtectedCalls = new HashSet<SootClass>();
        castedClasses = new HashSet<SootClass>();
    }

    private boolean isPackageProtected(int modifiers)
    {
        return !Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers)
                && !Modifier.isProtected(modifiers);
    }

    @Override
    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s,
            InstanceFieldRef instanceFieldRef, RefContext context)
    {
        if (isPackageProtected(instanceFieldRef.getField().getModifiers()))
            packProtectedCalls.add(instanceFieldRef.getField().getDeclaringClass());
        super.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
    }

    @Override
    public void visitInvokeExpr(SootMethod sm, Chain units, Stmt s, InvokeExpr invokeExpr,
            InvokeContext context)
    {
        if (isPackageProtected(invokeExpr.getMethod().getModifiers()))
            packProtectedCalls.add(invokeExpr.getMethod().getDeclaringClass());
        super.visitInvokeExpr(sm, units, s, invokeExpr, context);
    }

    private RefType getBaseRefType(Type t)
    {
        if (t instanceof RefType)
            return (RefType) t;
        if (t instanceof ArrayType)
        {
            ArrayType at = (ArrayType) t;
            if (at.getArrayElementType() instanceof RefType)
                return (RefType) at.getArrayElementType();
        }
        return null;
    }

    @Override
    public void visitMethodBegin(SootMethod sm, Chain units)
    {
        if (!sm.hasActiveBody())
            return;
        for (Object obj : sm.getActiveBody().getLocals())
        {
            Local l = (Local) obj;
            RefType t = getBaseRefType(l.getType());
            if (t == null)
                continue;
            if (isPackageProtected((t.getSootClass().getModifiers())))
                packProtectedCalls.add(t.getSootClass());
        }
        super.visitMethodBegin(sm, units);
    }

    @Override
    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s,
            StaticFieldRef staticFieldRef, RefContext context)
    {
        if (isPackageProtected(staticFieldRef.getField().getModifiers()))
            packProtectedCalls.add(staticFieldRef.getField().getDeclaringClass());
        super.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
    }

    @Override
    public void visitCastExpr(SootMethod sm, Chain units, Stmt s, CastExpr castExpr)
    {
        RefType t = getBaseRefType(castExpr.getCastType());
        if (t == null)
            return;
        if (isPackageProtected((t.getSootClass().getModifiers())))
            packProtectedCalls.add(t.getSootClass());
        castedClasses.add(t.getSootClass());
        super.visitCastExpr(sm, units, s, castExpr);
    }

    @Override
    public void visitNewArrayExpr(SootMethod sm, Chain units, Stmt s, NewArrayExpr newArrayExpr)
    {
        RefType t = getBaseRefType(newArrayExpr.getBaseType());
        if (t == null)
            return;
        if (isPackageProtected((t.getSootClass().getModifiers())))
            packProtectedCalls.add(t.getSootClass());
        super.visitNewArrayExpr(sm, units, s, newArrayExpr);
    }

    @Override
    public void visitNewExpr(SootMethod sm, Chain units, Stmt s, NewExpr newExpr)
    {
        RefType t = getBaseRefType(newExpr.getBaseType());
        if (t == null)
            return;
        if (isPackageProtected((t.getSootClass().getModifiers())))
            packProtectedCalls.add(t.getSootClass());
        super.visitNewExpr(sm, units, s, newExpr);
    }

}
