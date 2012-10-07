package javato.objectRaceDetection;

import javato.utils.ThreadBase;
import java.util.Set;
import java.util.HashSet;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
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
public class ObjectRaceDetectionEventHandler {
    private ThreadBase tb = new ThreadBase();
    private TupleBase tbase = new TupleBase();
    private Set<String> objTypes = new HashSet<String>();

    synchronized public void Start(int iid, int parent, int child, Thread childThread) {
        //System.out.println("parent "+parent+" is starting child "+child);
        tb.Start(parent, child);
    }

    synchronized public void Lock(int iid, int t, int l) {
        //System.out.println("iid: "+iid+" lock "+l+"obtained by thread "+t);
        tb.Lock(t, l);
    }

    synchronized public void Unlock(int iid, int t, int l) {
        //System.out.println("iid: "+iid+" lock "+l+"released by thread "+t);
        tb.Unlock(t, l);
    }

    synchronized public void Wait(int iid, int t, int l) {
        tb.Wait(t, l);
    }

    synchronized public void WaitTimed(int iid, int t, int l) {

    }

    synchronized public void Notify(int iid, int t, int l) {
        tb.Notify(t, l);
    }

    synchronized public void NotifyAll(int iid, int t, int l) {
        tb.Notify(t, l);
    }

    synchronized public void Join(int iid, int parent, int child) {
        tb.Join(parent, child);
    }

    synchronized public void JoinTimed(int iid, int parent, int child) {

    }

    synchronized public void Read(int iid, int t, long m) {

    }

    synchronized public void Write(int iid, int t, long m) {

    }

    public void ReadAfter(int iid, int t, long l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void WriteAfter(int iid, int t, long l) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    synchronized public void MethodInvocation(int iid, int t, int m, String objType) {
    	if(objTypes.contains(objType)){
    		return;
    	}
    	int racingiid;
    	//System.out.println("in MethodInvocation");
        if((racingiid = tbase.put(m,t,tb.getLockSet(t),tb.getVectorClock(t), iid)) != 0){
        	//System.err.println("type of object in race : "+objType+" in iid "+iid+" and races with event of iid "+racingiid);
        	//System.err.println(objType);
        	if(!objTypes.contains(objType)){
        		objTypes.add(objType);
        		System.err.println(objType);
        	}
        }
    }


    
}
