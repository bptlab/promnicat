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
	public IntermediateCatchingEvent(String eventid, int bpid, String label,
			int[] mult) {
		super(eventid, bpid, label, mult);
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public IntermediateCatchingEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 */
	public IntermediateCatchingEvent(int bpid, String label) {
		super(bpid, label);
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}
	
	/**
	 * @param label
	 */
	public IntermediateCatchingEvent(String label) {
		super(label);
		this.type = IntermediateCatchingEvent.EventType.CATCHINGINTEVENT;
	}

	public EventType getType() {
		return this.type;
	}

}
