package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.*;


public class Relation {
	
	private static Relation instance;
	 
	private Map<SendingEvent, ArrayList<StartEvent>> triggers = new HashMap<SendingEvent, ArrayList<StartEvent>>();
	private Map<SendingEvent, IntermediateCatchingEvent> messages = new HashMap<SendingEvent, IntermediateCatchingEvent>();
	
	private Relation(){
		
	}
	
	public static Relation getInstance(){
		if(Relation.instance == null){
			Relation.instance = new Relation();
		}
		return Relation.instance;
	}
	
	public void addTrigger(SendingEvent send, ArrayList<StartEvent> start){
		
		if(triggers.get(send)!=null){
			ArrayList<StartEvent> triggeredevents = triggers.get(send);
			 
			ListIterator<StartEvent> litr = start.listIterator();
			    while (litr.hasNext()) {
			      triggeredevents.add(litr.next());			     
			    }
			
		}else{
			triggers.put(send, start);	
		}
		
	}
	
	public void addMessage(SendingEvent send, IntermediateCatchingEvent catchint){
		messages.put(send, catchint);
	}
	
	public ArrayList<StartEvent> getTriggered(SendingEvent send){
		
		ArrayList<StartEvent> triggeredevents = triggers.get(send);
		return triggeredevents;
	}
}
