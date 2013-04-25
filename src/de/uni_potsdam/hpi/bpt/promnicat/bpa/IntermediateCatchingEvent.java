/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import de.uni_potsdam.hpi.bpt.promnicat.bpa.Event.EventType;

/**
 * @author rami.eidsabbagh
 *
 */
public class IntermediateCatchingEvent extends ReceivingEvent {
	private EventType type;
	/**
	 * @param eventid
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public IntermediateCatchingEvent(int eventid, int bpid, String label,
			int[] mult) {
		super(eventid, bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public IntermediateCatchingEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 */
	public IntermediateCatchingEvent(int bpid, String label) {
		super(bpid, label);
		// TODO Auto-generated constructor stub
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}
	
	public EventType getType() {
		return this.type;
	}

}
