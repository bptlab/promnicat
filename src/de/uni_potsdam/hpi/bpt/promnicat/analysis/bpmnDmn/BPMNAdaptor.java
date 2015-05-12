package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.bpmn.BpmnControlFlow;

public class BPMNAdaptor {

	public ProcessModel adaptFragments(ProcessModel fragment,
			DecisionModel decisionModel) {

		FlowNode startNode = findStartNode(fragment);
		Gateway gateway = (Gateway) fragment.getDirectSuccessors(startNode)
				.iterator().next();
		ProcessModel adpatedFragment = new ProcessModel();
		adpatedFragment.addFlowNode(startNode);
		new BpmnControlFlow<FlowNode>(adpatedFragment, startNode, gateway);

		for (Rule rule : decisionModel.getTopLevelDecision().getBkm()
				.getDecisionTable().getRules()) {
			FlowNode endNode = rule.getEndNode();
			BpmnControlFlow<FlowNode> edge = new BpmnControlFlow<FlowNode>(
					adpatedFragment, gateway, endNode);
			edge.setCondition(rule.getConclusion().getValue());
			edge.setName(rule.getConclusion().getValue());
		}
		return adpatedFragment;
	}

	private FlowNode findStartNode(ProcessModel fragment) {
		for (FlowNode node : fragment.getFlowNodes()) {
			if (fragment.getDirectPredecessors(node).isEmpty())
				return node;
		}
		return null;
	}

}
