package de.uni_potsdam.hpi.bpt.promnicat.processModelAlignment;

import org.jbpt.hypergraph.abs.IEntity;
import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.processModelAlignment.label.similarity.matrix.ISimilarityMatrix;

public interface Alignment<Entity extends IEntity> {
	/** The given processes are aligned to one another, i.e. their similarity is calculated */
	public ISimilarityMatrix<Entity> align(ProcessModel process1, ProcessModel process2);
}
