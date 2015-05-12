package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;
import java.util.ArrayList;
import java.util.HashMap;


public class DecisionModel {
	private ArrayList<DMNode> nodes;
	private ArrayList<Label> labels;
	private ArrayList<Edge> edges;
	private ArrayList<DecisionTable> tables;
	private HashMap<DMNode,Label> theta;
	private HashMap<BusinessKnowledgeModel, DecisionTable> tau;
	
	public DecisionModel() {
		nodes = new ArrayList<>();
		labels = new ArrayList<>();
		edges = new ArrayList<>();
		tables = new ArrayList<>();
		theta = new HashMap<>();
		tau = new HashMap<>();
	}

	public ArrayList<DMNode> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<DMNode> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<Label> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<Label> labels) {
		this.labels = labels;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}

	public ArrayList<DecisionTable> getTables() {
		return tables;
	}

	public void setTables(ArrayList<DecisionTable> tables) {
		this.tables = tables;
	}

	public HashMap<DMNode, Label> getTheta() {
		return theta;
	}

	public void setTheta(HashMap<DMNode, Label> theta) {
		this.theta = theta;
	}

	public HashMap<BusinessKnowledgeModel, DecisionTable> getTau() {
		return tau;
	}

	public void setTau(HashMap<BusinessKnowledgeModel, DecisionTable> tau) {
		this.tau = tau;
	}
	
	public Decision getTopLevelDecision() {
		Decision decision = null;
		for (Edge edge : edges) {
			if (edge instanceof InformationRequirementEdge) {
				decision = findTopLevelDecision(((InformationRequirementEdge) edge).getTo());
			}
		}
		if (decision == null) {
			for (DMNode node : nodes) {
				if (node instanceof Decision)
					return (Decision) node;
			}
		}
		return decision;
	}
	
	private Decision findTopLevelDecision(Decision decision) {
		for (Edge edge : edges) {
			if (edge instanceof InformationRequirementEdge && ((InformationRequirementEdge) edge).getFrom().equals(decision)) {
				return findTopLevelDecision(((InformationRequirementEdge) edge).getTo());
			}
		}
		return decision;
	}
}	
