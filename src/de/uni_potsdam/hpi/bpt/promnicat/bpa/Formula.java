package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.File;

/**
 * Represents a task to be checked by LoLA 
 * @author Marcin.Hewelt
 */
public class Formula {
	private final String content;
	private Boolean hasFile = Boolean.FALSE;
	private File filepath;
	private final CorrectnessCriteria type;
	private String id;
	
	public Formula(String content, CorrectnessCriteria type) {
		this.content = content;
		this.type = type;
	}
	
	public Formula(String content, CorrectnessCriteria type, String processId) {
		this.content = content;
		this.type = type;
		this.id = processId;
	}
	
	public String getid() {
		return id;
	}
	
	public String getContent() {
		return content;
	}

	public Boolean hasFile() {
		return hasFile;
	}

	public File getFilepath() {
		return filepath;
	}
	
	protected void setFilepath(File filepath) {
		if (filepath.exists()) {
			this.filepath = filepath;
			hasFile = Boolean.TRUE;
		}
	}

	protected CorrectnessCriteria getType() {
		return type;
	}
}
