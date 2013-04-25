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
	public IntermediateThrowingEvent(int eventid, int bpid, String label,
			int[] mult) {
		super(eventid, bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public IntermediateThrowingEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		// TODO Auto-generated constructor stub
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 */
	public IntermediateThrowingEvent(int bpid, String label) {
		super(bpid, label);
		// TODO Auto-generated constructor stub
		this.type = IntermediateThrowingEvent.EventType.THROWINGINTEVENT;
	}
	
	public EventType getType(){
		return this.type;
	}
}
