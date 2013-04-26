package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.*;


public class Relation {
	
//	private static Relation instance;
	 
	private Map<SendingEvent, List<StartEvent>> triggers = new HashMap<SendingEvent, List<StartEvent>>();
	private Map<SendingEvent, IntermediateCatchingEvent> messages = new HashMap<SendingEvent, IntermediateCatchingEvent>();
	
	public Relation(){
	// nothing to do here	
	}
	
//	public static Relation getInstance(){
//		if(Relation.instance == null){
//			Relation.instance = new Relation();
//		}
//		return Relation.instance;
//	}
	
	public void addTriggers(SendingEvent send, List<StartEvent> start){
		
		if(triggers.get(send)!=null){
			List<StartEvent> triggeredevents = triggers.get(send);
			 
			ListIterator<StartEvent> litr = start.listIterator();
			    while (litr.hasNext()) {
			      triggeredevents.add(litr.next());			     
			    }
			
		}else{
			triggers.put(send, start);	
		}
		
	}
	
	
	public void addTrigger(SendingEvent send, StartEvent start){
		
		if(triggers.get(send)!=null){
			List<StartEvent> triggeredevents = triggers.get(send);
			      triggeredevents.add(start);
			      start.addToPreset(send);
		}else{
			List<StartEvent> triggeredevents = new ArrayList<StartEvent>();
			triggeredevents.add(start);
			triggers.put(send, triggeredevents);	
		}
		
	}
	
	public void addMessage(SendingEvent send, IntermediateCatchingEvent catchint){
		messages.put(send, catchint);
	}
	
	public List<StartEvent> getTriggered(SendingEvent send){
		
		List<StartEvent> triggeredevents = triggers.get(send);
		if(triggeredevents==null) triggeredevents = new ArrayList<StartEvent>();
		return triggers.get(send);
	}
}
