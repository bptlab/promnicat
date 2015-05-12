package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.FileCopyUtils;

import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Model;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.PersistenceApiOrientDbObj;


public class JSONAdaptor {

	PersistenceApiOrientDbObj papi;

	public JSONAdaptor(PersistenceApiOrientDbObj papi) {
		this.papi = papi;
	}

	public void adaptJSON(ProcessModel fragment,
			DecisionModel decisionModel) throws JSONException {
		Representation representation = papi.loadRepresentation(fragment.getDescription());		
		String json = null;
		try {
			json = readFromFile(representation.getOriginalFilePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject modelJSON = new JSONObject(json);
		
		FlowNode startNode = findStartNode(fragment);

		ArrayList<FlowNode> endNodes = findEndNodes(fragment);

		for (Rule rule : decisionModel.getTopLevelDecision().getBkm()
				.getDecisionTable().getRules()) {
			FlowNode endNode = rule.getEndNode();
			ArrayList<String> precedingElementsIDs = new ArrayList<>();
			findPathToFirstGateway(fragment, endNode, precedingElementsIDs);

			String newCondition = rule.getConclusion().getValue();

			if (representation.getOriginalFilePath().contains(
					"d664712675074d0d8801075fe8fa4a8d")) {

				JSONObject sequenceFlowObject = findContainingJSONObject(
						modelJSON,
						precedingElementsIDs.get(precedingElementsIDs.size() - 2));

				JSONObject newEdge = cloneJSONObject(sequenceFlowObject, UUID
						.randomUUID().toString());

				setCondition(newCondition, newEdge);

				insertJSONObject(
						modelJSON,
						precedingElementsIDs.get(precedingElementsIDs.size() - 1),
						newEdge, endNode.getId());
			}
		}

		for (FlowNode endNode : endNodes) {
			ArrayList<String> precedingElementsIDs = new ArrayList<>();
			findPathToFirstGateway(fragment, endNode, precedingElementsIDs);
			for (int i = 0; i < precedingElementsIDs.size() - 1; i++) {
				removeJSONObject(modelJSON, precedingElementsIDs.get(i));
			}
		}

		if (representation.getOriginalFilePath().contains(
				"d664712675074d0d8801075fe8fa4a8d")) {
			Model model = representation.getModel().loadCompleteModel(papi);
			int nrOfRevisions = model.getNrOfRevisions();
			String newRepresentationString = modelJSON.toString();			
			File newRepresentationFile = createNewRepresentationFile(representation, nrOfRevisions);
			writeToFile(newRepresentationFile.getAbsolutePath(), newRepresentationString);
//			Representation newRepresentation = new Representation(
//					Constants.FORMAT_BPMAI_JSON, Constants.NOTATION_BPMN2_0, newRepresentationFile);
//			Revision newRevision = new Revision(nrOfRevisions+1);
//			newRepresentation.connectRevision(newRevision);
//			model.connectLatestRevision(newRevision);
//			papi.savePojo(model);
			
			String pathToNewSignavioXMLFile = newRepresentationFile.getAbsolutePath()
					.replace(".json", ".signavio.xml");
			writeToFile(
					pathToNewSignavioXMLFile,
					"<?xml version=\"1.0\" encoding=\"utf-8\"?><oryxmodel><description></description><type>BPMN</type><json-representation><![CDATA["
							+ newRepresentationString
							+ "]]></json-representation></oryxmodel>");
		}
	}
	
	private FlowNode findStartNode(ProcessModel fragment) {
		for (FlowNode node : fragment.getFlowNodes()) {
			if (fragment.getDirectPredecessors(node).isEmpty())
				return node;
		}
		return null;
	}

	private void setCondition(String newCondition, JSONObject sequenceFlowObject) {
		JSONObject propsObject = null;
		try {
			propsObject = sequenceFlowObject.getJSONObject("properties");
			propsObject.put("name", newCondition);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void removeJSONObject(JSONObject modelJSON, String resourceId) {

		JSONObject objectToRemove = findContainingJSONObject(modelJSON,
				resourceId);
		JSONArray containingJSONArray = findContainingJSONArray(modelJSON,
				objectToRemove);
		JSONObject containingJSONObject = findContainingJSONObject(modelJSON,
				containingJSONArray);
		JSONArray newJSONArray = new JSONArray();
		if (containingJSONArray != null) {
			for (int i = 0; i < containingJSONArray.length(); i++) {
				try {
					if (!containingJSONArray.getJSONObject(i).equals(
							objectToRemove)) {
						newJSONArray.put(containingJSONArray.getJSONObject(i));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				containingJSONObject.put("childShapes", newJSONArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private JSONObject findContainingJSONObject(JSONObject modelJSON,
			String resourceId) {
		JSONArray childShapesArray = null;
		JSONObject jsonObject = null;

		if (modelJSON.has("childShapes")) {
			try {
				childShapesArray = modelJSON.getJSONArray("childShapes");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < childShapesArray.length(); i++) {
			JSONObject childShapeObject = null;
			try {
				childShapeObject = childShapesArray.getJSONObject(i);
				if (childShapeObject.get("resourceId").equals(resourceId)) {
					jsonObject = childShapeObject;
				} else {
					jsonObject = findContainingJSONObject(childShapeObject,
							resourceId);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (jsonObject != null)
				return jsonObject;
		}
		return jsonObject;
	}

	private JSONObject findContainingJSONObject(JSONObject modelJSON,
			JSONArray jsonArray) {
		JSONArray childShapesArray = null;
		JSONObject jsonObject = null;

		if (modelJSON.has("childShapes")) {
			try {
				childShapesArray = modelJSON.getJSONArray("childShapes");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (childShapesArray.equals(jsonArray)) {
			jsonObject = modelJSON;
		} else {
			for (int i = 0; i < childShapesArray.length(); i++) {
				JSONObject childShapeObject = null;
				try {
					childShapeObject = childShapesArray.getJSONObject(i);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsonObject = findContainingJSONObject(childShapeObject,
						jsonArray);
				if (jsonObject != null)
					return jsonObject;
			}
		}
		return jsonObject;
	}

	private JSONArray findContainingJSONArray(JSONObject modelJSON,
			JSONObject jsonObject) {
		JSONArray childShapesArray = null;
		JSONArray containingJSONArray = null;

		if (modelJSON.has("childShapes")) {
			try {
				childShapesArray = modelJSON.getJSONArray("childShapes");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < childShapesArray.length(); i++) {
			JSONObject childShapeObject = null;
			try {
				childShapeObject = childShapesArray.getJSONObject(i);
				if (childShapeObject.equals(jsonObject)) {
					containingJSONArray = childShapesArray;
				} else {
					containingJSONArray = findContainingJSONArray(
							childShapeObject, jsonObject);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (containingJSONArray != null)
				return containingJSONArray;
		}
		return containingJSONArray;
	}

	private void insertJSONObject(JSONObject modelJSON, String sourceId,
			JSONObject object, String targetId) {

		JSONArray childShapesArray = null;
		try {
			childShapesArray = modelJSON.getJSONArray("childShapes");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JSONObject sourceObject = findContainingJSONObject(modelJSON, sourceId);

		String objectId = null;
		try {
			objectId = object.getString("resourceId");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		connectSourceToObject(sourceObject, objectId);
		connectObjectToTarget(object, targetId);

		childShapesArray.put(object);

	}

	private void connectSourceToObject(JSONObject sourceObject, String objectId) {
		try {
			JSONArray outgoingArray = null;
			outgoingArray = sourceObject.getJSONArray("outgoing");
			JSONObject newOutgoing = new JSONObject();
			newOutgoing.put("resourceId", objectId);
			outgoingArray.put(newOutgoing);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void connectObjectToTarget(JSONObject object, String targetId) {
		try {
			JSONArray outgoingArray = null;
			outgoingArray = object.getJSONArray("outgoing");
			outgoingArray.getJSONObject(0).put("resourceId", targetId);
			JSONObject targetObject = null;
			targetObject = object.getJSONObject("target");
			targetObject.put("resourceId", targetId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONObject cloneJSONObject(JSONObject objectToClone,
			String newObjectId) {
		JSONObject newObject = null;
		try {
			newObject = new JSONObject(objectToClone.toString());
			newObject.put("resourceId", newObjectId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return newObject;
	}

	private void findAndReplaceStringInJSON(JSONObject modelJSON,
			String string, String replacement) {
		JSONArray childShapesArray = null;
		try {
			childShapesArray = modelJSON.getJSONArray("childShapes");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < childShapesArray.length(); i++) {
			JSONObject childShapeObject = null;
			try {
				childShapeObject = childShapesArray.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (childShapeObject.has("properties")) {
				JSONObject propsObject = null;
				try {
					propsObject = childShapeObject.getJSONObject("properties");
					// Iterator<String> keys = propsObject.keys();
					if (propsObject.get("name").equals(string))
						propsObject.put("name", replacement);
					else {
						findAndReplaceStringInJSON(childShapeObject, string,
								replacement);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void findPathToFirstGateway(ProcessModel fragment,
			FlowNode fragmentNode, ArrayList<String> precedingElementsIDs) {
		if (fragment.getDirectPredecessors(fragmentNode).iterator().next()
				.getDescription() == null
				|| !fragment.getDirectPredecessors(fragmentNode).iterator()
						.next().getDescription().equals("decision task")
				|| fragment.getDirectPredecessors(
						fragment.getDirectPredecessors(fragmentNode).iterator()
								.next()).size() > 0) {
			precedingElementsIDs.add(((BpmnControlFlow<FlowNode>) fragment
					.getIncomingControlFlow(fragmentNode).iterator().next())
					.getId());
			FlowNode predecessor = fragment.getDirectPredecessors(fragmentNode)
					.iterator().next();
			precedingElementsIDs.add(predecessor.getId());
			findPathToFirstGateway(fragment, predecessor, precedingElementsIDs);
		}
	}

	private ArrayList<FlowNode> findEndNodes(ProcessModel fragment) {
		ArrayList<FlowNode> endNodes = new ArrayList<>();
		for (FlowNode node : fragment.getFlowNodes()) {
			if (fragment.getDirectSuccessors(node).isEmpty())
				endNodes.add(node);
		}
		return endNodes;
	}

	private static String readFromFile(String filepath) throws Exception {
		try {
			return FileCopyUtils.copyToString(new InputStreamReader(
					new FileInputStream(filepath), "UTF-8"));
		} catch (Exception e) {
			throw new Exception("Could not initialize modeldata for "
					+ filepath, e);
		}
	}

	private static void writeToFile(String filepath, String content) {
		try {
			FileCopyUtils.copy(content, new OutputStreamWriter(
					new FileOutputStream(filepath)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private File createNewRepresentationFile(Representation representation, int revision) {
    	revision++;
    	String pathToNewFile = representation.getOriginalFilePath().
    			replace("rev"+representation.getRevisionNumber()+".json", "rev"+revision+".json");
    	File fDest = new File(pathToNewFile);
    	try {
			fDest.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fDest;
	}

}

// // TODO maybe deriveRules() and compare with decisionModel
// for (FlowNode fragmentNode : fragment.getFlowNodes()) {
// if (fragment.getDirectSuccessors(fragmentNode).isEmpty()) {
// for (FlowNode adaptedFragmentNode : adaptedFragment
// .getFlowNodes()) {
// if ((fragmentNode.getName() == null && adaptedFragmentNode
// .getName() == null)
// || (fragmentNode.getName() != null && fragmentNode
// .getName().equals(
// adaptedFragmentNode.getName()))
// && adaptedFragment.getDirectSuccessors(
// adaptedFragmentNode).isEmpty()) {
// ArrayList<String> precedingElementsIDs = new ArrayList<>();
// findPathToFirstGateway(fragment, fragmentNode,
// precedingElementsIDs);
// String condition = ((BpmnControlFlow<FlowNode>) fragment
// .getIncomingControlFlow(fragmentNode)
// .iterator().next()).getName();
// String newCondition = ((BpmnControlFlow<FlowNode>) adaptedFragment
// .getIncomingControlFlow(adaptedFragmentNode)
// .iterator().next()).getName();
// if (representation.getOriginalFilePath().contains(
// "df0a3e82029e489e8062af0740fdb8c6")) {
// findAndReplaceStringInJSON(childShapeObject,
// condition, newCondition);
// }
// }
// }
// }
// }

// if (childShapeObject.has("outgoing")) {
// // JSONArray outgoingArray = null;
// // outgoingArray = childShapeObject
// // .getJSONArray("outgoing");
// // outgoingArray.getJSONObject(0).put("resourceId",
// // targetNodeID);
// // JSONObject targetObject = null;
// // targetObject =
// // childShapeObject.getJSONObject("target");
// // targetObject.put("resourceId", targetNodeID);

// if (representation
// .getOriginalFilePath()
// .contains(
// "df0a3e82029e489e8062af0740fdb8c6")
// && condition.equals("nein")) {
//
// connectToOtherActivity(modelJSON,
// precedingElementsIDs
// .get(precedingElementsIDs
// .size() - 1),
// precedingElementsIDs
// .get(precedingElementsIDs
// .size() - 2),
// "sid-172B34FC-EDC1-4927-AE45-ABC6329E5033");
// }
//
// if (representation
// .getOriginalFilePath()
// .contains(
// "df0a3e82029e489e8062af0740fdb8c6")) {
// findAndReplaceStringInJSON(modelJSON,
// condition, newCondition);
// }
