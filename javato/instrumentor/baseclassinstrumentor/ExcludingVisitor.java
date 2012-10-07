package javato.instrumentor.baseclassinstrumentor;

import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ConcreteRef;
import soot.jimple.Constant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Expr;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LengthExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.util.Chain;
import javato.instrumentor.RecursiveVisitor;
import javato.instrumentor.Visitor;
import javato.instrumentor.contexts.BinopExprContext;
import javato.instrumentor.contexts.InvokeContext;
import javato.instrumentor.contexts.LabelContext;
import javato.instrumentor.contexts.LocalContext;
import javato.instrumentor.contexts.LocalOrConstantContext;
import javato.instrumentor.contexts.RefContext;
import javato.instrumentor.contexts.TypeContext;

public class ExcludingVisitor extends Visitor
{

    public ExcludingVisitor(Visitor nextVisitor)
    {
        super(nextVisitor);
    }

    @Override
    public void visitArrayRef(SootMethod sm, Chain units, Stmt s, ArrayRef arrayRef,
            RefContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
    }

    @Override
    public void visitBinop(SootMethod sm, Chain units, Stmt s, String op, BinopExprContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitBinop(sm, units, s, op, context);
    }

    @Override
    public void visitBinopExpr(SootMethod sm, Chain units, Stmt s, BinopExpr expr,
            BinopExprContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitBinopExpr(sm, units, s, expr, context);
    }

    @Override
    public void visitCastExpr(SootMethod sm, Chain units, Stmt s, CastExpr castExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitCastExpr(sm, units, s, castExpr);
    }

    @Override
    public void visitCaughtExceptionRef(SootMethod sm, Chain units, IdentityStmt s,
            CaughtExceptionRef caughtExceptionRef)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitCaughtExceptionRef(sm, units, s, caughtExceptionRef);
    }

    @Override
    public void visitConcreteRef(SootMethod sm, Chain units, Stmt s, ConcreteRef concreteRef,
            RefContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitConcreteRef(sm, units, s, concreteRef, context);
    }

    @Override
    public void visitConstant(SootMethod sm, Chain units, Stmt s, Constant constant,
            LocalOrConstantContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitConstant(sm, units, s, constant, context);
    }

    @Override
    public void visitExpr(SootMethod sm, Chain units, Stmt s, Expr expr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitExpr(sm, units, s, expr);
    }

    @Override
    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s,
            InstanceFieldRef instanceFieldRef, RefContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
    }

    @Override
    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s,
            InstanceInvokeExpr invokeExpr, InvokeContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);
    }

    @Override
    public void visitInstanceOfExpr(SootMethod sm, Chain units, Stmt s,
            InstanceOfExpr instanceOfExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitInstanceOfExpr(sm, units, s, instanceOfExpr);
    }

    @Override
    public void visitInvokeExpr(SootMethod sm, Chain units, Stmt s, InvokeExpr invokeExpr,
            InvokeContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitInvokeExpr(sm, units, s, invokeExpr, context);
    }

    @Override
    public void visitLabel(SootMethod sm, Chain units, Stmt gotoStmt, Unit target,
            LabelContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitLabel(sm, units, gotoStmt, target, context);
    }

    @Override
    public void visitLengthExpr(SootMethod sm, Chain units, Stmt s, LengthExpr lengthExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitLengthExpr(sm, units, s, lengthExpr);
    }

    @Override
    public void visitLocal(SootMethod sm, Chain units, Stmt s, Local local, LocalContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitLocal(sm, units, s, local, context);
    }

    @Override
    public void visitLocalOrConstant(SootMethod sm, Chain units, Stmt s, Value right,
            LocalOrConstantContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitLocalOrConstant(sm, units, s, right, context);
    }

    @Override
    public void visitLookupValue(SootMethod sm, Chain units, Stmt stmt, int lookupValue)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitLookupValue(sm, units, stmt, lookupValue);
    }

    @Override
    public void visitMethodBegin(SootMethod sm, Chain units)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitMethodBegin(sm, units);
    }

    @Override
    public void visitMethodEnd(SootMethod sm, Chain units)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitMethodEnd(sm, units);
    }

    @Override
    public void visitNegExpr(SootMethod sm, Chain units, Stmt s, NegExpr negExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitNegExpr(sm, units, s, negExpr);
    }

    @Override
    public void visitNewArrayExpr(SootMethod sm, Chain units, Stmt s, NewArrayExpr newArrayExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitNewArrayExpr(sm, units, s, newArrayExpr);
    }

    @Override
    public void visitNewExpr(SootMethod sm, Chain units, Stmt s, NewExpr newExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitNewExpr(sm, units, s, newExpr);
    }

    @Override
    public void visitNewMultiArrayExpr(SootMethod sm, Chain units, Stmt s,
            NewMultiArrayExpr newMultiArrayExpr)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitNewMultiArrayExpr(sm, units, s, newMultiArrayExpr);
    }

    @Override
    public void visitParameterRef(SootMethod sm, Chain units, IdentityStmt s,
            ParameterRef parameterRef)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitParameterRef(sm, units, s, parameterRef);
    }

    @Override
    public void visitRHS(SootMethod sm, Chain units, Stmt s, Value right)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitRHS(sm, units, s, right);
    }

    @Override
    public void visitSignature(SootMethod sm, Chain units, Stmt s, String signature,
            InvokeContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitSignature(sm, units, s, signature, context);
    }

    @Override
    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s,
            StaticFieldRef staticFieldRef, RefContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
    }

    @Override
    public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s,
            StaticInvokeExpr invokeExpr, InvokeContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);
    }

    @Override
    public void visitStmt(SootMethod sm, Chain units, Stmt s)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmt(sm, units, s);
    }

    @Override
    public void visitStmtAssign(SootMethod sm, Chain units, AssignStmt assignStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtAssign(sm, units, assignStmt);
    }

    @Override
    public void visitStmtBreakpoint(SootMethod sm, Chain units, BreakpointStmt breakpointStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtBreakpoint(sm, units, breakpointStmt);
    }

    @Override
    public void visitStmtEnterMonitor(SootMethod sm, Chain units, EnterMonitorStmt enterMonitorStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
    }

    @Override
    public void visitStmtExitMonitor(SootMethod sm, Chain units, ExitMonitorStmt exitMonitorStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
    }

    @Override
    public void visitStmtGoto(SootMethod sm, Chain units, GotoStmt gotoStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtGoto(sm, units, gotoStmt);
    }

    @Override
    public void visitStmtIdentity(SootMethod sm, Chain units, IdentityStmt identityStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtIdentity(sm, units, identityStmt);
    }

    @Override
    public void visitStmtIf(SootMethod sm, Chain units, IfStmt ifStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtIf(sm, units, ifStmt);
    }

    @Override
    public void visitStmtInvoke(SootMethod sm, Chain units, InvokeStmt invokeStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtInvoke(sm, units, invokeStmt);
    }

    @Override
    public void visitStmtLookupSwitch(SootMethod sm, Chain units, LookupSwitchStmt lookupSwitchStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtLookupSwitch(sm, units, lookupSwitchStmt);
    }

    @Override
    public void visitStmtMonitor(SootMethod sm, Chain units, MonitorStmt monitorStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtMonitor(sm, units, monitorStmt);
    }

    @Override
    public void visitStmtNop(SootMethod sm, Chain units, NopStmt nopStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtNop(sm, units, nopStmt);
    }

    @Override
    public void visitStmtReturn(SootMethod sm, Chain units, ReturnStmt returnStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtReturn(sm, units, returnStmt);
    }

    @Override
    public void visitStmtReturnVoid(SootMethod sm, Chain units, ReturnVoidStmt returnVoidStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtReturnVoid(sm, units, returnVoidStmt);
    }

    @Override
    public void visitStmtTableSwitch(SootMethod sm, Chain units, TableSwitchStmt tableSwitchStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtTableSwitch(sm, units, tableSwitchStmt);
    }

    @Override
    public void visitStmtThrow(SootMethod sm, Chain units, ThrowStmt throwStmt)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitStmtThrow(sm, units, throwStmt);
    }

    @Override
    public void visitThisRef(SootMethod sm, Chain units, IdentityStmt s, ThisRef thisRef)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitThisRef(sm, units, s, thisRef);
    }

    @Override
    public void visitType(SootMethod sm, Chain units, Stmt s, Type castType, TypeContext context)
    {
        if (Exclusions.isExcludedVisitorMethod(sm.toString()))
            return;
        if (Exclusions.isExcludedClass(sm.getDeclaringClass().toString()))
            return;
        nextVisitor.visitType(sm, units, s, castType, context);
    }

}
