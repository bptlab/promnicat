/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

/**
 * @author rami.eidsabbagh
 *
 */
public class IntermediateThrowingEvent extends SendingEvent {
	private EventType type;
	/**
	 * @param eventid
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public IntermediateThrowingEvent(String eventid, int bpid, String label,
			int[] mult) {
		super(eventid, bpid, label, mult);
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public IntermediateThrowingEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 */
	public IntermediateThrowingEvent(int bpid, String label) {
		super(bpid, label);
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}
	
	/**
	 * @param label
	 */
	public IntermediateThrowingEvent(String label) {
		super(label);
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}

	public EventType getType(){
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
		return this.type;
		
	}
}
