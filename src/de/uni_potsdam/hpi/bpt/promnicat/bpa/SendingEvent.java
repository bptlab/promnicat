package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rami.eidsabbagh
 *
 */
public abstract class SendingEvent extends Event{
	
	List<ReceivingEvent> postset = new ArrayList<ReceivingEvent>();
	
	/**
	 * @param eventid
	 * @param bpid
	 * @param label
	 * @param mult 
	 */
	public SendingEvent(String eventid, int bpid, String label, int[] mult) {
		super(eventid, bpid, label, mult);
	}
	
	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public SendingEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
	}
	
	/**
	 * @param bpid
	 * @param label
	 */
	public SendingEvent(int bpid, String label) {
		super(bpid,label);
	}

	/**
	 * @param label
	 */
	public SendingEvent(String label) {
		super(label);
	}

	public void setPostset(List<ReceivingEvent> successors){
		postset = successors;
	}
	
	public List<ReceivingEvent> getPostset(){
		return postset;
	}
}
