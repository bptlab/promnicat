package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

public class InputData extends DMNode {
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public InputData(String label) {
		super();
		this.label = label;
	}
}
