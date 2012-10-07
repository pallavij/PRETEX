package javato.objectRaceDetection;
import java.util.Comparator;

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
public class Pair<A, B> {
    public final A fst;
    public final B snd;

    public Pair(A a, B b) {
        fst = a;
        snd = b;
    }
    public boolean equals(Object other){
    	if(this == other){
    		return true;
    	}
    	if(!(other instanceof Pair)){
    		return false;
    	}
    	Pair otherPair = (Pair)other;
    	return (((fst == null)?(otherPair.fst == null):(fst.equals(otherPair.fst))) && 
    	((snd == null)?(otherPair.snd == null):(snd.equals(otherPair.snd))));
    }
    
    public int hashCode(){
    	int hash = 1;
    	hash = hash*31 + (fst == null ? 0 : fst.hashCode());
    	hash = hash*31 + (snd == null ? 0 : snd.hashCode());
    	return hash;
    }
    
}

class IntegerLongPairComparator implements Comparator{
	public int compare(Object obj1, Object obj2){
		Pair pair1 = (Pair)obj1;
		Pair pair2 = (Pair)obj2;
		if((pair1 == null) && (pair2 == null)){
			return 0;
		}
		if((pair1 == null) && (pair2 != null)){
			return -1;
		}
		if((pair1 != null) && (pair2 == null)){
			return 1;
		}
		Integer i1 = (Integer)(pair1.fst);
		Integer i2 = (Integer)(pair2.fst);
		Long l1 = (Long)(pair1.snd);
		Long l2 = (Long)(pair2.snd);
		
		if((i1 != null) && i1.compareTo(i2) != 0){
			return i1.compareTo(i2);
		}
		else{
			if((i1 == null) && (i2 != null)){
				return -1;
			}
			if((l1 == null) && (l2 == null)){
				return 0;
			}
			if((l1 == null) && (l2 != null)){
				return -1;
			}
			if((l1 != null) && (l2 == null)){
				return 1;
			}
			return l1.compareTo(l2);
		}
	}
}

class IntegerIntegerPairComparator implements Comparator{
	public int compare(Object obj1, Object obj2){
		Pair pair1 = (Pair)obj1;
		Pair pair2 = (Pair)obj2;
		if((pair1 == null) && (pair2 == null)){
			return 0;
		}
		if((pair1 == null) && (pair2 != null)){
			return -1;
		}
		if((pair1 != null) && (pair2 == null)){
			return 1;
		}
		Integer f1 = (Integer)(pair1.fst);
		Integer f2 = (Integer)(pair2.fst);
		Integer s1 = (Integer)(pair1.snd);
		Integer s2 = (Integer)(pair2.snd);
		
		if((f1 != null) && f1.compareTo(f2) != 0){
			return f1.compareTo(f2);
		}
		else{
			if((f1 == null) && (f2 != null)){
				return -1;
			}
			if((s1 == null) && (s2 == null)){
				return 0;
			}
			if((s1 == null) && (s2 != null)){
				return -1;
			}
			if((s1 != null) && (s2 == null)){
				return 1;
			}
			return s1.compareTo(s2);
		}
	}
}

