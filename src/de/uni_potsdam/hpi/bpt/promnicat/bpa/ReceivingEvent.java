/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rami.eidsabbagh
 *
 */
public class ReceivingEvent extends Event {

	private List<SendingEvent> preset = new ArrayList<SendingEvent>();
	
	public ReceivingEvent(int eventid, int bpid, String label, int[] mult) {
		super(eventid, bpid, label, mult);
	}
	
	public ReceivingEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
	}
	
	public ReceivingEvent(int bpid, String label) {
		super(bpid, label);
	}
	
	public List<SendingEvent> getPreset(){
		return preset;
	}
    
	public void setPreset(List<SendingEvent> predecessors){
		preset = predecessors;
	}

	public void addToPreset(SendingEvent send) {
		getPreset().add(send);
	}
}
