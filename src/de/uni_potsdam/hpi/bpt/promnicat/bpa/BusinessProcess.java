package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessProcess {
	
	List<Event> events = new ArrayList<Event>();
	private String name;
	final private UUID id;
	private String shapeId;
	
	public BusinessProcess() {
		id = UUID.randomUUID();
	}
	
	
	
	public BusinessProcess(String resourceId) {
		 this.shapeId = resourceId;
		 id = UUID.randomUUID();
	}
	
	public BusinessProcess(List<Event> events,String name) {
		id = UUID.randomUUID();
		this.events = events;
		this.name = name;
	}
	
	public BusinessProcess(String resourceId, List<Event> events,String name) {
		id = UUID.randomUUID();
		this.shapeId = resourceId;
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
	
	public UUID getUid() {
		return id;
	}
	
	public String getShapeId() {
		return shapeId;
	}
	public void setName(String name) {
		this.name = name;
	}
	

}
