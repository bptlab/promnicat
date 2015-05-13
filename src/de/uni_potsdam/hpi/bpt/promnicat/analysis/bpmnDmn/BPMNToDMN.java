package de.uni_potsdam.hpi.bpt.promnicat.analysis.bpmnDmn;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.NonFlowNode;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.FileCopyUtils;

import sun.org.mozilla.javascript.internal.json.JsonParser;

import com.google.gwt.dev.util.collect.HashSet;

import de.hpi.bpmn2_0.model.activity.Activity;
import de.uni_potsdam.hpi.bpt.ai.collection.BPMAIExport;
import de.uni_potsdam.hpi.bpt.ai.collection.BPMAIExportBuilder;
import de.uni_potsdam.hpi.bpt.ai.collection.Revision;
import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
import de.uni_potsdam.hpi.bpt.ai.diagram.DiagramBuilder;
import de.uni_potsdam.hpi.bpt.ai.diagram.JSONBuilder;
import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;
import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.clustering.Clustering;
import de.uni_potsdam.hpi.bpt.promnicat.importer.bpmai.BpmaiImporter;
import de.uni_potsdam.hpi.bpt.promnicat.parser.Bpmn2_0Constants;
import de.uni_potsdam.hpi.bpt.promnicat.parser.BpmnParser;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Model;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.PersistenceApiOrientDbObj;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChain;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.UnitChainBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataLabelFilter;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitData;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataFeatureVector;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataJbpt;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataProcessMetrics;
import de.uni_potsdam.hpi.bpt.ai.collection.BPMAIExportBuilder;
import de.uni_potsdam.hpi.bpt.ai.collection.impl.RevisionImpl;

public class BPMNToDMN {

	private final static Logger logger = Logger.getLogger(Clustering.class
			.getName());
	static PersistenceApiOrientDbObj papi;

	public static void main(String[] args) throws IllegalTypeException,
			IOException, JSONException, ParseException {
		IUnitChainBuilder chainBuilder = new UnitChainBuilder("",
				Constants.DATABASE_TYPES.ORIENT_DB, UnitDataJbpt.class);
		buildUpUnitChain(chainBuilder);

		papi = PersistenceApiOrientDbObj
				.getInstance("configuration.properties");

		logger.info(chainBuilder.getChain().toString());
		Collection<UnitDataJbpt<Object>> result = (Collection<UnitDataJbpt<Object>>) chainBuilder
				.getChain().execute();

		// String pathToSgxArchive =
		// "C:\\Users\\kimon.batoulis\\promnicat\\resources\\BPMAI";
		// File rootDir = new File(pathToSgxArchive);
		// File container = new File(rootDir + File.separator + "dummy");
		// container.mkdir();
		// // search for sgx-archives and unzip them
		// extractAvailableSgxArchives(rootDir, container);

		// parse directory

		// BpmaiImporter importer = new BpmaiImporter();
		// BpmnParser bpmnParser = new BpmnParser(new Bpmn2_0Constants(),
		// false);
		// ArrayList<ProcessModel> processModels = new ArrayList<>();
		//
		// BPMAIExport directoryWalker = BPMAIExportBuilder
		// .parseDirectory(rootDir);
		// for (de.uni_potsdam.hpi.bpt.ai.collection.Model bpmAiModel :
		// directoryWalker
		// .getModels()) {
		// Model model = importer.parseModel(bpmAiModel);
		// for (Representation representation : model.getLatestRevision()
		// .getRepresentations()) {
		// if (representation.getFormat().equals(
		// Constants.FORMAT_BPMAI_JSON)) {
		// String json = readFile(representation.getOriginalFilePath());
		// Diagram diagram = DiagramBuilder.parseJson(json);
		// ProcessModel pm = bpmnParser.transformProcess(diagram);
		// if (pm != null) {
		// pm.setDescription(representation.getOriginalFilePath());
		// processModels.add(pm);
		// }
		// }
		// }
		// }

		ArrayList<UnitDataJbpt<Object>> filteredResult = new ArrayList<>();
		for (UnitDataJbpt<Object> diagramResult : result) {
			if ((diagramResult.getDbId() != null)
					&& (diagramResult.getProcessModel() != null)) {
				filteredResult.add(diagramResult);
			}
		}

		HashMap<ProcessModel, HashSet<ProcessModel>> pattern1Fragments = new HashMap<ProcessModel, HashSet<ProcessModel>>();
		HashMap<ProcessModel, HashSet<ProcessModel>> pattern2Fragments = new HashMap<ProcessModel, HashSet<ProcessModel>>();
		HashMap<ProcessModel, HashSet<ProcessModel>> pattern3Fragments = new HashMap<ProcessModel, HashSet<ProcessModel>>();

		PatternMatcher pm = new PatternMatcher(papi);
		// System.out.println(filteredResult.size());
		for (UnitDataJbpt<Object> diagramResult : filteredResult) {
			ProcessModel model = diagramResult.getProcessModel();
			model.setDescription(diagramResult.getDbId());
			// for (ProcessModel model : processModels) {
			for (FlowNode gateway : model.getFlowNodes()) {
				boolean pattern1 = true;
				boolean pattern2 = false;
				boolean pattern3 = false;
				if (gateway instanceof XorGateway
						|| gateway instanceof OrGateway) {
					if (model.getDirectPredecessors(gateway).size() == 1) {
						FlowNode task = model.getDirectPredecessors(gateway)
								.iterator().next();
						if (task instanceof Task) {
							pm.lookForPatternOne(pattern1Fragments, model,
									gateway, pattern1);
							pm.lookForPatternTwo(pattern2Fragments, model,
									gateway, pattern2);
							pm.lookForPatternThree(pattern3Fragments, model,
									gateway, pattern3);
						}
					}
				}
			}
		}

		System.out.println(pattern1Fragments.size()
				/ (double) filteredResult.size());
		System.out.println(pattern2Fragments.size()
				/ (double) filteredResult.size());
		System.out.println(pattern3Fragments.size()
				/ (double) filteredResult.size());

		HashMap<ProcessModel, DecisionModel> pattern1fragmentsToDM = new HashMap<>();
		HashMap<ProcessModel, DecisionModel> pattern2fragmentsToDM = new HashMap<>();
		HashMap<ProcessModel, DecisionModel> pattern3fragmentsToDM = new HashMap<>();

		// derive DRD
		deriveDM(pattern1Fragments, pattern1fragmentsToDM);
		deriveDM(pattern2Fragments, pattern2fragmentsToDM);
		deriveDM(pattern3Fragments, pattern3fragmentsToDM);

		HashMap<ProcessModel, ProcessModel> pattern1fragmentsToAdaptation = new HashMap<>();
		HashMap<ProcessModel, ProcessModel> pattern2fragmentsToAdaptation = new HashMap<>();
		HashMap<ProcessModel, ProcessModel> pattern3fragmentsToAdaptation = new HashMap<>();

		adaptFragment(pattern1fragmentsToDM, pattern1fragmentsToAdaptation);
		adaptFragment(pattern2fragmentsToDM, pattern2fragmentsToAdaptation);
		adaptFragment(pattern3fragmentsToDM, pattern3fragmentsToAdaptation);

		// adaptJSON(pattern1fragmentsToDM);
		// adaptJSON(pattern2fragmentsToDM);
		adaptJSON(pattern3fragmentsToDM);
	}

	private static void adaptJSON(
			HashMap<ProcessModel, DecisionModel> patternFragmentsToDM) {
		JSONAdaptor jsonAdaptor = new JSONAdaptor(papi);
		for (ProcessModel fragment : patternFragmentsToDM.keySet()) {
			try {
				jsonAdaptor.adaptJSON(fragment,
						patternFragmentsToDM.get(fragment));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private static void adaptFragment(
			HashMap<ProcessModel, DecisionModel> patternFragmentsToDM,
			HashMap<ProcessModel, ProcessModel> patternFragmentsToAdaptation) {
		BPMNAdaptor adaptor = new BPMNAdaptor();
		for (ProcessModel fragment : patternFragmentsToDM.keySet()) {
			patternFragmentsToAdaptation.put(
					fragment,
					adaptor.adaptFragments(fragment,
							patternFragmentsToDM.get(fragment)));
		}
	}

	private static void deriveDM(
			HashMap<ProcessModel, HashSet<ProcessModel>> patternFragments,
			HashMap<ProcessModel, DecisionModel> patternfragmentsToDM) {
		DMNBuilder db = new DMNBuilder();
		for (ProcessModel model : patternFragments.keySet()) {
			for (ProcessModel fragment : patternFragments.get(model)) {
				patternfragmentsToDM.put(fragment, db.buildDMN(fragment));
			}
		}
	}

	private static void extractAvailableSgxArchives(File rootDir,
			File dummyFolder) throws ZipException, IOException {
		for (File file : rootDir.listFiles()) {
			if ((!file.isDirectory()) && (file.getName().endsWith(".sgx"))) {
				ZipFile zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				// iterate through files of an zip archive
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					String entryName = entry.getName();
					if (entryName.contains("/")) {
						// ignore meta data files
						if (entryName.endsWith("_meta.json")) {
							continue;
						}
						// remove directory folder to fit into expected
						// structure
						String[] pathParts = entryName.split("/");
						if (entryName.contains("directory_")) {
							entryName = "";
							for (int i = 0; i < pathParts.length; i++) {
								if (!(pathParts[i].startsWith("directory_"))) {
									entryName = entryName.concat(pathParts[i]
											+ "/");
								}
							}
							entryName = entryName.substring(0,
									entryName.length() - 1);
						}
						// rename process model files
						String oldModelName = pathParts[pathParts.length - 1];
						String[] nameParts = oldModelName.split("_");
						if (nameParts.length > 2) {
							String modelName = pathParts[pathParts.length - 2]
									.split("_")[1]
									+ "_rev"
									+ nameParts[1]
									+ nameParts[2];
							entryName = entryName.replace(oldModelName,
									modelName);
						}
						// create directories
						(new File(dummyFolder.getPath()
								+ File.separatorChar
								+ entryName.substring(0,
										entryName.lastIndexOf("/")))).mkdirs();
					}
					// extract process model
					copyInputStream(zipFile.getInputStream(entry),
							dummyFolder.getPath() + File.separatorChar
									+ entryName);
				}
				zipFile.close();
			}
		}
	}

	private static void copyInputStream(InputStream in, String targetPath)
			throws IOException {
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
				new FileOutputStream(targetPath));
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			bufferedOutputStream.write(buffer, 0, len);
		}
		in.close();
		bufferedOutputStream.close();
	}

	private static void buildUpUnitChain(IUnitChainBuilder chainBuilder)
			throws IllegalTypeException {
		// build db query
		chainBuilder.addDbFilterConfig(createDbFilterConfig());
		chainBuilder.createBpmaiJsonToJbptUnit(true);

		// chainBuilder.createProcessModelMetricsCalulatorUnit();
		// chainBuilder.createModelToFeatureVectorUnit(createMetricsConfig());
		// chainBuilder.createElementExtractorUnit(XorGateway.class);
		chainBuilder.createProcessModelFilterUnit(XorGateway.class);
		// chainBuilder.createElementExtractorUnit(OrGateway.class);

		// collect results
		chainBuilder.createSimpleCollectorUnit();
	}

	private static DbFilterConfig createDbFilterConfig() {
		DbFilterConfig dbFilter = new DbFilterConfig();
		// dbFilter.addOrigin(Constants.ORIGINS.BPMAI);
		dbFilter.addFormat(Constants.FORMATS.BPMAI_JSON);
		dbFilter.addNotation(Constants.NOTATIONS.BPMN2_0);
		return dbFilter;
	}
}
