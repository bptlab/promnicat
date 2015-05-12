package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;
import java.util.ArrayList;

public class DecisionTable {
	private ArrayList<Rule> rules;
	private String label;
	private HitPolicy policy;

	public DecisionTable() {
		rules = new ArrayList<Rule>();
	}
	
	public void addRule(Rule rule) {
		rules.add(rule);
	}
	
	public ArrayList<Rule> getRules() {
		return rules;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isEmpty() {
		if (rules.isEmpty())
			return true;
		return false;
	}
	
	public HitPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(HitPolicy policy) {
		this.policy = policy;
	}
}
