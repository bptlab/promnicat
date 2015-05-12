package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.Task;

public class DMNBuilder {
	
	HashMap<Task, Decision> rho = null;
	HashMap<Decision, Task> rho2 = null;
	int outputValue = 65;
	DecisionModel dm = null;
	
	public DMNBuilder() {
		
	}

	public DecisionModel buildDMN(ProcessModel fragment) {
//		System.out.println(fragment.getDescription());
		outputValue = 65;
		dm = new DecisionModel();
		rho = new HashMap<Task, Decision>();
		rho2 = new HashMap<>();
		for (FlowNode node : fragment.getFlowNodes()) {
			if (node.getDescription() != null
					&& node.getDescription().equals("decision task")
					&& fragment.getDirectSuccessors(node).size() > 0) {
				Task decisionTask = (Task) node;
				Decision d = new Decision(decisionTask.getLabel());
				Label dLabel = new Label(decisionTask.getLabel());
				dm.getNodes().add(d);
				dm.getLabels().add(dLabel);
				dm.getTheta().put(d, dLabel);
				rho.put((Task) decisionTask, d);
				rho2.put(d, (Task) decisionTask);
				BusinessKnowledgeModel bkm = new BusinessKnowledgeModel(
						decisionTask.getName() + " table");
				Label bkmLabel = new Label(decisionTask.getName()
						+ " table");
				d.setBkm(bkm);
				bkm.setDecision(d);
				bkm.setDecisionTable(new DecisionTable());
				bkm.getDecisionTable().setLabel(bkm.getLabel());
				dm.getNodes().add(bkm);
				dm.getLabels().add(bkmLabel);
				dm.getTheta().put(bkm, bkmLabel);
				// TODO incluce data objects
				KnowledgeRequirementEdge kr = new KnowledgeRequirementEdge(
						bkm, d);
				dm.getEdges().add(kr);
			}
		}
		for (FlowNode node : fragment.getFlowNodes()) {
			if (node.getDescription() != null
					&& node.getDescription().equals("decision task")
					&& fragment.getDirectSuccessors(node).size() > 0) {
				findKnowledgeRequirements(fragment, rho.get(node),
						fragment.getDirectSuccessors(node).iterator()
								.next());
			}
		}
		for (DMNode node : dm.getNodes()) {
			if (node instanceof Decision) {
				deriveDecisionTable(fragment, (Decision) node);
			}
		}
		return dm;
	}
	
	private void deriveDecisionTable(ProcessModel fragment, Decision d) {
		for (Edge edge : dm.getEdges()) {
			if (edge instanceof InformationRequirementEdge) {
				if (((InformationRequirementEdge) edge).getTo().equals(d)
						&& ((InformationRequirementEdge) edge).getFrom() instanceof Decision)
					deriveDecisionTable(fragment,
							(Decision) ((InformationRequirementEdge) edge)
									.getFrom());
			}
		}
		if (d.getBkm().getDecisionTable().isEmpty()) {
			DecisionTable table = new DecisionTable();
			Rule rule = new Rule();
			table.setLabel(rho2.get(d).getName());
			deriveRules(fragment,
					(Gateway) fragment.getDirectSuccessors(rho2.get(d))
							.iterator().next(), rule, table);
			d.getBkm().setDecisionTable(table);
			dm.getTables().add(table);
//			System.out.println(rho2.get(d));
//			for (Rule rule2 : table.getRules())
//				System.out.println(rule2);
//			System.out.println("----");
		}
	}

	// TODO additional param ArrayList<String> inputs?
	private void deriveRules(ProcessModel fragment, Gateway gateway,
			Rule rule, DecisionTable table) {

		for (ControlFlow<FlowNode> edgeToSuccessor : fragment
				.getOutgoingControlFlow(gateway)) {
			if (((BpmnControlFlow<FlowNode>) edgeToSuccessor).getCondition() != null
					&& !((BpmnControlFlow<FlowNode>) edgeToSuccessor)
							.getCondition().equals("")) {
				rule.getConditions().put(
						gateway.getName(),
						((BpmnControlFlow<FlowNode>) edgeToSuccessor)
								.getCondition());
			} else if (((BpmnControlFlow<FlowNode>) edgeToSuccessor).getName() != null
					&& !((BpmnControlFlow<FlowNode>) edgeToSuccessor).getName()
							.equals("")) {
				rule.getConditions()
						.put(gateway.getName(),
								((BpmnControlFlow<FlowNode>) edgeToSuccessor)
										.getName());
			}
			FlowNode successor = edgeToSuccessor.getTarget();
			while ((successor.getDescription() == null || !successor
					.getDescription().equals("decision task"))
					&& successor instanceof org.jbpt.pm.Activity
					&& fragment.getDirectSuccessors(successor).size() > 0) {
				successor = fragment.getDirectSuccessors(successor).iterator()
						.next();
			}
			if (successor instanceof Gateway) {
				deriveRules(fragment, (Gateway) successor, new Rule(
						new HashMap<>(rule.getConditions())), table);
			} else if (successor.getDescription() != null
					&& successor.getDescription().equals("decision task")
					&& fragment.getDirectSuccessors(successor).size() > 0
					&& !rho.get(successor).getBkm().getDecisionTable()
							.isEmpty()) {
				for (Rule rule2 : rho.get(successor).getBkm()
						.getDecisionTable().getRules()) {
					Rule rule3 = new Rule(new HashMap<>(rule.getConditions()));
					rule3.getConditions().put(rule2.getConclusion().getKey(),
							rule2.getConclusion().getValue());
					Map.Entry<String, String> conclusion = new AbstractMap.SimpleEntry<String, String>(
							table.getLabel() + " output",
							Character.toString((char) outputValue++));
					rule3.setConclusion(conclusion);
					rule3.setEndNode(rule2.getEndNode());
					table.addRule(rule3);
				}
			} else {
				Map.Entry<String, String> conclusion = new AbstractMap.SimpleEntry<String, String>(
						table.getLabel() + " output",
						Character.toString((char) outputValue++));
				rule.setConclusion(conclusion);
				rule.setEndNode(successor);
				table.addRule(rule);
			}
			rule = new Rule(new HashMap<>(rule.getConditions()));
		}
	}

	private void findKnowledgeRequirements(ProcessModel fragment,
			Decision d, FlowNode node) {
		if (node.getDescription() != null
				&& node.getDescription().equals("decision task") && fragment.getDirectSuccessors(node).size() > 0) {
			InformationRequirementEdge ir = new InformationRequirementEdge(
					rho.get(node), d);
			dm.getEdges().add(ir);
		} else if (node instanceof Gateway) {
			for (FlowNode successor : fragment.getDirectSuccessors(node)) {
				if (!(successor instanceof Gateway))
					findKnowledgeRequirements(fragment, d, successor);
			}
		}
	}
}
