package javato.instrumentor.baseclassinstrumentor;

import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.util.Chain;
import javato.instrumentor.Visitor;
import soot.jimple.*;
import soot.Scene;

public class TestVisitor extends Visitor
{
    private SootClass throwable;

    public TestVisitor(Visitor nextVisitor)
    {
        super(nextVisitor);
        throwable = Scene.v().loadClassAndSupport("java.lang.Throwable");
    }

    @Override
    public void visitMethodBegin(SootMethod sm, Chain units)
    {
        this.thisStmt = (Stmt) units.getFirst();
        this.addCallWithObject(units, (Stmt) units.getFirst(), "enter", StringConstant.v(sm
                .toString()), true);
        super.visitMethodBegin(sm, units);
    }

    @Override
    public void visitMethodEnd(SootMethod sm, Chain units)
    {
        Stmt last = (Stmt) units.getLast();
        this.addCallWithObject(units, (Stmt) units.getLast(), "exit", StringConstant.v(sm
                .toString()), false);
        Chain traps = sm.getActiveBody().getTraps();
        traps.add(Jimple.v().newTrap(throwable, (Unit) units.getFirst(), (Unit) last,
                (Unit) units.getLast()));
        Local l;
        sm.getActiveBody().getLocals().add(
                l = Jimple.v().newLocal(
                        "javato_instrumentor_baseclassinstrumentor_TestVisitor_exc",
                        RefType.v(throwable)));
        units.addLast(Jimple.v().newIdentityStmt(l, Jimple.v().newCaughtExceptionRef()));
        units.addLast(Jimple.v().newThrowStmt(l));
        super.visitMethodEnd(sm, units);
    }

    @Override
    public void visitStmtReturn(SootMethod sm, Chain units, ReturnStmt returnStmt)
    {
        this.addCallWithObject(units, returnStmt, "exit", StringConstant.v(sm.toString()), true);
        super.visitStmtReturn(sm, units, returnStmt);
    }

    @Override
    public void visitStmtReturnVoid(SootMethod sm, Chain units, ReturnVoidStmt returnVoidStmt)
    {
        this
                .addCallWithObject(units, returnVoidStmt, "exit", StringConstant.v(sm.toString()),
                        true);
        super.visitStmtReturnVoid(sm, units, returnVoidStmt);
    }
}
