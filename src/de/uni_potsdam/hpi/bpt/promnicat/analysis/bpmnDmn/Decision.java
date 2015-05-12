package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

public class Decision extends DMNode {

	private String label;
	private BusinessKnowledgeModel bkm;

	public Decision() {
		this("");
	}

	public Decision(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BusinessKnowledgeModel getBkm() {
		return bkm;
	}

	public void setBkm(BusinessKnowledgeModel bm) {
		this.bkm = bm;
	}


}
