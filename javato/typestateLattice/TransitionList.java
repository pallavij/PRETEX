package javato.typestateLattice;

import java.util.*;
import java.io.*;
import javato.utils.Parameters;

/**
 * Copyright (c) 2007-2008,
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

/***
* each transition in the transitionlabels file is of the form :
* Type:MethodName:TransitionId
* state1:state2
* state1:state2
* ....
* the same method may possibly result in different transitions based on the state that the object is in
* hence we have multiple state1:state2 pairs for each transitionId
* -1 corresponds to the error state and 0 corresponds to the initial state
***/

public class TransitionList{
	private HashMap<Integer,Vector<Pair<Integer,Integer>>> transitionMap = new HashMap<Integer,Vector<Pair<Integer,Integer>>>();
	public static final int ERRSTATE = -1;
	public static final int STARTSTATE = 0;
	public static final int INVALIDTRANSITION = -1;
	
	public TransitionList(){
		String fname = Parameters.TRANSITION_LABELS_FILE;
		try{
			if(fname != null){
				BufferedReader in = new BufferedReader(new FileReader(fname));
				int lastTransitionId = -1;
				while(true){
					String line = in.readLine();
					if(line == null)
						break;
					String[] transitionElems = line.split(":");
					if(transitionElems.length == 3){
						Integer transitionId = new Integer(Integer.parseInt(transitionElems[2]));
						Vector<Pair<Integer,Integer>> statePairs = new Vector<Pair<Integer,Integer>>();
						transitionMap.put(transitionId, statePairs);
						lastTransitionId = Integer.parseInt(transitionElems[2]);
					}
					if(transitionElems.length == 2){
						Vector<Pair<Integer,Integer>> statePairs = transitionMap.get(new Integer(lastTransitionId));
						statePairs.add(new Pair<Integer,Integer>(Integer.parseInt(transitionElems[0]),Integer.parseInt(transitionElems[1])));
					}
					
				}
				in.close();
			}
		}
		catch(FileNotFoundException e){
			System.err.println("File " + fname + " not found");
			System.exit(1);
		}
		catch(IOException e){
			System.err.println("IO Exception while reading the file "+fname);
			System.exit(1);
		}
		
	}
	
	public Set<Integer> getStatesAfterTransition(Set<Integer> prevStates, int transitionId, int iid, List<Pair<Integer,Integer>> setOfEvents, int m){
		//System.out.println("In getStatesAfterTransition");
		
		Set<Integer> afterStates = new HashSet<Integer>();
		Iterator prevStatesItr = prevStates.iterator();
		Vector<Pair<Integer,Integer>> statePairs = transitionMap.get(new Integer(transitionId));
		while(prevStatesItr.hasNext()){
			Integer prevState = (Integer)(prevStatesItr.next());
			Iterator statePairsItr = statePairs.iterator();
			boolean isTransitionPossible = false;
			while(statePairsItr.hasNext()){
				Pair<Integer,Integer> statePair = (Pair<Integer,Integer>)(statePairsItr.next());
				if(statePair.fst.intValue() == prevState.intValue()){
					afterStates.add(statePair.snd);
					isTransitionPossible = true;
					//System.out.println("Transition observed : "+statePair.fst.intValue()+" to "+statePair.snd.intValue());
					break;
				}
			}
			if(isTransitionPossible == false){
				System.err.println("TYPESTATE ERROR : Transition "+ transitionId + " not possible for state "+prevState.intValue()+" iid : "+iid);	
				
				Iterator<Pair<Integer,Integer>> setOfEventsItr = setOfEvents.iterator();
				while(setOfEventsItr.hasNext()){
					Pair<Integer,Integer> nextEvent = setOfEventsItr.next();
					if(nextEvent.snd == m){
						System.err.print("("+nextEvent.fst+","+nextEvent.snd+")"+" ");
					}
				}
				System.err.println();
				
				
			}
		}
		return afterStates;
	}
	
	public boolean isErrorStatePresent(Set<Integer> states){
		if(states.contains(new Integer(ERRSTATE))){
			return true;
		}
		return false;
	}
}
