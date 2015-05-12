package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

public class KnowledgeRequirementEdge extends Edge {
	private BusinessKnowledgeModel from;
	private Decision to;
	public KnowledgeRequirementEdge(BusinessKnowledgeModel from, Decision to) {
		super();
		this.from = from;
		this.to = to;
	}
	public BusinessKnowledgeModel getFrom() {
		return from;
	}
	public void setFrom(BusinessKnowledgeModel from) {
		this.from = from;
	}
	public Decision getTo() {
		return to;
	}
	public void setTo(Decision to) {
		this.to = to;
	}

}
