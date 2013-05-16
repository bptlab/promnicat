/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
import de.uni_potsdam.hpi.bpt.ai.diagram.DiagramBuilder;
import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;

/**
 * Import a BPA modeled with Signavio and exported as XML.
 * 
 * @author Marcin.Hewelt
 * 
 */
public class BPAImporter {

	public static void main(String[] args) {

		Path jsonPath = Paths.get(System.getenv("userprofile") + File.separator
				+ "test.xml");
		fromXML(jsonPath.toFile());
	}

	public static BPA fromXML(File toImport) {
		BPA bpa = null;
		try {
			// DOMBuilder in-memory, ok because DOM is small
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(toImport);
			NodeList json = doc.getElementsByTagName("json-representation");
			if (json.getLength() == 0) {
				throw new DOMException(DOMException.NOT_FOUND_ERR,
						"No json-representation found in XML file " + toImport);
			}
			org.w3c.dom.Node first = json.item(0);
			JSONObject jsonBPA = new JSONObject(first.getTextContent());

			// let bpmai parse json to diagram
			Diagram diag = DiagramBuilder.parseJson(jsonBPA);
			List<Shape> shapes = diag.getShapes();
			Map<String, Event> eventMapper = new HashMap<String, Event>();
			Map<String, List<String>> postSetMapper = new HashMap<String, List<String>>();
			Map<String, List<String>> preSetMapper = new HashMap<String, List<String>>();
			Map<BusinessProcess, List<String>> processEventMapper = new HashMap<BusinessProcess, List<String>>();
			Relation relation = new Relation();
			for (Shape shape : shapes) {
				String shapeType = shape.getStencilId();
				String resourceId = shape.getResourceId();
				String name = shape.getProperty("name");
				if (isSending(shapeType) || isReceiving(shapeType)) {
					int[] multiplicity = BPAImporter.convertMultiplicity(shape.getProperty("multiplicity"));
					for (int i = 0; i < multiplicity.length; i++) {
						System.out.println(multiplicity[i]);
					}
					
					Event ev = createEvent(shapeType, name);
					ev.setMultiplicity(multiplicity);
					eventMapper.put(resourceId, ev);
				}
				if (BPAImporter.isSending(shapeType)) {
					List<Shape> out = shape.getOutgoings();
					List<String> outIds = new ArrayList<String>();
					for (Shape messageShape : out) {
						// Puts Target Event Id into ArrayList
						outIds.add(messageShape.getTarget().getResourceId());
						if (null == preSetMapper.get(messageShape.getTarget()
								.getResourceId())) {
							List<String> inIds = new ArrayList<String>();
							inIds.add(resourceId);
							preSetMapper.put(messageShape.getTarget()
									.getResourceId(), inIds);
						} else {
							List<String> inIds = preSetMapper.get(messageShape
									.getTarget().getResourceId());
							inIds.add(resourceId);
						}
					}
					postSetMapper.put(resourceId, outIds);
				} else if (shapeType.equals("BPAProcess")) {
					ArrayList<Shape> childEvents = shape.getChildShapes();
					List<String> eventIds = new ArrayList<String>();
					BusinessProcess bp = new BusinessProcess(resourceId);
					Collections.sort(childEvents, new Comparator<Shape>() {
						@Override
						public int compare(Shape arg0, Shape arg1) {
							return ((Double) arg0.getUpperLeft().getX())
									.compareTo(arg1.getUpperLeft().getX());
						}
					});
					for (Shape child : childEvents) {
//						System.out.println(child.getUpperLeft().getX());
						String childId = child.getResourceId();
						eventIds.add(childId);
					}
					processEventMapper.put(bp, eventIds);
					System.out.println("BP Events: " + eventIds);
				}
			}

			// now that all events are created, link them
			for (String rid : postSetMapper.keySet()) {
				System.out.println(rid);
				Event ev = eventMapper.get(rid);
				List<ReceivingEvent> postSet = new ArrayList<ReceivingEvent>();
				for (String postId : postSetMapper.get(rid)) {
					System.out.println("eventID: " + rid + " Postset :"
							+ postId);
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
					System.out.println("eventID: " + rid + " Preset :" + preId);
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
			bpa = new BPA();
			bpa.setProcesslist(new ArrayList<BusinessProcess>(processEventMapper.keySet()));
			
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
		return bpa;
	}

	private static Event createEvent(String shapeType, String name) {
		Event result = null;
		if (shapeType.equals("EndEvent")) {
			result = new EndEvent(name);
		} else if (shapeType.equals("StartEvent")) {
			result = new StartEvent(name);
		} else if (shapeType.equals("IntermediateCatchingEvent")) {
			result = new IntermediateCatchingEvent(name);
		} else if (shapeType.equals("IntermediateThrowingEvent")) {
			result = new IntermediateThrowingEvent(name);
		}
		return result;
	}
	/**
	 * @param sid
	 * @return
	 */
	private static boolean isReceiving(String sid) {
		return sid.equals("IntermediateCatchingEvent")
				|| sid.equals("StartEvent");
	}

	/**
	 * @param sid
	 * @return
	 */
	private static boolean isSending(String sid) {
		return sid.equals("IntermediateThrowingEvent")
				|| sid.equals("EndEvent");
	}

	/**
	 * @param sid
	 * @return
	 */
	private static boolean isMTFlow(String sid) {
		return sid.equals("Message") || sid.equals("Trigger");
	}

	private static int[] convertMultiplicity(String multiplicity) {
		if (multiplicity != null && !multiplicity.isEmpty()) {
			
			String[] multArray  = multiplicity.replaceAll("\\s","").split(",");
			     
			      
			int[] intMultiplicity = new int[multArray.length];
			for (int i = 0; i < multArray.length; i++) {
				
				intMultiplicity[i] = Integer.parseInt(multArray[i]);
				System.out.println("multarray: "+multArray[i]+" int mult: "+intMultiplicity[i]);
			}
			return intMultiplicity;
		} else {
			return new int[] { 1 };
		}
	}

}
