package javato.objectRaceDetection;

import javato.utils.VectorClock;

import java.util.*;

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
public class TupleBase {
    private TreeMap<Pair<Integer, Integer>, Vector<Tuple<Set<Integer>,VectorClock,Integer>>> database;
    private Set<Integer> threadIds;
    
    public TupleBase() {
        IntegerIntegerPairComparator comp = new IntegerIntegerPairComparator();
        database = new TreeMap<Pair<Integer, Integer>, Vector<Tuple<Set<Integer>,VectorClock,Integer>>>(comp);
        threadIds = new TreeSet<Integer>();
    }


    public int put(int obj, int t, Set<Integer> lockSet, VectorClock vectorClock, int iid) {
        Tuple<Set<Integer>, VectorClock, Integer> databaseTuple = null;
        Pair<Integer,Integer> databaseKey = null;
        Set<Integer> databaseLockSet = new HashSet<Integer>(lockSet);
        VectorClock databaseVectorClock = new VectorClock(vectorClock);
        
        Iterator<Integer> threadIdsItr = threadIds.iterator();
        while(threadIdsItr.hasNext()){
        	Integer tid = threadIdsItr.next();
        	if(tid != t){
        	   Pair<Integer,Integer> dbKey = new Pair<Integer,Integer>(tid,obj);
        	   Vector<Tuple<Set<Integer>,VectorClock,Integer>> dbTupleList = database.get(dbKey);
        	   if(dbTupleList != null){
        		   Iterator<Tuple<Set<Integer>,VectorClock,Integer>> dbTupleListItr = dbTupleList.iterator();
        		   while(dbTupleListItr.hasNext()){
        			   Tuple<Set<Integer>,VectorClock,Integer> dbTuple = dbTupleListItr.next();
        			   if(areEventsConcurrent(t,vectorClock,lockSet,tid,dbTuple.snd,dbTuple.fst)){
        				   return dbTuple.thrd;
        			   }
        		   }
        	   }
        	}
        	
        }
        
        
        databaseTuple = new Tuple<Set<Integer>, VectorClock, Integer>(databaseLockSet, databaseVectorClock, iid);
        databaseKey = new Pair<Integer,Integer>(new Integer(t), new Integer(obj));
        Vector<Tuple<Set<Integer>,VectorClock,Integer>> dbTupleListWithDbKey;

        dbTupleListWithDbKey = database.get(databaseKey);

        if (dbTupleListWithDbKey != null) {
            Iterator tplListItr = dbTupleListWithDbKey.iterator();
            boolean putIntoDatabase = true;
            //optimization : check if the entry already exists in the database
            while (tplListItr.hasNext()) {
            	Tuple<Set<Integer>,VectorClock,Integer> tpl = (Tuple<Set<Integer>,VectorClock,Integer>) tplListItr.next();
            	if (areTuplesEqual(databaseTuple,tpl,t)) {
            		putIntoDatabase = false;
            		break;
            	}
            }
            if (putIntoDatabase) {
            	dbTupleListWithDbKey.add(0, databaseTuple);
            	database.put(databaseKey, dbTupleListWithDbKey);
            	if(!threadIds.contains(t)){
            		threadIds.add(t);
            	}
            }
            
        } else {
        	
        	dbTupleListWithDbKey = new Vector<Tuple<Set<Integer>,VectorClock,Integer>>();
        	dbTupleListWithDbKey.add(databaseTuple);
        	database.put(databaseKey, dbTupleListWithDbKey);
        	if(!threadIds.contains(t)){
        		threadIds.add(t);
        	}
        	
        }
        
        
        
        return 0;
    }

    //PALLAVI : not very sure about this checking
    //the event corresponding to the first vector clock has happened before the event corresponding 
    //to the second vector clock
    public boolean areVectorClocksConcurrent(int t1, VectorClock vc1, int t2, VectorClock vc2) {

        if (!(vc1.getValue(t1) <= vc2.getValue(t1))) {
            return true;
        }

        return false;
    }

    public boolean areEventsConcurrent(int t1, VectorClock vc1, Set<Integer> lockSet1, int t2, VectorClock vc2, Set<Integer> lockSet2){
   
        Set<Integer> intersectionOfLocksets = new HashSet<Integer>(lockSet1);
        intersectionOfLocksets.retainAll(lockSet2);

        if (areVectorClocksConcurrent(t1, vc1, t2, vc2)) {
            if (intersectionOfLocksets.isEmpty()) {
                return true;
            } 
        }
        return false;
        
    }

    
    public boolean areTuplesEqual(Tuple<Set<Integer>,VectorClock,Integer> tupl1, Tuple<Set<Integer>,VectorClock,Integer> tupl2, int t) {
        Set<Integer> t1Elem1 = tupl1.fst;
        VectorClock t1Elem2 = tupl1.snd;
        Integer t1Elem3 = tupl1.thrd;

        Set<Integer> t2Elem1 = tupl2.fst;
        VectorClock t2Elem2 = tupl2.snd;
        Integer t2Elem3 = tupl2.thrd;

        boolean areElems1Equal;
        boolean areElems2Equal;
        boolean areElems3Equal;

        if ((t1Elem1 == null) && (t2Elem1 == null)) {
            areElems1Equal = true;
        } else {
            if ((t1Elem1 != null) && (t2Elem1 != null) && (t1Elem1.equals(t2Elem1))) {
                areElems1Equal = true;
            } else {
                areElems1Equal = false;
            }
        }

        if ((t1Elem2 == null) && (t2Elem2 == null)) {
            areElems2Equal = true;
        } else {
            if ((t1Elem2 != null) && (t2Elem2 != null) && (t1Elem2.getValue(t) == t2Elem2.getValue(t))) {
                areElems2Equal = true;
            } else {
                areElems2Equal = false;
            }
        }
        
        if(t1Elem3 == t2Elem3){
        	areElems3Equal = true;
        }
        else{
        	areElems3Equal = false;
        }

        return (areElems1Equal && areElems2Equal && areElems3Equal);
    }

    public int get_db_size() {
        return database.size();
    }
}

