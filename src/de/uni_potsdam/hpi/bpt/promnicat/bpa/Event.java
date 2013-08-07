package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author rami.eidsabbagh
 *
 */
public abstract class Event {
private UUID id;
private String label;
private int[] multiplicity;
private EventType type;
public int owner; 

private static Map<Event, Integer> ids = new HashMap<Event, Integer>();
private static Integer maxId = 0;

public enum EventType{
	STARTEVENT,
	THROWINGINTEVENT,
	CATCHINGINTEVENT,
	ENDEVENT;
	
}

public Event(String eventid,int bpid, String label, int[] mult){
	this.id = UUID.fromString(eventid);
	this.owner = bpid;
	this.label = label;
	this.multiplicity = mult;
}


//private void setID(int newid){
//	this.id = newid;
//}


/**
 * Constructor which automatically creates id.
 * @param bpid
 * @param label
 * @param mult
 */
public Event(int bpid, String label, int[] mult) {
	this(UUID.randomUUID().toString(),bpid,label, mult);
}


/**
 * Constructor with trivial multiplicity.
 * @param bpid
 * @param label
 */
public Event (int bpid, String label) {
	this(bpid,label, new int[]{1});
}

/**
 * @param label
 */
public Event (String label){
	this(0,label);
}


public String getID(){
	return id.toString();
}

public void setLabel(String newlabel){
	this.label = newlabel;
}

public String getLabel(){
	return this.label;
}

public void setMultiplicity(int[] mult){
	this.multiplicity = mult;
}

public int[] getMultiplicity(){
	return this.multiplicity;
}

public int getOwner(){ 
	return this.owner;
}

//private void setOwner(int bpid){
//	this.owner = bpid;
//}

public EventType getType(){
	return this.type;
}

//private void setType(final EventType type){
//	this.type = type;
//}

public boolean hasTrivialMultiplicity() {
	return (multiplicity.length == 1 && multiplicity[0] == 1);
	
}


}
