package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.List;
import java.util.UUID;

public class BusinessProcess {
	
	List<Event> events;
	private String name;
	final private UUID id;
	
	public BusinessProcess() {
		id = UUID.randomUUID();
	}
	
	public BusinessProcess(List<Event> events,String name) {
		id = UUID.randomUUID();
		this.events = events;
		this.name = name;
	}

	/**
	 * @param asList
	 */
	public BusinessProcess(List<Event> events) {
		id = UUID.randomUUID();
		this.events = events;
		// TODO Auto-generated constructor stub
	}

	public List<Event> getEvents() {
		return events;
	}
	
	public void addEvent(Event e) {
		events.add(e);
	}
	
	public void addEvents(List<Event> events) {
		events.addAll(events);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

}
