package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

public class InformationRequirementEdge extends Edge {
	private DMNode from;
	private Decision to;
	public InformationRequirementEdge(DMNode from, Decision to) {
		super();
		this.from = from;
		this.to = to;
	}
	public DMNode getFrom() {
		return from;
	}
	public void setFrom(DMNode from) {
		this.from = from;
	}
	public Decision getTo() {
		return to;
	}
	public void setTo(Decision to) {
		this.to = to;
	}

}
