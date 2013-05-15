package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jbpt.graph.Edge;
import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.petri.*;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.throwable.SerializationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextPackage.NotEmpty;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
import de.uni_potsdam.hpi.bpt.ai.diagram.DiagramBuilder;
import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;

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
	
	/**
	 * @param args
	 */
	@SuppressWarnings("serial")
	/**
	 * Testing
	 * @param args
	 */
	public static void main(String[] args) {
		
		// read json file
		//File jsonPath =  new File(System.getenv("userprofile") + File.separator + "test.json");
		try {
			Path jsonPath = Paths.get(System.getenv("userprofile") + File.separator + "test.xml");
			
			// DOMBuilder in-memory, ok because DOM is small
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(jsonPath.toFile());
			NodeList json = doc.getElementsByTagName("json-representation");
			if (json.getLength() > 0) {
				org.w3c.dom.Node first = json.item(0);
				JSONObject j = new JSONObject(first.getTextContent());
				for (Iterator it = j.keys(); it.hasNext();) {
					String key = (String) it.next();
					Object obj = j.get(key);
					if (obj instanceof String) {
						String s = (String) obj;
					}
					//System.out.println(key+"-> "+obj);
				}
				
				// let bpmai parse json to diagram
				Diagram diag = DiagramBuilder.parseJson(j);
                List<Shape> shapes = diag.getShapes();
                Map<String, Event> eventMapper = new HashMap<String, Event>();
                Map<String,List<String>> postSetMapper = new HashMap<String, List<String>>();
                Map<String,List<String>> preSetMapper = new HashMap<String, List<String>>();
                Map<BusinessProcess,List<String>> processEventMapper = new HashMap<BusinessProcess, List<String>>();
                for (Shape shape : shapes) {
					String stencilId = shape.getStencilId();
					String resourceId = shape.getResourceId();
					if (isSending(stencilId)) {
						// TODO here we get Message-Shape
						List<Shape> out = shape.getOutgoings();
						List<String> outIds = new ArrayList<String>();
						for (Shape messageShape : out) {
							// Puts Target Event Id into ArrayList
							outIds.add(messageShape.getTarget().getResourceId());
							if (null == preSetMapper.get(messageShape.getTarget().getResourceId())) {
								List<String> inIds = new ArrayList<String>();
								inIds.add(resourceId);
								preSetMapper.put(messageShape.getTarget().getResourceId(), inIds);	
							} else {
								List<String> inIds = preSetMapper.get(messageShape.getTarget().getResourceId());
								inIds.add(resourceId);
							}							
						}
						postSetMapper.put(resourceId, outIds);
					} else if (stencilId.equals("BPAProcess")) {
						ArrayList<Shape> childEvents = shape.getChildShapes();
						List<String> eventIds = new ArrayList<String>();
						BusinessProcess bp = new BusinessProcess();
						Collections.sort(childEvents, new Comparator<Shape>() {
							@Override
							public int compare(Shape arg0, Shape arg1) {
								return ((Double)arg0.getUpperLeft().getX()).compareTo(arg1.getUpperLeft().getX());
							}
						});
						for (Shape child : childEvents) {
							System.out.println(child.getUpperLeft().getX());
							String childId = child.getResourceId();
							eventIds.add(childId);
						}
						processEventMapper.put(bp,eventIds);
						System.out.println("BP Events: "+eventIds);
					}
					// awkward event creation, TODO: use a factory
					if (stencilId.equals("EndEvent")) {
						// TODO: Label events in signavio?
						Event ev = new EndEvent(0, "blub");
						eventMapper.put(resourceId, ev);
					} else if (stencilId.equals("StartEvent")) {
						StartEvent ev = new StartEvent(0, "start");
						eventMapper.put(resourceId, ev);
					} else if (stencilId.equals("IntermediateCatchingEvent")) {
						IntermediateCatchingEvent ev = new IntermediateCatchingEvent(
								0, "catch");
						eventMapper.put(resourceId, ev);
					} else if (stencilId.equals("IntermediateThrowingEvent")) {
						IntermediateThrowingEvent ev = new IntermediateThrowingEvent(
								0, "throw");
						eventMapper.put(resourceId, ev);
					}
				}

				// now that all events are created, link them
                for (String rid : postSetMapper.keySet()) {
                	System.out.println(rid);
                	Event ev = eventMapper.get(rid);
                	List<ReceivingEvent> postSet = new ArrayList<ReceivingEvent>();
					for (String postId : postSetMapper.get(rid)) {
						System.out.println("eventID: "+rid+" Postset :"+postId);
						postSet.add((ReceivingEvent) eventMapper.get(postId));
					}
					if (ev instanceof SendingEvent) {
						((SendingEvent) ev).setPostset(postSet);
					}
				}
                for (String rid : preSetMapper.keySet()) {
                	System.out.println(rid);
                	Event ev = eventMapper.get(rid);
                	List<SendingEvent> preSet = new ArrayList<SendingEvent>();
					for (String preId : preSetMapper.get(rid)) {
						System.out.println("eventID: "+rid+" Preset :"+preId);
						preSet.add((SendingEvent) eventMapper.get(preId));
					}
					if (ev instanceof ReceivingEvent) {
						((ReceivingEvent) ev).setPreset(preSet);
					}
				}
                for (BusinessProcess bp : processEventMapper.keySet()) {
                	for (String eventId : processEventMapper.get(bp)) {
                		bp.addEvent(eventMapper.get(eventId));	
                	}
				}
                
                
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
	 * @param sid
	 * @return
	 */
	private static boolean isReceiving(String sid) {
		return sid.equals("IntermediateCatchingEvent") || sid.equals("StartEvent");
	}

	/**
	 * @param sid
	 * @return
	 */
	private static boolean isSending(String sid) {
		return sid.equals("IntermediateThrowingEvent") || sid.equals("EndEvent");
	}

	/**
	 * @param sid
	 * @return
	 */
	private static boolean isMTFlow(String sid) {
		return sid.equals("Message") || sid.equals("Trigger");
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
