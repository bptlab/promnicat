package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;
public class BusinessKnowledgeModel extends DMNode {
	private String label;
	private Decision decision;
	private DecisionTable decisionTable;

	public BusinessKnowledgeModel() {
		this("");
	}
	
	public BusinessKnowledgeModel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public DecisionTable getDecisionTable() {
		return decisionTable;
	}

	public void setDecisionTable(DecisionTable decisionTable) {
		this.decisionTable = decisionTable;
	}

	public Decision getDecision() {
		return decision;
	}

	public void setDecision(Decision decision) {
		this.decision = decision;
	}

}
