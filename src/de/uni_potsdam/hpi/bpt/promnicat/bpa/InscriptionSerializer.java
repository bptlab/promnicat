package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.throwable.SerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InscriptionSerializer extends PNMLSerializer {
	
	/**
	 * Cannot override static private method {@link PNMLSerializer.serializePetriNet}.
	 * Hence we use it to build the {@link Document} and then add an additional 
	 * inscription-element to those arcs, which have an Integer value as tag.
	 * The resulting XML is returned as String.
	 * @see {@link Flow.getTag()} 
	 * @param net
	 * @return
	 */
	public static String serializeNet(PetriNet net) {
		Map<String,Flow> needInscription = new HashMap<String,Flow>();
		for (Flow flow : net.getEdges()) {
			if (flow.getTag() instanceof Integer) {
				needInscription.put(flow.getId(),flow);
			}
		}
		Document doc = null;
		String docXML = null;
		try {
			// not passing LOLA switch, no final markings required 
			doc = PNMLSerializer.serialize((NetSystem) net);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
		if (doc == null) {
			return "Document is null";
		}
		// inspect arcs, check if they need inscription 
		NodeList arcs = doc.getElementsByTagName("arc");
		for (int i = 0; i < arcs.getLength(); i++) {
			Node arc = arcs.item(i);
			String id = arc.getAttributes().getNamedItem("id").getNodeValue();
			if (needInscription.containsKey(id)) {
				Element inscription = doc.createElement("inscription");
				Element text = doc.createElement("text");
				text.setTextContent(needInscription.get(id).getTag().toString());
				inscription.appendChild(text);
				arc.appendChild(inscription);
				//System.out.println("Added inscription to node " + arc);
			}
		}
		DOMSource domSource = new DOMSource(doc);
		StreamResult streamResult = new StreamResult(new StringWriter());
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer;
		try {
			serializer = tf.newTransformer();
			serializer.transform(domSource, streamResult);
			//docXML = streamResult.toString();
			docXML =  ((StringWriter) streamResult.getWriter()).getBuffer().toString();
			
		} catch (TransformerException e) {
			e.printStackTrace();
				//throw new SerializationException(e.getMessage());
		}	
		return  docXML;
	}
}
