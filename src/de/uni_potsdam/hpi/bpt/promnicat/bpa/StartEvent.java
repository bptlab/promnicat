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
	public StartEvent(String eventid, int bpid, String label, int[] mult) {
		super(eventid, bpid, label, mult);
		this.type = ReceivingEvent.EventType.STARTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 * @param mult
	 */
	public StartEvent(int bpid, String label, int[] mult) {
		super(bpid, label, mult);
		this.type = ReceivingEvent.EventType.STARTEVENT;
	}

	/**
	 * @param bpid
	 * @param label
	 */
	public StartEvent(int bpid, String label) {
		super(bpid, label);
		this.type = StartEvent.EventType.STARTEVENT;
	}
	
	/**
	 * @param label
	 */
	public StartEvent(String label) {
		super(label);
		this.type = StartEvent.EventType.STARTEVENT;
	}

	public EventType getType() {
		this.type = StartEvent.EventType.STARTEVENT;
		return this.type;
	}
	
	/**
	 * Checks whether this StartEvent is an initial place, ie. whether
	 * is has no incoming triggers. In this case its place in the 
	 * PetriNet representation is marked.
	 * @return
	 */
	public boolean isInitialPlace() {
		return (getPreset() == null || getPreset().isEmpty());
	}

}
