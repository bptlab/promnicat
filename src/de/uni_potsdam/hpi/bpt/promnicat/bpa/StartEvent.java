/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;



/**
 * @author rami.eidsabbagh
 *
 */
public class StartEvent extends ReceivingEvent {
	private EventType type;
	/**
	 * @param eventid
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public StartEvent(int eventid, int bpid, String label, int[] mult) {
		super(eventid, bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = StartEvent.EventType.STARTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public StartEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = StartEvent.EventType.STARTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 */
	public StartEvent(int bpid, String label) {
		super(bpid, label);
		// TODO Auto-generated constructor stub
		this.type = StartEvent.EventType.STARTEVENT;
	}
	
	public EventType getType() {
		return this.type;
	}

}
