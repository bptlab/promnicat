package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpt.graph.Edge;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.petri.*;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.throwable.SerializationException;

/**
 * Transforms a {@link BPA} (subset) into a Petri Net.
 * Assumes that all events of the BPA are unique. 
 * @author Marcin.Hewelt
  */
public class BPATransformer {
	
	/**
	 * TODO: Should I take a strategy to allow different types of transformations?
	 */
	public BPATransformer() {
	// nothing to do here
	}
	
	/**
	 * Transforms a given BPA into a {@link NetSystem}.
	 * @param bpa
	 * @return
	 */
	public NetSystem transform(BPA bpa) {
		List<BusinessProcess> processes = bpa.getAllProcesses();
		Map<BusinessProcess,PetriNet> resultingNets = new HashMap<BusinessProcess,PetriNet>();
		Map<Event, List<PetriNet>> intermediaryNets = new HashMap<Event, List<PetriNet>>();
		NetSystem bpaNet = new NetSystem();
		
		// process nets
		for (BusinessProcess process : processes) {
			resultingNets.put(process, transform(process));
		}
		// intermediary nets
		List<Event> allEvents = bpa.getEvents();
		for (Event event : allEvents) {
			List<PetriNet> transformed = transform(event);
			if (!transformed.isEmpty()) {
				intermediaryNets.put(event, transformed);
			}
		}
		// now compose them 
		Collection<PetriNet> allNets = new ArrayList<PetriNet>(); 
		allNets.addAll(resultingNets.values());
		for (List<PetriNet>	nets : intermediaryNets.values()) {
			allNets.addAll(nets);
		}
		bpaNet = compose(allNets);
		
		return bpaNet;
	}

	/**
	 * Transform a single {@link BusinessProcess} into a {@link PetriNet}.
	 * @param process
	 * @return a org.jbpt.petri.PetriNet
	 */
	private PetriNet transform(BusinessProcess process) {
		NetSystem processNet = new NetSystem();
		// iterate over events, construct process' net
		Boolean first = true;
		Place p, pPrime = null;
		Transition t;
		//Marking initialMarking = new Marking(processNet);
//		initialMarking.createMarking(processNet);
		Iterator<Event> iter = process.getEvents().iterator();
		while (iter.hasNext()) {	
			Event ev = iter.next();
			p = new Place("p_"+ev.getLabel());
			t = new Transition("t_"+ev.getLabel());
			processNet.addTransition(t);
			processNet.addPlace(p);
	
			// determine arc direction between p and t
			if (ev instanceof SendingEvent) {
				processNet.addEdge(t,p);
			} else if (ev instanceof ReceivingEvent) {
				processNet.addEdge(p, t);
			}

			// handle start event, no pPrime exists for it
			if (first) {
				first = false;
				if (ev instanceof StartEvent && ((StartEvent) ev).isInitialPlace()) { 
					processNet.getMarking().put(p,1);
				}
			} else {
				processNet.addEdge(pPrime, t);
			}

			// add new pPrime if not last element
			if (iter.hasNext()) { 
				pPrime = new Place("p'_"+ev.getLabel());
				processNet.addPlace(pPrime);
				processNet.addEdge(t, pPrime);
			} else {
				// TODO how to indicate final marking?
			}
		}
		return processNet;
	}

	/**
	 * Compose a list of {@link PetriNet}s merging places with 
	 * the same {@code getLabel()}.
	 * @param allNets
	 * @return a composed PetriNet
	 */
	private NetSystem compose(Collection<PetriNet> allNets) {
		NetSystem composedNet = new ComposingPetriNet();
		
		for (PetriNet pn : allNets) {
			for (Place p : pn.getPlaces()) {
				composedNet.addPlace(p);
			}
			for (AbstractDirectedEdge<Node> arc : pn.getEdges()) {
				Flow newFlow = composedNet.addFreshFlow(arc.getSource(), arc.getTarget());
				newFlow.setTag(arc.getTag());
				newFlow.setName(arc.getName());
			}
			if (pn instanceof NetSystem) {
				composedNet.getMarking().putAll(((NetSystem) pn).getMarking());
			}
		}
		return composedNet;
	}

	private class ComposingPetriNet extends NetSystem {

		Map<String,Place> existingPlaces = new HashMap<String,Place>();
		
		@Override
		/**
		 * Checks if place with the same name already exists
		 * before inserting.
		 */
		public Place addPlace(Place place) {
			Place added;
			String label = place.getLabel();
			if (! existingPlaces.containsKey(label)) {
				added = super.addPlace(place);
				existingPlaces.put(label, place);
			} else {
				added = existingPlaces.get(label);
			}
			return added;
		}

		@Override
		public Flow addFreshFlow(Node from, Node to) {
			Flow added;
			String fromLabel = from.getLabel();
			String toLabel = to.getLabel();
			if (from instanceof Place && existingPlaces.containsKey(fromLabel)) {
				added = super.addFreshFlow(existingPlaces.get(fromLabel), to);
			} else if (to instanceof Place && existingPlaces.containsKey(toLabel)) {
				added = super.addFreshFlow(from, existingPlaces.get(toLabel));
			} else {
				added = super.addFreshFlow(from, to);
			}
			return added;
		}
		
	}
	
	@SuppressWarnings("serial")
	/**
	 * Testing
	 * @param args
	 */
	public static void main(String[] args) {

		// transform it
		BPATransformer trans = new BPATransformer();
		BPA testBPA = BPAExamples.complexBPA();
		NetSystem pns = trans.transform(testBPA);
		pns.setName("Testnetz");
		String xmlString = InscriptionSerializer.serializeNet(pns);

		//jbpt serializing PNML requires NetSystem instead of PetriNet
		//NetSystem pns = trans.transform(BPAExamples.complexBPA);
		//System.out.println(InscriptionSerializer.serializeNet(pns));
		
		// serialize and write to file
		try {
			File file = new File(System.getenv("userprofile") + File.separator + "test.pnml");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xmlString);
			bw.close();
			System.out.println("Transformation complete, written to: " + file);
			System.out.println("Import with Renew (File-Import-XML-PNML) and choose Layout-Automatic Layout");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
	}

	

	/**
	 * Generates the intermediary nets for a given event. This needs to be
	 * a list because some events produce e.g. multicast and splitter net.
	 * If the given event needs has no intermediary net, an empty list is returned.
	 * @param event
	 * @return a list of intermediary {@link PetriNet}s or an empty list
	 */
	private List<PetriNet> transform(Event event) {
		List<PetriNet> intermediaryNet = new ArrayList<PetriNet>();
		
		//complicated distinction of cases
		// for SendingEvents
		if (event instanceof SendingEvent) {
			List<ReceivingEvent> post = ((SendingEvent) event).getPostset(); 
			if (post != null && !post.isEmpty()) { // postset not empty
				if (!event.hasTrivialMultiplicity() || // non-trivial or...
					(post.size() == 1 && // exactly one successor with trivial multiplicity 
					 post.get(0).hasTrivialMultiplicity() &&
					 post.get(0).getPreset().size() == 1)) {
					intermediaryNet.add(createMulticastNet((SendingEvent) event));
				}
				if (post.size() > 1) { // multiple successors
					intermediaryNet.add(createSplitterNet((SendingEvent) event));
				}
			}
		// now for ReceivingEvents
		} else if (event instanceof ReceivingEvent) {
			List<SendingEvent> pre = ((ReceivingEvent) event).getPreset();
			if (pre != null && !pre.isEmpty()) { //preset not empty
				if (!event.hasTrivialMultiplicity()) { // non-trivial
					intermediaryNet.add(createMultireceiverNet((ReceivingEvent) event));
				}
				if (pre.size() > 1) { // multiple predecessors
					intermediaryNet.add(createCollectorNet((ReceivingEvent) event));
				}
			}
		}
		return intermediaryNet;
	}
	
	/**
	 * Creates the collector net for the given {@link ReceivingEvent} assuming
	 * it requires such a net (this is not checked here). 
	 * @param a {@link ReceivingEvent} which requires a collector net
	 * @return the collector {@link PetriNet}
	 */
	private PetriNet createCollectorNet(ReceivingEvent event) {
		PetriNet collector = new PetriNet();
		List<SendingEvent> pre = event.getPreset();
		String eventLabel = event.getLabel();
		
		Place outPlace = new Place();
		if (event.hasTrivialMultiplicity()) {
			outPlace.setLabel("p_"+eventLabel);
		} else {
			outPlace.setLabel("p''_"+eventLabel);
		}
		for (SendingEvent predecessor : pre) {
			Place inPlace = new Place();
			Transition tmpTransition = new Transition("t_"+eventLabel);
			List<ReceivingEvent> predecessorPost = predecessor.getPostset();
			if (predecessorPost != null && predecessorPost.size() > 1) {
				inPlace.setLabel("p''_"+predecessor.getLabel()+"_"+eventLabel);
			} else {	
				if (!predecessor.hasTrivialMultiplicity()) {
					inPlace.setLabel("p''_"+predecessor.getLabel());
				} else {
					inPlace.setLabel("p_"+predecessor.getLabel());
				}
			}
			collector.addFlow(inPlace, tmpTransition);
			collector.addFlow(tmpTransition, outPlace);
		}
	
		
 		return collector;
	}

	/**
	 * Creates the multireceiver net for the given event assuming
	 * the event requires such a net (not checked here!)
	 * @param {@link ReceivingEvent}, that requires multireceiver net
	 * @return the multireceiver {@link PetriNet}
	 */
	private PetriNet createMultireceiverNet(ReceivingEvent event) {
		PetriNet multireceiver = new PetriNet();
		List<SendingEvent> pre = event.getPreset();
		String eventLabel = event.getLabel();
		
		Place outPlace = new Place("p_"+eventLabel); 
		multireceiver.addPlace(outPlace);
		Place inPlace = new Place();
		if (pre.size() == 1) {
			SendingEvent predecessor = pre.get(0);
			if (predecessor.getPostset().size() == 1) {
				if (predecessor.hasTrivialMultiplicity()) {
					inPlace.setLabel("p_"+predecessor.getLabel());
				} else {
					inPlace.setLabel("p_"+predecessor.getLabel()+"_"+eventLabel);
				}
			} else {
				inPlace.setLabel("p''_"+eventLabel);
			}
		} else if (pre.size() > 1) { 
			inPlace.setLabel("p''_"+eventLabel);
		}
		multireceiver.addPlace(inPlace);
		
		// now create and connect transitions
		Transition tmp;
		AbstractDirectedEdge<Node> inFlow;
		for (int mult : event.getMultiplicity()) {
			tmp =  new Transition(event.getLabel()+"_"+mult);
			multireceiver.addTransition(tmp);
			multireceiver.addEdge(tmp, outPlace);
			inFlow = multireceiver.addEdge(inPlace,tmp);
			inFlow.setTag(new Integer(mult));
			//inFlow.setName(new Integer(mult).toString());
		}
		
		return multireceiver;
	}
	
	/**
	 * Creates the splitter net for given event assuming it requires
	 * such a net (Not checked here!)
	 * @param {@link SendingEvent} which requires splitter net
	 * @return the splitter {@link PetriNet}
	 */
	private PetriNet createSplitterNet(SendingEvent event) {
		PetriNet splitter = new PetriNet();
		List<ReceivingEvent> post = event.getPostset();
		
		Place inPlace = new Place();
		String eventLabel = event.getLabel();
		if (event.hasTrivialMultiplicity()) {
			inPlace.setLabel("p_"+eventLabel);
		} else {
			inPlace.setLabel("p''_"+eventLabel);
		}
		splitter.addPlace(inPlace);
		Transition t = new Transition("t_"+eventLabel);
		splitter.addTransition(t);
		splitter.addFlow(inPlace, t);
		Place tmpPlace;
		for (ReceivingEvent successor : post) {
			tmpPlace = new Place();
			if (successor.getPreset().size() == 1) {
				if (successor.hasTrivialMultiplicity()) {
					tmpPlace.setLabel("p_"+successor.getLabel());
				} else {
					tmpPlace.setLabel("p''_"+successor.getLabel());
				}
			} else {
				tmpPlace.setLabel("p''_"+eventLabel+"_"+successor.getLabel());
			}
			splitter.addFlow(t, tmpPlace);
		}
		return splitter;
	}
	
	/**
	 * Creates the multicast net for the given event.
	 * @param event
	 * @return a {@link PetriNet} 
	 */
	private PetriNet createMulticastNet(SendingEvent event) {
		PetriNet multicaster = new PetriNet();
		List<ReceivingEvent> post = event.getPostset();
		
		Place inPlace = new Place("p_"+event.getLabel());
		multicaster.addPlace(inPlace );
		Place outPlace = new Place();
		// set the label of the output place (see Eid-Sabbagh+13b)
		if (post.size() == 1) {
			ReceivingEvent successor = post.get(0);
			if (successor.getPreset().size() == 1) {
				if (successor.hasTrivialMultiplicity()) {
					outPlace.setLabel("p_"+successor.getLabel());
				} else {
					outPlace.setLabel("p_"+event.getLabel()+"_"+successor.getLabel());
				}
			} else if (successor.getPreset().size() > 1) { // collector net for successor
				outPlace.setLabel("p''_"+event.getLabel());
			}
		} else if (post.size() > 1) { // splitter net was also created
			outPlace.setLabel("p''_"+event.getLabel());
		}
		multicaster.addPlace(outPlace);
		
		// now create and connect transitions
		Transition tmp;
		AbstractDirectedEdge<Node> outFlow;
		for (int mult : event.getMultiplicity()) {
			tmp =  new Transition(event.getLabel()+"_"+mult);
			multicaster.addTransition(tmp);
			multicaster.addEdge(inPlace, tmp);
			outFlow = multicaster.addEdge(tmp, outPlace);
			outFlow.setTag(new Integer(mult));
			//outFlow.setName("2");
		}
		return multicaster;
	}

	//TODO: Naming of places and transitions


}
