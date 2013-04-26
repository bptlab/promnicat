package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.List;


public class BPA {

private String name = "";
private String organisation = "";
private List<BusinessProcess> processlist;
private Relation relations;

public void addProcess(BusinessProcess process){
	processlist.add(process);
}

public void setRelation(Relation relation){
	relations = relation;
}

public void addRelation(SendingEvent e1,  ReceivingEvent e2){
	if (e2 instanceof StartEvent) {
		Relation.getInstance().addTrigger(e1, (StartEvent) e2);
	} else if (e2 instanceof IntermediateCatchingEvent) {
		Relation.getInstance().addMessage(e1, (IntermediateCatchingEvent) e2);
	}
	
	
}


public List<Event> getEvents() {
	List<Event> events = new ArrayList<Event>();
	for (BusinessProcess bp : processlist) {
		events.addAll(bp.getEvents());
	}
	return events;
}

public List<BusinessProcess> getAllProcesses() {
	return processlist;
}

public void setProcesslist(List<BusinessProcess> processlist) {
	this.processlist = processlist;
}

//public void removeProcess(){
//
//}
//public void removeRelation(){
//	
//}
}

