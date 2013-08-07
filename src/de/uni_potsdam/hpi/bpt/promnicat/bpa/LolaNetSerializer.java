package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpt.petri.Flow;
import org.jbpt.petri.Marking;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;

public class LolaNetSerializer {
	
	private static final String LF = System.lineSeparator();
	private static final String PRODUCE = "PRODUCE" + LF;
	private static final String CONSUME =  LF + "CONSUME" + LF;
	private static final String TRANSITION = "TRANSITION ";
	private static final String SEMICOLON_NEWLINE = ";" + LF;
	private static final String COLON = ": ";
	private static final String COMMA_NEWLINE = "," + LF;
	private static final int PRE = 0;
	private static final int POST = 1;

	public static String serialize(NetSystem pn) {
		StringBuilder sb = new StringBuilder();
		sb.append("PLACE");
		sb.append(LF);
		boolean first = true;
		for (Place p : pn.getPlaces()) {
			if (first) {
				first = false;
			} else {
				sb.append(COMMA_NEWLINE);
			}
			sb.append(p.getLabel());
		}
		sb.append(SEMICOLON_NEWLINE);
		
		sb.append(LF);
		sb.append("MARKING");
		sb.append(LF);
		first = true;
		Marking marking = pn.getMarking();
		for (Place p : marking.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(COMMA_NEWLINE);
			}
			sb.append(p);
			sb.append(COLON);
			sb.append(marking.get(p));		
		}
		sb.append(SEMICOLON_NEWLINE);
		
		List<Flow> pre = new ArrayList<Flow>();
		List<Flow> post = new ArrayList<Flow>();
		StringBuilder tmp = new StringBuilder();
		Collection<Transition> trans = pn.getTransitions();
		System.out.println("Collection of transitions has size :" + trans.size());
		for (Transition t : pn.getTransitions()) {
			System.out.println("Transition label :"+t.getLabel());
			sb.append(TRANSITION);
			sb.append(t.getLabel());
			Collection<Flow> edges = pn.getEdges(t);
			for (Flow flow : edges) {
				if (flow.getSource().equals(t)) {
					System.out.println("t is source");
					post.add(flow);
				} else if (flow.getTarget().equals(t)) {
					System.out.println("t is target");
					pre.add(flow);
				} else {
					System.out.println("neither source nor target");
				}
			}
			sb.append(CONSUME);
			sb.append(prePost(pre,tmp, PRE));
			tmp.setLength(0);
			sb.append(PRODUCE);
			sb.append(prePost(post,tmp, POST));
			tmp.setLength(0);
			pre.clear();
			post.clear();
		}
		return sb.toString();
	}

	private static int getWeight(Flow f) {
		int weight = 1;
		if (f.getTag() instanceof Integer) {
			weight = ((Integer) f.getTag()).intValue();
		}
		return weight;
	}
	
	private static String prePost(Collection<Flow> flows, StringBuilder prePostBuilder, int preOrPost) {
		boolean first = true;
		for (Flow f : flows) {
//			Flow edge = pn.getEdge(n, t);
			if (first) {
				first = false;
			} else {
				prePostBuilder.append(COMMA_NEWLINE);
			}
			if (preOrPost == PRE) {
				prePostBuilder.append(f.getSource().getLabel());
			} else {
				prePostBuilder.append(f.getTarget().getLabel());
			}
			prePostBuilder.append(COLON);
			prePostBuilder.append(getWeight(f));
		}
		prePostBuilder.append(SEMICOLON_NEWLINE);
		return prePostBuilder.toString();
	}
	
	/*
	 * Test
	 */
	public static void main(String[] args) {
		BPATransformer trans = new BPATransformer();
		NetSystem net = trans.transform(BPAExamples.simpleBPA());
		//System.out.println(LolaNetSerializer.serialize(net));
		File file = new File(System.getenv("userprofile") + "/.bpa", "test.net");
		FileWriter fw;
		try {
			fw = new FileWriter(file);
			fw.write(LolaNetSerializer.serialize(net));
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
