package javato.objectRaceDetection;

import javato.observer.Observer;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
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
public class ObserverForObjectRaceDetection extends Observer {

    protected static ObjectRaceDetectionEventHandler eh = new ObjectRaceDetectionEventHandler();

    public static void myStart(int iid, Object t) {
        //logme("start before", true);
        eh.Start(iid, uniqueId(Thread.currentThread()), uniqueId(t), (Thread) t);
        //logme("start before", false);
    }

    public static void myLock(int iid, int oid) {
        //logme("lock", true);
        eh.Lock(iid, uniqueId(Thread.currentThread()), oid);
        //logme("lock", false);
    }

    public static void myUnlock(int iid, int oid) {
        //logme("unlock", true);
        eh.Unlock(iid, uniqueId(Thread.currentThread()), oid);
        //logme("unlock", false);
    }

    public static void myLock(int iid, Object l) {
        //logme("lock", true);
        eh.Lock(iid, uniqueId(Thread.currentThread()), uniqueId(l));
        //logme("lock", false);
    }

    public static void myUnlock(int iid, Object l) {
        //logme("unlock", true);
        eh.Unlock(iid, uniqueId(Thread.currentThread()), uniqueId(l));
        //logme("unlock", false);
    }

    public static void myWait(int iid, Object l) {
        //logme("wait", true);
        eh.Wait(iid, uniqueId(Thread.currentThread()), uniqueId(l));
        //logme("wait", false);
    }

    public static void myWaitTimed(int iid, Object l) {
        //logme("wait timed", true);
        eh.WaitTimed(iid, uniqueId(Thread.currentThread()), uniqueId(l));
        //logme("wait timed", false);
    }

    public static void myNotify(int iid, Object l) {
        //logme("notify", true);
        eh.Notify(iid, uniqueId(Thread.currentThread()), uniqueId(l));
        //logme("notify", false);
    }

    public static void myNotifyAll(int iid, Object l) {
        //logme("notifyAll", true);
        eh.NotifyAll(iid, uniqueId(Thread.currentThread()), uniqueId(l));
        //logme("notifyAll", false);
    }

    public static void myJoin(int iid, Object t) {
        //logme("join", true);
        eh.Join(iid, uniqueId(Thread.currentThread()), uniqueId(t));
        //logme("join", false);
    }

    public static void myJoinTimed(int iid, Object t) {
        //logme("join timed", true);
        eh.JoinTimed(iid, uniqueId(Thread.currentThread()), uniqueId(t));
        //logme("join timed", false);
    }

    public static void myRead(int iid, Object o, int field) {
        //logme("read", true);
        eh.Read(iid, uniqueId(Thread.currentThread()), id(o, field));
        //logme("read", false);
    }

    public static void myRead(int iid, int clss, int field) {
        //logme("read", true);
        eh.Read(iid, uniqueId(Thread.currentThread()), id(clss, field));
        //logme("read", false);
    }

    public static void myWrite(int iid, Object o, int field) {
        //logme("write", true);
        eh.Write(iid, uniqueId(Thread.currentThread()), id(o, field));
        //logme("write", false);
    }

    public static void myWrite(int iid, int clss, int field) {
        //logme("write", true);
        eh.Write(iid, uniqueId(Thread.currentThread()), id(clss, field));
        //logme("write", false);
    }

    public static void myReadAfter(int iid, Object o, int field) {
        //logme("read", true);
        eh.ReadAfter(iid, uniqueId(Thread.currentThread()), id(o, field));
        //logme("read", false);
    }

    public static void myReadAfter(int iid, int clss, int field) {
        //logme("read", true);
        eh.ReadAfter(iid, uniqueId(Thread.currentThread()), id(clss, field));
        //logme("read", false);
    }

    public static void myWriteAfter(int iid, Object o, int field) {
        //logme("write", true);
        eh.WriteAfter(iid, uniqueId(Thread.currentThread()), id(o, field));
        //logme("write", false);
    }

    public static void myWriteAfter(int iid, int clss, int field) {
        //logme("write", true);
        eh.WriteAfter(iid, uniqueId(Thread.currentThread()), id(clss, field));
        //logme("write", false);
    }

    public static void myMethodInvocation(int iid, Object val) {
        //logme("transition", true);
        eh.MethodInvocation(iid, uniqueId(Thread.currentThread()),uniqueId(val),val.getClass().getName());
        //logme("transition", false);
    }

}
