package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

import java.util.HashMap;

import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.Event;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.EndEvent;
import org.jbpt.pm.bpmn.Task;

import com.google.gwt.dev.util.collect.HashSet;

import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.PersistenceApiOrientDbObj;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataJbpt;

public class PatternMatcher {

	PersistenceApiOrientDbObj papi;

	public PatternMatcher(PersistenceApiOrientDbObj papi) {
		this.papi = papi;
	}

	public void lookForPatternThree(
			HashMap<ProcessModel, HashSet<ProcessModel>> fragments,
			ProcessModel model, FlowNode gateway, boolean pattern3) {
		ProcessModel fragment = new ProcessModel();
		fragment.setDescription(model.getDescription());
		FlowNode task = model.getDirectPredecessors(gateway).iterator().next();
		task.setDescription("decision task");
		fragment.addFlowNode(task);
		fragment.addFlowNode(gateway);
		BpmnControlFlow<FlowNode> controlFlow = new BpmnControlFlow<FlowNode>(
				fragment, task, gateway);
		controlFlow.setId(((BpmnControlFlow<FlowNode>) model
				.getOutgoingControlFlow(task).iterator().next()).getId());
		pattern3 = checkPatternThreeGateway(model, gateway, pattern3, fragment);

		if (pattern3) {
			if (fragments.get(model) == null)
				fragments.put(model, new HashSet<ProcessModel>());
			for (FlowNode f : fragment.getFlowNodes()) {
				for (ControlFlow<FlowNode> modelControlFlowEdge : model
						.getOutgoingControlFlow(f)) {
					if (!fragment.getFlowNodes().contains(
							modelControlFlowEdge.getTarget())) {
						fragment.addFlowNode(modelControlFlowEdge.getTarget());
						BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
								fragment, f, modelControlFlowEdge.getTarget());
						setCondition(modelControlFlowEdge, conditionalFlow);
						conditionalFlow.setId(modelControlFlowEdge.getId());
//					} else {
//						boolean foundEdge = false;
//						for (ControlFlow<FlowNode> fragmentControlFlowEdge : fragment.getControlFlow()) {
//							if (fragmentControlFlowEdge.getId().equals(modelControlFlowEdge.getId())) {
//								foundEdge = true;
//								break;
//							}
//						}
//						if (!foundEdge && fragment.getFlowNodes().contains(
//								modelControlFlowEdge.getTarget())) {
//							BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
//									fragment, f, modelControlFlowEdge.getTarget());
//							setCondition(modelControlFlowEdge, conditionalFlow);
//							conditionalFlow.setId(modelControlFlowEdge.getId());
//						}
//					}
				}}
			}
			fragments.get(model).add(fragment);
		}

	}

	private void setCondition(ControlFlow<FlowNode> controlFlowEdge,
			BpmnControlFlow<FlowNode> conditionalFlow) {
		if (((BpmnControlFlow<FlowNode>) controlFlowEdge).getCondition() != null
				&& !((BpmnControlFlow<FlowNode>) controlFlowEdge)
						.getCondition().equals("")) {
			conditionalFlow
					.setCondition(((BpmnControlFlow<FlowNode>) controlFlowEdge)
							.getCondition());
		} else if (((BpmnControlFlow<FlowNode>) controlFlowEdge).getName() != null
				&& !((BpmnControlFlow<FlowNode>) controlFlowEdge).getName()
						.equals("")) {
			conditionalFlow
					.setName(((BpmnControlFlow<FlowNode>) controlFlowEdge)
							.getName());
		}
	}

	private boolean checkPatternThreeGateway(ProcessModel model,
			FlowNode gateway, boolean pattern3, ProcessModel fragment) {
		for (ControlFlow<FlowNode> controlFlowEdge : model
				.getOutgoingControlFlow(gateway)) {
			if ((controlFlowEdge.getTarget() instanceof XorGateway || controlFlowEdge
					.getTarget() instanceof OrGateway)
					&& model.getDirectPredecessors(controlFlowEdge.getTarget())
							.size() == 1) {
				fragment.addFlowNode(controlFlowEdge.getTarget());
				BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
						fragment, gateway, controlFlowEdge.getTarget());
				setCondition(controlFlowEdge, conditionalFlow);
				conditionalFlow.setId(controlFlowEdge.getId());
				checkPatternThreeGateway(model, controlFlowEdge.getTarget(),
						pattern3, fragment);
			} else if (controlFlowEdge.getTarget() instanceof Task
					&& model.getDirectSuccessors(controlFlowEdge.getTarget())
							.size() == 1
					&& (model.getDirectSuccessors(controlFlowEdge.getTarget())
							.iterator().next() instanceof XorGateway || model
							.getDirectSuccessors(controlFlowEdge.getTarget())
							.iterator().next() instanceof OrGateway)
					&& model.getDirectPredecessors(
							model.getDirectSuccessors(
									controlFlowEdge.getTarget()).iterator()
									.next()).size() == 1) {
				if (fragment.contains(controlFlowEdge.getTarget()))
					continue;
				pattern3 = true;
				FlowNode nextGateway = model
						.getDirectSuccessors(controlFlowEdge.getTarget())
						.iterator().next();
				fragment.addFlowNode(controlFlowEdge.getTarget());
				controlFlowEdge.getTarget().setDescription("decision task");
				fragment.addFlowNode(nextGateway);
				BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
						fragment, gateway, controlFlowEdge.getTarget());
				setCondition(controlFlowEdge, conditionalFlow);
				conditionalFlow.setId(controlFlowEdge.getId());

				BpmnControlFlow<FlowNode> edgeToNextGateway = new BpmnControlFlow<FlowNode>(
						fragment, controlFlowEdge.getTarget(), nextGateway);
				edgeToNextGateway.setId(((BpmnControlFlow<FlowNode>) model
						.getOutgoingControlFlow(controlFlowEdge.getTarget())
						.iterator().next()).getId());
				checkPatternThreeGateway(model,
						model.getDirectSuccessors(controlFlowEdge.getTarget())
								.iterator().next(), pattern3, fragment);
			}
		}
		return pattern3;
	}

	public void lookForPatternTwo(
			HashMap<ProcessModel, HashSet<ProcessModel>> fragments,
			ProcessModel model, FlowNode gateway, boolean pattern2) {
		ProcessModel fragment = new ProcessModel();
		fragment.setDescription(model.getDescription());
		FlowNode task = model.getDirectPredecessors(gateway).iterator().next();
		task.setDescription("decision task");
		fragment.addFlowNode(task);
		fragment.addFlowNode(gateway);
		BpmnControlFlow<FlowNode> controlFlow = new BpmnControlFlow<FlowNode>(
				fragment, task, gateway);
		controlFlow.setId(((BpmnControlFlow<FlowNode>) model
				.getOutgoingControlFlow(task).iterator().next()).getId());
		pattern2 = checkGateway(model, gateway, pattern2, fragment);

		if (pattern2) {
			if (fragments.get(model) == null)
				fragments.put(model, new HashSet<ProcessModel>());
			for (FlowNode f : fragment.getFlowNodes()) {
				for (ControlFlow<FlowNode> controlFlowEdge : model
						.getOutgoingControlFlow(f)) {
					if (!fragment.getFlowNodes().contains(
							controlFlowEdge.getTarget())) {
						fragment.addFlowNode(controlFlowEdge.getTarget());
						BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
								fragment, f, controlFlowEdge.getTarget());
						setCondition(controlFlowEdge, conditionalFlow);
						conditionalFlow.setId(controlFlowEdge.getId());
					}
				}
			}
			fragments.get(model).add(fragment);
		}
	}

	private boolean checkGateway(ProcessModel model, FlowNode gateway,
			boolean pattern2, ProcessModel fragment) {
		for (ControlFlow<FlowNode> controlFlowEdge : model
				.getOutgoingControlFlow(gateway)) {
			if ((controlFlowEdge.getTarget() instanceof XorGateway || controlFlowEdge
					.getTarget() instanceof OrGateway)
					&& model.getDirectPredecessors(controlFlowEdge.getTarget())
							.size() == 1) {
				pattern2 = true;
				fragment.addFlowNode(controlFlowEdge.getTarget());
				BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
						fragment, gateway, controlFlowEdge.getTarget());
				setCondition(controlFlowEdge, conditionalFlow);
				conditionalFlow.setId(controlFlowEdge.getId());
				checkGateway(model, controlFlowEdge.getTarget(), pattern2,
						fragment);
			}
		}
		return pattern2;
	}

	public boolean lookForDoDont(
			HashMap<ProcessModel, HashSet<ProcessModel>> fragments,
			ProcessModel model, FlowNode gateway, boolean doDont) {

		for (FlowNode successor : model.getDirectSuccessors(gateway)) {
			if (!(successor instanceof org.jbpt.pm.Activity
					&& model.getDirectSuccessors(successor).size() == 1
					&& model.getDirectSuccessors(successor).iterator().next() instanceof XorGateway && model
					.getDirectPredecessors(
							model.getDirectSuccessors(successor).iterator()
									.next()).size() > 1)
					&& !(successor instanceof XorGateway && model
							.getDirectPredecessors(successor).size() > 1)
					&& !(successor instanceof org.jbpt.pm.Event)) {
				doDont = false;
				break;
			}
		}
		return doDont;
	}

	public void lookForPatternOne(
			HashMap<ProcessModel, HashSet<ProcessModel>> fragments,
			ProcessModel model, FlowNode gateway, boolean pattern1) {
		for (FlowNode successor : model.getDirectSuccessors(gateway)) {
			// previous BUG:
			// if (!(successor instanceof org.jbpt.pm.Activity)) {
			if (!(successor instanceof org.jbpt.pm.Activity || successor instanceof EndEvent)
					|| model.getAllPredecessors(gateway).contains(successor)) {
				pattern1 = false;
				break;
			}
		}

		pattern1 = pattern1 || lookForDoDont(fragments, model, gateway, true);

		if (pattern1) {
			if (fragments.get(model) == null)
				fragments.put(model, new HashSet<ProcessModel>());
			ProcessModel fragment = new ProcessModel();
			fragment.setDescription(model.getDescription());
			FlowNode task = model.getDirectPredecessors(gateway).iterator()
					.next();
			task.setDescription("decision task");
			fragment.addFlowNode(task);
			fragment.addFlowNode(gateway);

			BpmnControlFlow<FlowNode> controlFlow = new BpmnControlFlow<FlowNode>(
					fragment, task, gateway);
			controlFlow.setId(((BpmnControlFlow<FlowNode>) model
					.getOutgoingControlFlow(task).iterator().next()).getId());

			for (ControlFlow<FlowNode> controlFlowEdge : model
					.getOutgoingControlFlow(gateway)) {
				if (controlFlowEdge instanceof BpmnControlFlow<?>) {
					BpmnControlFlow<FlowNode> conditionalFlow = new BpmnControlFlow<FlowNode>(
							fragment, gateway, controlFlowEdge.getTarget());
					setCondition(controlFlowEdge, conditionalFlow);
					conditionalFlow.setId(controlFlowEdge.getId());
				}
			}
			fragments.get(model).add(fragment);
		}
	}
}
