package javato.typestateLattice;

import javato.instrumentor.Visitor;
import javato.instrumentor.contexts.InvokeContext;
import soot.RefType;
import soot.SootMethod;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.util.Chain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Copyright (c) 2007-2008
 * Pallavi Joshi	<pallavi@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class VisitorForTypeStateChecking extends Visitor {//extends VisitorForHybridRaceChecking {
    private HashMap<String, HashMap<String, Integer>> transitionMap;


    public VisitorForTypeStateChecking(Visitor visitor) {
        super(visitor);
        String fname = System.getProperty("pretex.transitionlabels", null);
        String line;
        if (fname != null) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(fname));
                transitionMap = new HashMap<String, HashMap<String, Integer>>();
                while ((line = in.readLine()) != null) {
                    String[] tokens = line.split(":");
                    if (tokens.length < 3) {
                        continue;
                    }
                    HashMap<String, Integer> tmp = transitionMap.get(tokens[1]);
                    if (tmp == null) {
                        tmp = new HashMap<String, Integer>();
                        tmp.put(tokens[0], new Integer(Integer.parseInt(tokens[2])));
                        transitionMap.put(tokens[1], tmp);
                    } else {
                        tmp.put(tokens[0], new Integer(Integer.parseInt(tokens[2])));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s, InstanceInvokeExpr instanceInvokeExpr, InvokeContext context) {
        // now add calls to track typestate
        if (transitionMap != null) {
            HashMap<String, Integer> tmp = transitionMap.get(instanceInvokeExpr.getMethod().getName());
            if (tmp != null) {
                for (String typeName : tmp.keySet()) {
                	if(instanceInvokeExpr.getBase().getType() instanceof RefType)
                		if (isSubClass(((RefType) instanceInvokeExpr.getBase().getType()).getSootClass(), typeName)) {
                			addCallWithObjectInt(units, s, "myTransition",
                					(instanceInvokeExpr.getBase()),
                					IntConstant.v(tmp.get(typeName)), true);
                			//System.out.println("transitionId is "+tmp.get(typeName));
                		}
                }
            }

        }

        nextVisitor.visitInstanceInvokeExpr(sm, units, s, instanceInvokeExpr, context);
    }

}