package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jbpt.graph.Edge;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.petri.*;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.throwable.SerializationException;
import org.json.JSONArray;
import org.omg.CosNaming.NamingContextPackage.NotEmpty;

/**
 * Transforms a {@link BPA} (subset) into a Petri Net.
 * Assumes that all events of the BPA are unique. 
 * @author Marcin.Hewelt
 */
/**
 * @author rami.eidsabbagh
 * 
 */
public class BPATransformer {

	private final String INTERNAL_PLACE = "intern_"; // replaces p'
	private final String INTERMEDIARY_PLACE = "q_"; // replaces p''
	private final String NORMAL_PLACE = "p_";

	private List<String> deadProcessFormulae = new ArrayList<String>();
	private List<String> livelockFormulae = new ArrayList<String>();
	private StringBuilder terminatingFormula = new StringBuilder("FORMULA ");

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
		Map<BusinessProcess, PetriNet> resultingNets = new HashMap<BusinessProcess, PetriNet>();
		Map<Event, List<PetriNet>> intermediaryNets = new HashMap<Event, List<PetriNet>>();
		NetSystem bpaNet = new NetSystem();

		for (BusinessProcess process : processes) {
			System.out.println("|- Transforming process " + process.getName());
			PetriNet transformedProcess = transform(process);
			resultingNets.put(process, transformedProcess);
			for (Place p : transformedProcess.getPlaces()) {
				terminatingFormula
						.append(p.getLabel()
								+ (transformedProcess.getDirectSuccessors(p)
										.isEmpty() ? " > " : " = ") + "0 AND ");
			}
		}

		// intermediary nets
		List<Event> allEvents = bpa.getEvents();
		for (Event event : allEvents) {
			List<PetriNet> transformed = transform(event);
			if (!transformed.isEmpty()) {
				System.out.println("|- Transforming event " + event.getLabel()
						+ " into " + transformed.size() + " intermediary net");
				intermediaryNets.put(event, transformed);
			}
		}
		int end = terminatingFormula.length();
		terminatingFormula.delete(end - 4, end);
		System.out.println("|= Formula for terminating run: "
				+ terminatingFormula);

		// now compose them
		Collection<PetriNet> allNets = new ArrayList<PetriNet>();
		allNets.addAll(resultingNets.values());
		for (List<PetriNet> nets : intermediaryNets.values()) {
			allNets.addAll(nets);
		}
		bpaNet = compose(allNets);

		// construct liveness stateprop for each transition, livelock formula
		StringBuilder livelockFormula = new StringBuilder(256);
		for (Transition t : bpaNet.getTransitions()) {
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
			livelockFormula.setLength(0); // reset StringBuilder
		}
		return bpaNet;
	}

	/**
	 * Transform a single {@link BusinessProcess} into a {@link PetriNet}.
	 * 
	 * @param process
	 * @return a org.jbpt.petri.PetriNet
	 */
	private PetriNet transform(BusinessProcess process) {
		NetSystem processNet = new NetSystem();
		Boolean first = true;
		Place p, pPrime = null;
		Transition t;
		StringBuilder formula = new StringBuilder("FORMULA ");
		// iterate over events, construct process' net
		Iterator<Event> iter = process.getEvents().iterator();
		while (iter.hasNext()) {
			Event ev = iter.next();
			p = new Place(NORMAL_PLACE + ev.getLabel());
			t = new Transition("t_" + ev.getLabel());
			System.out.println(" -- Handling event " + ev.getLabel()
					+ ", created place " + p.getName());
			processNet.addTransition(t);
			processNet.addPlace(p);

			// determine arc direction between p and t
			if (ev instanceof SendingEvent) {
				processNet.addEdge(t, p);
			} else if (ev instanceof ReceivingEvent) {
				processNet.addEdge(p, t);
				// if (!first) formula.append(p.getLabel() + " = 0 AND ");
			}

			// handle start event, no pPrime exists for it
			if (first) {
				first = false;
				if (ev instanceof StartEvent) {
					// build CTL formula
					formula.append(p.getLabel() + " > 0 ");
					if (((StartEvent) ev).isInitialPlace()) {
						// put token on initial place
						processNet.getMarking().put(p, 1);
					}
				}
			} else {
				processNet.addEdge(pPrime, t);
			}

			// add new pPrime if not last element
			if (iter.hasNext()) {
				pPrime = new Place(INTERNAL_PLACE + ev.getLabel());
				processNet.addPlace(pPrime);
				processNet.addEdge(t, pPrime);
			} else {
				// TODO: Last element, necessary for lazy termination
			}
		}
		System.out.println(" -- State predicate to check dead process: "
				+ formula);
		deadProcessFormulae.add(formula.toString());
		return processNet;
	}

	/**
	 * Compose a list of {@link PetriNet}s merging places with the same
	 * {@code getLabel()}.
	 * 
	 * @param allNets
	 * @return a composed PetriNet
	 */
	private NetSystem compose(Collection<PetriNet> allNets) {
		NetSystem composedNet = new ComposingPetriNet();
		System.out.println("Starting composition of " + allNets.size()
				+ " nets.");
		for (PetriNet pn : allNets) {
			System.out.println(" - Merging petri net " + pn);
			for (Place p : pn.getPlaces()) {
				composedNet.addPlace(p);
			}
			for (AbstractDirectedEdge<Node> arc : pn.getEdges()) {
				Flow newFlow = composedNet.addFreshFlow(arc.getSource(),
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

		@Override
		public Flow addFreshFlow(Node from, Node to) {
			Flow added;
			String fromLabel = from.getLabel();
			String toLabel = to.getLabel();
			if (from instanceof Place && existingPlaces.containsKey(fromLabel)) {
				added = super.addFreshFlow(existingPlaces.get(fromLabel), to);
			} else if (to instanceof Place
					&& existingPlaces.containsKey(toLabel)) {
				added = super.addFreshFlow(from, existingPlaces.get(toLabel));
			} else {
				added = super.addFreshFlow(from, to);
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
	private List<PetriNet> transform(Event event) {
		List<PetriNet> intermediaryNet = new ArrayList<PetriNet>();

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
	private PetriNet createCollectorNet(ReceivingEvent event) {
		PetriNet collector = new PetriNet();
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
		for (SendingEvent predecessor : pre) {
			Place inPlace = new Place();
			Transition tmpTransition = new Transition("t_" + eventLabel);
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
	private PetriNet createMultireceiverNet(ReceivingEvent event) {
		PetriNet multireceiver = new PetriNet();
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
	private PetriNet createSplitterNet(SendingEvent event) {
		PetriNet splitter = new PetriNet();
		List<ReceivingEvent> post = event.getPostset();

		Place inPlace = new Place();
		String eventLabel = event.getLabel();
		if (event.hasTrivialMultiplicity()) {
			inPlace.setLabel(NORMAL_PLACE + eventLabel);
		} else {
			inPlace.setLabel(INTERMEDIARY_PLACE + eventLabel);
		}
		splitter.addPlace(inPlace);
		Transition t = new Transition("t_" + eventLabel);
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
	private PetriNet createMulticastNet(SendingEvent event) {
		PetriNet multicaster = new PetriNet();
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

	protected List<String> getFormulae() {
		return deadProcessFormulae;
	}

	// TODO: Naming of places and transitions

}
