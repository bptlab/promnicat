package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.Arrays;
import java.util.List;

/**
 * This class collects exemplary BPAs.
 * @author Marcin.Hewelt
 *
 */
public class BPAExamples {
	
	public static BPA simpleBPA() {
		BPA simpleBPA = new BPA();
		Event e1 = new StartEvent(0, "b");
		SendingEvent e2 = new IntermediateThrowingEvent(0,"s", new int[]{1,2,3});
		List<Event> eventsA = Arrays.asList(e1,e2);
		BusinessProcess broadcaster = new BusinessProcess(eventsA , "Broadcaster");
		
		Event e3 = new StartEvent(1, "c");
		ReceivingEvent e4 = new IntermediateCatchingEvent(1, "r");
		List<Event> eventsB = Arrays.asList(e3,e4);
		BusinessProcess receiver = new BusinessProcess(eventsB , "Receiver");
		
//		e2.setPostset(Arrays.asList(e4));
//		e4.setPreset(Arrays.asList(e2));
		simpleBPA.addProcess(broadcaster);
		simpleBPA.addProcess(receiver);
		simpleBPA.addRelation(e2, e4);
		
		return simpleBPA;
	}

	public static BPA complexBPA() {
		final StartEvent e0 = new StartEvent(2, "p", new int[]{1});
		final SendingEvent e1 = new EndEvent(2,"q", new int[]{1,2} );
		
		final ReceivingEvent e2 = new IntermediateCatchingEvent(4, "r",new int[]{3,4} );
		final SendingEvent e3 = new IntermediateThrowingEvent(4,"s", new int[]{1});
		final SendingEvent e4 = new EndEvent(4, "t", new int[]{1});

		final ReceivingEvent e5 = new IntermediateCatchingEvent(9, "u", new int[]{1});
		final ReceivingEvent e6 = new IntermediateCatchingEvent(9,"v", new int[]{1} );
		final SendingEvent e9 = new IntermediateThrowingEvent(9,"z", new int[]{1} );
		
		final ReceivingEvent e7 = new StartEvent(12, "x", new int[]{1});
		final SendingEvent e8 = new EndEvent(12,"y", new int[]{1} );
		
		
//		e1.setPostset(Arrays.asList(e2,e5));
//		e2.setPreset(Arrays.asList(e1));
//		e5.setPreset(Arrays.asList(e1,e8));
//		e8.setPostset(Arrays.asList(e5));
//		e3.setPostset(Arrays.asList(e7));
//		e7.setPreset(Arrays.asList(e3));
//		e4.setPostset(Arrays.asList(e6));
//		e6.setPreset(Arrays.asList(e4));
		
		BusinessProcess p1 = new BusinessProcess(Arrays.asList(e2,e4,e3), "P1");
		BusinessProcess p2 = new BusinessProcess(Arrays.asList(e0, e1),"P2");
		BusinessProcess p3 = new BusinessProcess(Arrays.asList(e5, e6, e9),"P3");
		BusinessProcess p4 = new BusinessProcess(Arrays.asList(e7, e8),"P4");
		BPA bpa = new BPA();
		bpa.setProcesslist(Arrays.asList(p1,p2,p3,p4));
		bpa.addRelation(e1, e2);
		bpa.addRelation(e1, e5);
		bpa.addRelation(e8, e5);
		return bpa;
	}
	
	public static void main(String[] args) {
		//testing Arrays.asList
		List<String> words = Arrays.asList("noten", "indien");
		//words.add("unten"); unsupported operation
		//words.remove(1); unsupported operation
		words.set(0, "unten"); // this works
		System.out.println(words);
	}
}
