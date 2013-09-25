/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;



/**
 * @author rami.eidsabbagh
 *
 */
public class EndEvent extends SendingEvent {
	
	private EventType type;
	/**
	 * @param eventid
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public EndEvent(String eventid, int bpid, String label, int[] mult) {
		super(eventid, bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = EndEvent.EventType.ENDEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public EndEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = EndEvent.EventType.ENDEVENT;
	}
	
	/**
	 * @param bpid
	 * @param label
	 */
	public EndEvent(int bpid, String label) {
		super(bpid, label);
		// TODO Auto-generated constructor stub
		this.type = EndEvent.EventType.ENDEVENT;
	}
	
	/**
	 * @param label
	 */
	public EndEvent(String label) {
		super(label);
		this.type = EndEvent.EventType.ENDEVENT;
	}

	public EventType getType() {
		this.type = EndEvent.EventType.ENDEVENT;
		return this.type;
	}
	
}
