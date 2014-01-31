package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.petri.AbstractNetSystem;
import org.jbpt.petri.Flow;
import org.jbpt.petri.Marking;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;

/**
 * Transforms a {@link BPA} (subset) into a Petri Net.
 * Assumes that all events of the BPA are unique. 
 * @author Marcin.Hewelt
 * @author rami.eidsabbagh
 * 
 */
public class BPATransformer {

	private final String INTERNAL_PLACE = "intern_"; // replaces p'
	private final String INTERMEDIARY_PLACE = "q_"; // replaces p''
	private final String NORMAL_PLACE = "p_";
	private final String CHECK_START_PLACE = "p_cs_"; // for testing if a process started
	private final String CHECK_END_PLACE = "p_ce_"; // for testing if a process terminated

	private List<String> deadProcessFormulae = new ArrayList<String>();
	private List<String> terminatingProcessFormulae = new ArrayList<String>();
	private List<String> livelockFormulae = new ArrayList<String>();
	private StringBuilder terminatingFormula = new StringBuilder("FORMULA EXPATH EVENTUALLY ");
	private List<Formula> allFormulae = new ArrayList<Formula>(); 
	private StringBuilder lazyTerminatingFormula = new StringBuilder("FORMULA EXPATH EVENTUALLY ");
	
	private static Mapper mapper = Mapper.getInstance();
	/**
	 * TODO: Should I take a strategy to allow different types of
	 * transformations?
	 */
	public BPATransformer() {
	}

	/**
	 * Transforms a given BPA into a {@link NetSystem}.
	 * 
	 * @param bpa
	 * @return
	 */
	public NetSystem transform(BPA bpa) {
		
		System.out.println("Starting transformation of BPA " + bpa.getName());
		List<BusinessProcess> processes = bpa.getAllProcesses();
		Map<BusinessProcess, AbstractNetSystem<Flow,Node,Place,Transition,Marking>> resultingNets = new HashMap<BusinessProcess, AbstractNetSystem<Flow,Node,Place,Transition,Marking>>();
		Map<Event, List<AbstractNetSystem<Flow,Node,Place,Transition,Marking>>> intermediaryNets = new HashMap<Event, List<AbstractNetSystem<Flow,Node,Place,Transition,Marking>>>();
		NetSystem bpaNet = new NetSystem();
		
		for (BusinessProcess process : processes) {
			System.out.println("|- Transforming process " + process.getName());
			AbstractNetSystem<Flow, Node, Place, Transition, Marking> transformedProcess = transform(process);
			resultingNets.put(process, transformedProcess);
		}

		// intermediary nets
		List<Event> allEvents = bpa.getEvents();
		for (Event event : allEvents) {
			List<AbstractNetSystem<Flow,Node,Place,Transition,Marking>> transformed = transform(event);
			if (!transformed.isEmpty()) {
				System.out.println("|- Transforming event " + event.getLabel()
						+ " into " + transformed.size() + " intermediary net");
				intermediaryNets.put(event, transformed);
			}
		}

		// now compose them
		Collection<AbstractNetSystem<Flow,Node,Place,Transition,Marking>> allNets = new ArrayList<AbstractNetSystem<Flow,Node,Place,Transition,Marking>>();
		allNets.addAll(resultingNets.values());
		for (List<AbstractNetSystem<Flow,Node,Place,Transition,Marking>> nets : intermediaryNets.values()) {
			allNets.addAll(nets);
		}
		bpaNet = compose(allNets);
		for (Place p : bpaNet.getPlaces()) {
			terminatingFormula
					.append(p.getLabel()
							+ (bpaNet.getDirectSuccessors(p)
									.isEmpty() ? " > " : " = ") + "0 AND ");
		}
		int end = terminatingFormula.length();
		terminatingFormula.delete(end - 4, end);
		System.out.println("|= Formula for terminating run: "
				+ terminatingFormula);
		allFormulae.add(new Formula(terminatingFormula.toString(), CorrectnessCriteria.Termination,bpa.getCanvasId()));
		
		//construct lazy termination formula
		
		lazyTerminatingFormula.append("( ");
		Iterator<String> iterList = terminatingProcessFormulae.listIterator(); 
		while (iterList.hasNext()) {
			String nextPart = iterList.next().toString();
			System.out.println(nextPart);
			nextPart = nextPart.replaceAll("\\bFORMULA\\b","");
			System.out.println("after regex: "+nextPart);
				lazyTerminatingFormula.append("(( "+nextPart+" ) OR ( "+nextPart.replaceAll(">","=")+" )) AND ");	
					
		}
		
		lazyTerminatingFormula.append("( NOT (");
		Iterator<String> iterList2 = terminatingProcessFormulae.listIterator(); 
		while (iterList2.hasNext()) {
			String nextPart = iterList2.next().toString();
			System.out.println(nextPart);
			nextPart = nextPart.replace("FORMULA","");
			System.out.println("This is the string: "+nextPart);
				lazyTerminatingFormula.append("( "+nextPart.toString().replaceAll(">","=")+" ) AND ");	
			
		}
		end = lazyTerminatingFormula.length();
		lazyTerminatingFormula.delete(end - 4, end);
		lazyTerminatingFormula.append(")) AND (");
		// adding final marking
		for (Place p : bpaNet.getPlaces()) {
			if (p.getLabel().contains(CHECK_START_PLACE) || p.getLabel().contains(CHECK_END_PLACE) ) {
				
			}else{
			lazyTerminatingFormula
					.append( bpaNet.getDirectSuccessors(p)
									.isEmpty() ? p.getLabel()+" > 0 AND " :"");
			}
		}
		end = lazyTerminatingFormula.length();
		lazyTerminatingFormula.delete(end - 4, end);
		lazyTerminatingFormula.append("))");
		System.out.println("|= Formula for lazy terminating run: "
				+ lazyTerminatingFormula);
		allFormulae.add(new Formula(lazyTerminatingFormula.toString(), CorrectnessCriteria.LazyTermination,bpa.getCanvasId()));
		//construction of lazy terminating formula finished
		
		// construct liveness stateprop for each transition, livelock formula
		//StringBuilder livelockFormula = new StringBuilder(256);
		/*for (Transition t : bpaNet.getTransitions()) {
			livelockFormula.append("FORMULA ");
			Collection<Flow> incomingEdges = bpaNet.getIncomingEdges(t);
			for (Flow flow : incomingEdges) {
				Object inscription = flow.getTag();
				Integer x = 1;
				if (inscription != null) {
					try {
						x = (Integer) inscription;
					} catch (ClassCastException e) {
						System.out.println("Could not cast edge inscription "
								+ inscription + " to Integer.");
						e.printStackTrace();
					}
				}
				livelockFormula.append(flow.getSource().getLabel() + " >= " + x + " AND ");
			}
			
			// delete last "AND "
			end = livelockFormula.length();
			livelockFormula.delete(end-4, end);
			System.out.println(" --- Formula for transition " + t.getLabel() + ": " + livelockFormula);
			livelockFormulae.add(livelockFormula.toString());
			allFormulae.add(new Formula(livelockFormula.toString(), CorrectnessCriteria.NoLiveLocks ));
			livelockFormula.setLength(0); // reset StringBuilder
		}*/
		return bpaNet;
	}

	/**
	 * Transform a single {@link BusinessProcess} into a {@link PetriNet}.
	 * 
	 * @param process
	 * @return a org.jbpt.petri.PetriNet
	 */
	private AbstractNetSystem<Flow,Node,Place,Transition,Marking> transform(BusinessProcess process) {
		NetSystem processNet = new NetSystem();
		Boolean first = true;
		Place p, pIntern, pHelp, pPrime, pCheck = null;
		Transition t;
		StringBuilder formula = new StringBuilder("FORMULA ");
		
		StringBuilder formulaForLivelocks = new StringBuilder("FORMULA ");
		String processId = process.getShapeId();
		pHelp = new Place(INTERNAL_PLACE);
		// iterate over events, construct process' net
		Iterator<Event> iter = process.getEvents().iterator();
		while (iter.hasNext()) {
			Event ev = iter.next();
			p = new Place(NORMAL_PLACE + ev.getLabel());
			pIntern = new Place(INTERNAL_PLACE + ev.getLabel());
						
			t = new Transition("t_" + ev.getLabel());
			processNet.addTransition(t);
			processNet.addPlace(p);
			
			if (ev instanceof StartEvent) {
				pCheck = new Place(CHECK_START_PLACE + ev.getLabel());
				
				System.out.println(" -- Handling event " + ev.getLabel()
						+ ", created places " + p.getName() + " and test place "+pCheck.getLabel());
				
				processNet.addPlace(pCheck);
				
			} else if(ev instanceof EndEvent){
				pCheck = new Place(CHECK_END_PLACE + ev.getLabel());
				System.out.println(" -- Handling event " + ev.getLabel()
						+ ", created place " + p.getName()+ " and test place "+pCheck.getLabel());
				processNet.addPlace(pCheck);
			}
			
			
			
			// determine arc direction between p and t
			if (ev instanceof SendingEvent) {
				processNet.addEdge(t, p);
				processNet.addEdge(pHelp, t);
				if(ev.getType().equals(Event.EventType.ENDEVENT)){
					System.out.println(" Added edge : "+t.getLabel()+" place "+pCheck.getLabel());
					processNet.addEdge(t, pCheck);
					
				}else {
					processNet.addPlace(pIntern);
					processNet.addEdge(t, pIntern);
				}
				pHelp = pIntern;
			} else if (ev instanceof ReceivingEvent) {
				processNet.addEdge(p, t);
				processNet.addPlace(pIntern);
				processNet.addEdge(t, pIntern);
				// if (!first) formula.append(p.getLabel() + " = 0 AND ");
				System.out.println(ev.getType());
				if(ev.getType().equals(Event.EventType.STARTEVENT)){
					System.out.println(" Added edge place "+t.getLabel()+" transition "+pCheck.getLabel());
					processNet.addEdge(t, pCheck);
					
				}else {
					processNet.addEdge(pHelp, t);
				}
				pHelp = pIntern;
			}

			// handle start event, no pPrime exists for it
			if (first) {
				first = false;
				if (ev instanceof StartEvent) {
					// build CTL formula
					
					
					formula.append(pCheck.getLabel() + " > 0 ");
					
					//terminatingProcess.append(pCheck.getLabel() + " > 0 ");
					deadProcessFormulae.add(formula.toString());
					allFormulae.add(new Formula(formula.toString(), CorrectnessCriteria.NoDeadProcesses, processId));
					System.out.println("In dead process");
					formulaForLivelocks.append(p.getLabel() + " >= 1 ");
					allFormulae.add(new Formula(formulaForLivelocks.toString(), CorrectnessCriteria.NoLiveLocks, processId ));
					if (((StartEvent) ev).isInitialPlace()) {
						// put token on initial place
						processNet.getMarking().put(p, 1);
					}
				}
			} else if (iter.hasNext()){
				/*//pPrime = new Place(INTERNAL_PLACE + ev.getLabel());
				processNet.addPlace(pPrime);
				if (ev instanceof IntermediateCatchingEvent) {
					processNet.addEdge(pPrime, t);
						
				}else if(ev instanceof IntermediateThrowingEvent) {
					processNet.addEdge(t, pPrime);
				}*/
				
												
			}else { // for end event
				if (ev instanceof EndEvent) {
					formula.append("AND "+pCheck.getLabel()+" > 0");
					System.out.println("in terminating process formula");
					terminatingProcessFormulae.add(formula.toString());
					allFormulae.add(new Formula(formula.toString(), CorrectnessCriteria.TerminatingProcess, processId ));
				}
			} 

			// add new pPrime if not last element
//			if (iter.hasNext()) {
//				pPrime = new Place(INTERNAL_PLACE + ev.getLabel());
//				processNet.addPlace(pPrime);
//				processNet.addEdge(t, pPrime);
//			} else {
//				// TODO: Last element, necessary for lazy termination
//			}
		}
		
		
		
		//System.out.println(" -- State predicate to check for terminating process: "+ formula);
		
		//allFormulae.add(new Formula(formula.toString(), CorrectnessCriteria.NoDeadProcesses));
		return processNet;
	}

	/**
	 * Compose a list of {@link PetriNet}s merging places with the same
	 * {@code getLabel()}.
	 * 
	 * @param allNets
	 * @return a composed PetriNet
	 */
	private NetSystem compose(Collection<AbstractNetSystem<Flow,Node,Place,Transition,Marking>> allNets) {
		NetSystem composedNet = new ComposingPetriNet();
		System.out.println("Starting composition of " + allNets.size()
				+ " nets.");
		for (AbstractNetSystem<Flow,Node,Place,Transition,Marking> pn : allNets) {
			System.out.println(" - Merging petri net " + pn);
			for (Place p : pn.getPlaces()) {
				composedNet.addPlace(p);
			}
			for (AbstractDirectedEdge<Node> arc : pn.getEdges()) {
				Flow newFlow = composedNet.addFlow(arc.getSource(),
						arc.getTarget());
				newFlow.setTag(arc.getTag());
				newFlow.setName(arc.getName());
			}
			if (pn instanceof NetSystem) {
				Marking mark = ((NetSystem) pn).getMarking();
				System.out.println("  -- Marking: " + mark);
				composedNet.getMarking().putAll(((NetSystem) pn).getMarking());
			}
		}
			
		
		return composedNet;
	}

	private class ComposingPetriNet extends NetSystem {

		Map<String, Place> existingPlaces = new HashMap<String, Place>();

		@Override
		/**
		 * Checks if place with the same name already exists
		 * before inserting.
		 */
		public Place addPlace(Place place) {
			Place added;
			String label = place.getLabel();
			if (!existingPlaces.containsKey(label)) {
				added = super.addPlace(place);
				existingPlaces.put(label, place);
			} else {
				added = existingPlaces.get(label);
			}
			return added;
		}

		public Flow addFreshFlow(Node from, Node to) {
			Flow added;
			String fromLabel = from.getLabel();
			String toLabel = to.getLabel();
			if (from instanceof Place && existingPlaces.containsKey(fromLabel)) {
				added = super.addFlow(existingPlaces.get(fromLabel), to);
			} else if (to instanceof Place
					&& existingPlaces.containsKey(toLabel)) {
				added = super.addFlow(from, existingPlaces.get(toLabel));
			} else {
				added = super.addFlow(from, to);
			}
			return added;
		}

	}

	/**
	 * Testing
	 * 
	 * @param args
	 * @deprecated Use BPAAnalyzer instead!
	 */
	public static void main(String[] args) {
		// no longer used!
		BPA bpa = BPAExamples.complexBPA();
		new BPATransformer().transform(bpa);
	}

	/**
	 * Generates the intermediary nets for a given event. This needs to be a
	 * list because some events produce e.g. multicast and splitter net. If the
	 * given event needs has no intermediary net, an empty list is returned.
	 * 
	 * @param event
	 * @return a list of intermediary {@link PetriNet}s or an empty list
	 */
	private List<AbstractNetSystem<Flow, Node, Place, Transition, Marking>> transform(Event event) {
		List<AbstractNetSystem<Flow, Node, Place, Transition, Marking>> intermediaryNet = new ArrayList<AbstractNetSystem<Flow,Node,Place,Transition,Marking>>();

		// complicated distinction of cases
		// for SendingEvents
		if (event instanceof SendingEvent) {
			List<ReceivingEvent> post = ((SendingEvent) event).getPostset();
			if (post != null && !post.isEmpty()) { // postset not empty
				if (!event.hasTrivialMultiplicity() || // non-trivial or...
						(post.size() == 1 && // exactly one successor with
												// trivial multiplicity
								post.get(0).hasTrivialMultiplicity() && post
								.get(0).getPreset().size() == 1)) {
					intermediaryNet
							.add(createMulticastNet((SendingEvent) event));
				}
				if (post.size() > 1) { // multiple successors
					intermediaryNet
							.add(createSplitterNet((SendingEvent) event));
				}
			}
			// now for ReceivingEvents
		} else if (event instanceof ReceivingEvent) {
			List<SendingEvent> pre = ((ReceivingEvent) event).getPreset();
			if (pre != null && !pre.isEmpty()) { // preset not empty
				if (!event.hasTrivialMultiplicity()) { // non-trivial
					intermediaryNet
							.add(createMultireceiverNet((ReceivingEvent) event));
				}
				if (pre.size() > 1) { // multiple predecessors
					intermediaryNet
							.add(createCollectorNet((ReceivingEvent) event));
				}
			}
		}
		return intermediaryNet;
	}

	/**
	 * Creates the collector net for the given {@link ReceivingEvent} assuming
	 * it requires such a net (this is not checked here).
	 * 
	 * @param a
	 *            {@link ReceivingEvent} which requires a collector net
	 * @return the collector {@link PetriNet}
	 */
	private AbstractNetSystem<Flow,Node,Place,Transition,Marking> createCollectorNet(ReceivingEvent event) {
		AbstractNetSystem<Flow,Node,Place,Transition,Marking> collector = new NetSystem();
		List<SendingEvent> pre = event.getPreset();
		String eventLabel = event.getLabel();
		String placeName = null;
		Place outPlace = new Place();
		if (event.hasTrivialMultiplicity()) {
			// case 1: no multireceive net, directly connected
			outPlace.setLabel(NORMAL_PLACE + eventLabel);
		} else {
			// case 2 : connect to in-place of multireceive net
			placeName = INTERMEDIARY_PLACE + eventLabel;
			outPlace.setLabel(placeName);
		}
		int transitionI = 0;
		for (SendingEvent predecessor : pre) {
			Place inPlace = new Place();
			Transition tmpTransition = new Transition("t_" + eventLabel+transitionI);
			List<ReceivingEvent> predecessorPost = predecessor.getPostset();
			if (predecessorPost != null && predecessorPost.size() > 1) {
				placeName = INTERMEDIARY_PLACE + predecessor.getLabel() + "_"
						+ eventLabel;
				inPlace.setLabel(placeName);
			} else {
				if (!predecessor.hasTrivialMultiplicity()) {
					placeName = INTERMEDIARY_PLACE + predecessor.getLabel();
					inPlace.setLabel(placeName);
				} else {
					inPlace.setLabel(NORMAL_PLACE + predecessor.getLabel());
				}
			}
			collector.addFlow(inPlace, tmpTransition);
			collector.addFlow(tmpTransition, outPlace);
			transitionI++;
		}
		System.out.println(" --- place " + collector.getPlaces());
		if (placeName != null)
			terminatingFormula.append(placeName + " = 0 AND ");
		return collector;
	}

	/**
	 * Creates the multireceiver net for the given event assuming the event
	 * requires such a net (not checked here!)
	 * 
	 * @param {@link ReceivingEvent}, that requires multireceiver net
	 * @return the multireceiver {@link PetriNet}
	 */
	private AbstractNetSystem<Flow,Node,Place,Transition,Marking> createMultireceiverNet(ReceivingEvent event) {
		AbstractNetSystem<Flow,Node,Place,Transition,Marking> multireceiver = new NetSystem();
		List<SendingEvent> pre = event.getPreset();
		String eventLabel = event.getLabel();

		Place outPlace = new Place(NORMAL_PLACE + eventLabel);
		multireceiver.addPlace(outPlace);
		Place inPlace = new Place();
		String placeName = null;
		if (pre.size() == 1) {
			SendingEvent predecessor = pre.get(0);
			if (predecessor.getPostset().size() == 1) {
				if (predecessor.hasTrivialMultiplicity()) {
					inPlace.setLabel(NORMAL_PLACE + predecessor.getLabel());
				} else {
					placeName = NORMAL_PLACE + predecessor.getLabel() + "_"
							+ eventLabel;
					inPlace.setLabel(placeName);
				}
			} else {
				placeName = INTERMEDIARY_PLACE + eventLabel;
				inPlace.setLabel(placeName);
			}
		} else if (pre.size() > 1) {
			placeName = INTERMEDIARY_PLACE + eventLabel;
			inPlace.setLabel(placeName);
		}
		if (placeName != null)
			terminatingFormula.append(placeName + " = 0 AND ");
		multireceiver.addPlace(inPlace);

		// now create and connect transitions
		Transition tmp;
		AbstractDirectedEdge<Node> inFlow;
		for (int mult : event.getMultiplicity()) {
			tmp = new Transition(event.getLabel() + "_" + mult);
			multireceiver.addTransition(tmp);
			multireceiver.addEdge(tmp, outPlace);
			inFlow = multireceiver.addEdge(inPlace, tmp);
			inFlow.setTag(new Integer(mult));
			// inFlow.setName(new Integer(mult).toString());
		}
		System.out.println(" -- places: " + multireceiver.getPlaces());
		return multireceiver;
	}

	/**
	 * Creates the splitter net for given event assuming it requires such a net
	 * (Not checked here!)
	 * 
	 * @param {@link SendingEvent} which requires splitter net
	 * @return the splitter {@link PetriNet}
	 */
	private AbstractNetSystem<Flow,Node,Place,Transition,Marking> createSplitterNet(SendingEvent event) {
		AbstractNetSystem<Flow,Node,Place,Transition,Marking> splitter = new NetSystem();
		List<ReceivingEvent> post = event.getPostset();

		Place inPlace = new Place();
		String eventLabel = event.getLabel();
		if (event.hasTrivialMultiplicity()) {
			inPlace.setLabel(NORMAL_PLACE + eventLabel);
		} else {
			inPlace.setLabel(INTERMEDIARY_PLACE + eventLabel);
		}
		splitter.addPlace(inPlace);
		Transition t = new Transition("t_split_" + eventLabel);
		splitter.addTransition(t);
		splitter.addFlow(inPlace, t);
		Place tmpPlace;
		String placeName = null;
		for (ReceivingEvent successor : post) {
			tmpPlace = new Place();
			if (successor.getPreset().size() == 1) {
				if (successor.hasTrivialMultiplicity()) {
					tmpPlace.setLabel(NORMAL_PLACE + successor.getLabel());
				} else {
					placeName = INTERMEDIARY_PLACE + successor.getLabel();
					tmpPlace.setLabel(placeName);
				}
			} else {
				placeName = INTERMEDIARY_PLACE + eventLabel + "_"
						+ successor.getLabel();
				tmpPlace.setLabel(placeName);
			}
			splitter.addFlow(t, tmpPlace);
		}
		if (placeName != null)
			terminatingFormula.append(placeName + " = 0 AND ");
		return splitter;
	}

	/**
	 * Creates the multicast net for the given event.
	 * 
	 * @param event
	 * @return a {@link PetriNet}
	 */
	private AbstractNetSystem<Flow,Node,Place,Transition,Marking> createMulticastNet(SendingEvent event) {
		AbstractNetSystem<Flow,Node,Place,Transition,Marking> multicaster = new NetSystem();
		List<ReceivingEvent> post = event.getPostset();

		Place inPlace = new Place(NORMAL_PLACE + event.getLabel());
		multicaster.addPlace(inPlace);
		Place outPlace = new Place();
		// set the label of the output place (see Eid-Sabbagh+13b)
		String placeName = null;
		if (post.size() == 1) {
			ReceivingEvent successor = post.get(0);
			if (successor.getPreset().size() == 1) {
				if (successor.hasTrivialMultiplicity()) {
					outPlace.setLabel(NORMAL_PLACE + successor.getLabel());
				} else {
					placeName = NORMAL_PLACE + event.getLabel() + "_"
							+ successor.getLabel();
					outPlace.setLabel(placeName);
				}
			} else if (successor.getPreset().size() > 1) { // collector net for
															// successor
				placeName = INTERMEDIARY_PLACE + event.getLabel();
				outPlace.setLabel(placeName);
			}
		} else if (post.size() > 1) { // splitter net was also created
			placeName = INTERMEDIARY_PLACE + event.getLabel();
			outPlace.setLabel(placeName);
		}
		if (placeName != null)
			terminatingFormula.append(placeName + " = 0 AND ");
		multicaster.addPlace(outPlace);

		// now create and connect transitions
		Transition tmp;
		AbstractDirectedEdge<Node> outFlow;
		for (int mult : event.getMultiplicity()) {
			tmp = new Transition(event.getLabel() + "_" + mult);
			multicaster.addTransition(tmp);
			multicaster.addEdge(inPlace, tmp);
			outFlow = multicaster.addEdge(tmp, outPlace);
			outFlow.setTag(new Integer(mult));
			// outFlow.setName("2");
		}
		System.out.println(" -- places: " + multicaster.getPlaces());
		return multicaster;
	}

	protected List<String> getDeadProcessFormulae() {
		return deadProcessFormulae;
	}

	protected List<String> getLivelockFormulae() {
		return livelockFormulae;
	}

	protected List<String> getTerminatingFormula() {
		List<String> result = new ArrayList<String>();
		result.add(terminatingFormula.toString()); 
		return result;
	}
	
	protected List<String> getLazyTerminatingFormula() {
		List<String> result = new ArrayList<String>();
		result.add(lazyTerminatingFormula.toString()); 
		//String result = lazyTerminatingFormula.toString(); 
		return result;
	}

	protected List<Formula> getAllFormulae() {
		return allFormulae;
	}

	// TODO: Naming of places and transitions

}
