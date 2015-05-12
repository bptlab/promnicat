package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jbpt.pm.FlowNode;

public class Rule {
	private HashMap<String,String> conditions;
	private Entry<String,String> conclusion;
	private FlowNode endNode;

	public Rule() {
		conditions = new HashMap<String,String>();
	}
	
	public Rule(HashMap<String, String> conditions) {
		this.conditions = conditions;
	}
	
	public Entry<String, String> getConclusion() {
		return conclusion;
	}

	public void setConclusion(Entry<String, String> conclusion) {
		this.conclusion = conclusion;
	}

	public HashMap<String, String> getConditions() {
		return conditions;
	}

	public void setConditions(HashMap<String, String> conditions) {
		this.conditions = conditions;
	}
	
	public FlowNode getEndNode() {
		return endNode;
	}

	public void setEndNode(FlowNode endNode) {
		this.endNode = endNode;
	}


	public String toString() {
		String rule = "";

		for (String condition : conditions.values()) {
			rule += condition;

		}
		rule += "->" + conclusion.getValue();
		rule += "(" + endNode.getName() + ")";
		return rule;
	}
}
