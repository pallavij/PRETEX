package javato.typestateLattice;

import javato.utils.VectorClock;
import javato.utils.LongCounter;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Copyright (c) 2007-2008,
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
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

public class Lattice{
	private List<Tuple<Integer,VectorClock,Integer,Integer,Integer>> eventQueue;
	private List<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>> curLevel;
	private List<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>> nextLevel;
	private static int width = 10;
	private static int length = 20;
	private static int level = 0;
	private static int numEvents = 0;
	
	public Lattice(){
		eventQueue = new LinkedList<Tuple<Integer,VectorClock,Integer,Integer,Integer>>();
		curLevel = new LinkedList<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>>();
		nextLevel = new LinkedList<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>>();
		VectorClock initVC = new VectorClock();
		Memory initMem = new Memory();
		Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>> initState = new Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>(initVC,initMem,new LinkedList<Pair<Integer,Integer>>());
		curLevel.add(initState);
	}
	
	public void enqueue(Integer tid, VectorClock vc, Integer transitionId, Integer m,int iid){
		eventQueue.add(new Tuple<Integer,VectorClock,Integer,Integer,Integer>(tid,vc,transitionId,m,iid));
		numEvents++;
		//System.out.println("event queued");
		//vc.print();
		//System.out.println("No of events = "+numEvents);
	}
	
	public boolean constructLevel(TransitionList tlist){
	  try{
		Iterator<Tuple<Integer,VectorClock,Integer,Integer,Integer>> eventQueueItr = eventQueue.iterator();
		
		while(eventQueueItr.hasNext()){
			Tuple<Integer,VectorClock,Integer,Integer,Integer> event = eventQueueItr.next();
			VectorClock eventVC = event.snd;
			int eventThreadId = event.fst;
			int eventTransitionId = event.thrd;
			int m = event.frth;
			int iid = event.fifth;
			
			Iterator<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>> curLevelItr = curLevel.iterator();
			
			while(curLevelItr.hasNext()){
				
				Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>> curLevelState = curLevelItr.next();
				VectorClock curLevelStateVC = curLevelState.fst;
				Set<Integer> curLevelStateAutomatonStates = null;
				if(eventTransitionId != tlist.INVALIDTRANSITION){
					curLevelStateAutomatonStates = curLevelState.snd.getStates(m);
				}
				
				if(isNextState(curLevelStateVC,eventVC,eventThreadId,eventTransitionId)){
					
					//if(eventTransitionId == 3){
						//System.out.println("getOutputStream() event on "+m+" forming a new state");
					//}
					
					VectorClock newStateVC = createState(curLevelStateVC,eventVC,eventThreadId);
					Set<Integer> newStateAutomatonStates = null;
					
					List<Pair<Integer,Integer>> setOfIids = new LinkedList<Pair<Integer,Integer>>(curLevelState.thrd);
					setOfIids.add(new Pair<Integer,Integer>(iid,m));
					
					if(eventTransitionId != tlist.INVALIDTRANSITION){
						newStateAutomatonStates = getAutomatonStates(curLevelStateAutomatonStates,eventTransitionId,iid,tlist,setOfIids,m);
					}
					
					boolean addNewState = true;
					
					Iterator<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>> nextLevelItr = nextLevel.iterator();
					//could be optimized
					while(nextLevelItr.hasNext()){
						Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>> nextLevelState = nextLevelItr.next();
						if(VectorClock.areVecClocksEqual(nextLevelState.fst,newStateVC)){
							if(eventTransitionId != tlist.INVALIDTRANSITION){
								nextLevelState.snd.updateStates(m,newStateAutomatonStates);
							}
							addNewState = false;
							break;
						}
					}
					
					if(addNewState){
						Memory newStateMem = new Memory(curLevelState.snd);
						if(eventTransitionId != tlist.INVALIDTRANSITION){
							newStateMem.updateStatesDestructively(m, newStateAutomatonStates);
						}
						
						nextLevel.add(new Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>(newStateVC,newStateMem,setOfIids));
						
					}
				  }
				
				  boolean lastEvent = false;
				  if(!(eventQueueItr.hasNext())){
						lastEvent = true;
				  }
				  
				  boolean lastState = false;
				  if(!(curLevelItr.hasNext())){
						lastState = true;
				  }

				  
				  //deviating from the algorithm in the paper	
				  if(isLevelComplete(eventVC,lastEvent && lastState)){
						removeUselessEvents();
						curLevel = nextLevel;
						nextLevel = new LinkedList<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>>();
						return true;
				  }
				  else{
					  //System.out.println("No...level not yet complete");
				  }
				
			}
		}
		return false;
	  }
	  catch(Exception e){
			System.out.println("Exception in constructLevel "+e.toString());
			e.printStackTrace();
			return false;
	  }
	}
	
	//how to know when a level is complete in the exhaustive case?
	public boolean isLevelComplete(VectorClock eventVC, boolean isLastEvent){
		//System.out.println("size of nextLevel is "+nextLevel.size());
		//System.out.println("size of eventQueue is "+eventQueue.size());
		if(nextLevel.size() >= width){
			//System.out.println("yes....at most width states have been formed");
			//System.out.println("yes....level "+ (++level) +"finished");
			//printNextLevelStates(level);
			return true;
		}
		//deviating from the algorithm in the paper
		//length = getLength();
		if(isLastEvent && (eventQueue.size() >= length)){
			length += 20;
			//System.out.println("yes....level "+ (++level) +"finished");
			//printNextLevelStates(level);
			return true;
		}
		
		return false;
	}
	
	public void printNextLevelStates(int level){
		System.out.println("printing level "+level);
		Iterator<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>> nextLevelStatesItr = nextLevel.iterator();
		while(nextLevelStatesItr.hasNext()){
			Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>> state = nextLevelStatesItr.next();
			state.fst.print();
			System.out.println("---");
		}
		System.out.println("done");
	}
	
	public int getLength(){
		
		VectorClock maxVC = new VectorClock();
		
		Iterator<Tuple<Integer,VectorClock,Integer,Integer,Integer>> eventQueueItr = eventQueue.iterator();
		while(eventQueueItr.hasNext()){
			VectorClock eventQueueVC = eventQueueItr.next().snd;
			Iterator<Integer> eventQueueVCItr = eventQueueVC.vc.keySet().iterator();
			while(eventQueueVCItr.hasNext()){
				Integer eventQueueVCTId = eventQueueVCItr.next();
				long eventQueueVCTIdCounter = eventQueueVC.vc.get(eventQueueVCTId).val;
				long maxVCTIdCounter = maxVC.getValue(eventQueueVCTId);
				if(eventQueueVCTIdCounter > maxVCTIdCounter){
					maxVC.vc.put(eventQueueVCTId,new LongCounter(eventQueueVCTIdCounter));
				}
			}
		}
		int maxLevel = 0;
		Iterator<Integer> maxVCItr = maxVC.vc.keySet().iterator();
		while(maxVCItr.hasNext()){
			maxLevel += maxVC.getValue(maxVCItr.next());
		}
		//System.out.println("maxLevel = "+maxLevel);
		return maxLevel;	
	}
	
	public void removeUselessEvents(){
		int eventsRemoved = 0;
		VectorClock minVC = new VectorClock();
		
		Iterator<Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>>> curLevelItr = curLevel.iterator();
		if(curLevelItr.hasNext()){
			Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>> curLevelState = curLevelItr.next();
			VectorClock curLevelStateVC = curLevelState.fst;
			minVC = new VectorClock(curLevelStateVC);
		}
		while(curLevelItr.hasNext()){ 
			Tuple3<VectorClock,Memory,List<Pair<Integer,Integer>>> curLevelState = curLevelItr.next();
			VectorClock curLevelStateVC = curLevelState.fst;
			Iterator<Integer> minVCItr = minVC.vc.keySet().iterator();
			while(minVCItr.hasNext()){
				Integer tid = minVCItr.next();
				LongCounter curLevelStateVCTidCounter = curLevelStateVC.vc.get(tid);
				LongCounter minVCTidCounter = minVC.vc.get(tid);
				if(curLevelStateVCTidCounter == null){
					minVC.vc.put(tid,new LongCounter(0));
				}
				else{
					if(curLevelStateVCTidCounter.val < minVCTidCounter.val){
						minVC.vc.put(tid,new LongCounter(curLevelStateVCTidCounter.val));
					}
				}
			}
		}
		Iterator<Tuple<Integer,VectorClock,Integer,Integer,Integer>> eventQueueItr = eventQueue.iterator();
		while(eventQueueItr.hasNext()){
			VectorClock eventVC = eventQueueItr.next().snd;
			if(VectorClock.isVC1LessThanOrEqualToVC2(eventVC,minVC)){
				eventQueueItr.remove();
				eventsRemoved++;
			}
		}
		
		//System.out.println("No of events removed = "+eventsRemoved);
		
	}
	
	public boolean isNextState(VectorClock curLevelStateVC, VectorClock eventVC, int eventThreadId, int eventTransitionId){
		
		Iterator<Integer> eventVCItr = eventVC.vc.keySet().iterator();
		while(eventVCItr.hasNext()){
			Integer tid = eventVCItr.next();
			LongCounter eventVCCounter = eventVC.vc.get(tid);
			LongCounter curLevelStateVCCounter = curLevelStateVC.vc.get(tid);
			if(tid.intValue() != eventThreadId){
				if((curLevelStateVCCounter == null) || (curLevelStateVCCounter.val < eventVCCounter.val)){			
					return false;
				}
			}
			else{
				if(curLevelStateVCCounter == null){
					if(eventVCCounter.val != 1){
						return false;
					}
				}
				else{
					if(!(curLevelStateVCCounter.val+1 == eventVCCounter.val)){
						return false;
					}
				}
			}
		}
		/***
		if((eventTransitionId == 1) || (eventTransitionId == 2)){
			System.out.println("checked if next state is possible");
			System.out.println("eventThreadId is "+eventThreadId);
			System.out.println("eventTransitionId is "+eventTransitionId);
			System.out.println("--curLevelVC--");
			curLevelStateVC.print();
			System.out.println("--eventVC--");
			eventVC.print();
			System.out.println("next state is possible");
		}
		***/
		return true;
		
	}
	
	public VectorClock createState(VectorClock curLevelState, VectorClock eventVC, int eventThreadId){
		VectorClock newState = new VectorClock(curLevelState);
		newState.inc(eventThreadId);
		return newState;
		
	}
	
	public Set<Integer> getAutomatonStates(Set<Integer> prevStates, int transitionId, int iid, TransitionList tlist, List<Pair<Integer,Integer>> setOfEvents, int m){
		Set<Integer> newStates = tlist.getStatesAfterTransition(prevStates, transitionId, iid, setOfEvents, m);
		return newStates;
		
	}
	
}