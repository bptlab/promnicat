package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

import org.jbpt.petri.NetSystem;

public class BPAAnalyzer {

	private static final File workDir = new File(System.getenv("userprofile")
			+ File.separator + ".bpa");
	
	private static final String renewPath;
	//private static final String lolaPath;
	private static final String directoryPath;
	private static final String defaultFileName;
	private static final Properties configuration = new Properties();

	private static File jsonInput;
	
	static // here the configuration is read
	{
		//Properties props = new Properties();
		try {
			configuration.load(new FileReader(new File(workDir,".properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		renewPath = configuration.getProperty("renew.path");
		//lolaPath = configuration.getProperty("lola.path");
		directoryPath = configuration.getProperty("directory.path");
		defaultFileName = configuration.getProperty("default.file");
	}
	
	/**
	 * I glue all the stuff together: Import the json, transform to pnml and
	 * write to file, import with renew, export to net file, call lola,
	 * interpret result.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Boolean found = findInputFile(args);
		if (!found) {
			System.out.println("No input file found, aborting.\n Pass file as argument or configure 'default.file' property.");
		} else {
			// read json file
			BPA bpa = BPAImporter.fromXML(jsonInput);
			
			// transform it
			BPATransformer trans = new BPATransformer();
			NetSystem pns = trans.transform(bpa);
			pns.setName(bpa.getName() != null ? bpa.getName() : "Testnetz");

			// serialize and write to file
			String pnmlNetSerialization = InscriptionSerializer
					.serializeNet(pns);
			File pnmlOutput = writePNML(pnmlNetSerialization);

			// transform to net file
			File netFile = pnmlToNet(pnmlOutput);
			//writeTaskFiles(trans.getDeadProcessFormulae(),"deadProcess");
			//writeTaskFiles(trans.getLivelockFormulae(), "liveTransitions");
			//writeTaskFiles(trans.getTerminatingFormula(), "terminatingRun");
			//File netFile = new File(workDir, "bpa-test.net");

			CorrectnessChecker checker = new CorrectnessChecker(netFile, trans.getAllFormulae().subList(0, 1));
			checker.checkBPA();
			
			//HashMap <String, HashMap<Integer, ArrayList<String>>> completeResults = checker.analyseAllProperties(pnmlOutput.getPath().replaceAll(".pnml", ".net"));
			//DisplayAnalysisResults test = new DisplayAnalysisResults(completeResults);
			//test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

	}

	private static Boolean findInputFile(String[] args) {
		Boolean found = false;
		System.out.println(workDir);
		if (args.length > 0 && args[0] != null) { // filename provided
			jsonInput = new File(args[0]);
			if (jsonInput.exists()) {
				found = true;
			} else { // try relative path
				jsonInput = new File(directoryPath, args[0]);
				if (jsonInput.exists())
					found = true;
			}
		} else {
			jsonInput = new File(workDir, defaultFileName);
			if (jsonInput.exists()) {
				found = true;	
			} else { // deprecated: use default.file in .properties
				jsonInput = new File(workDir, "bpa-test.xml");
				if (jsonInput.exists()) {
					found = true;
				}
			}
		}
		return found;
	}

	private static File pnmlToNet(File pnmlFile) {
		// import/export with renew
		System.out.println("Transforming Petri net");
		String pnmlFilePath = pnmlFile.getAbsolutePath();
		String baseFileName = pnmlFilePath.substring(0, pnmlFilePath.lastIndexOf("."));
		File rnwFile = new File(baseFileName + ".rnw");
		File netFile = new File(baseFileName + ".net");
		Runtime rt = Runtime.getRuntime();
		System.out.println(pnmlFile);
		String importCmd = new String("java -jar  " + renewPath + "loader.jar import " + pnmlFile);
		String exportCmd = new String("java -jar  " + renewPath + "loader.jar ex Lola " + rnwFile);
		try {
			Process renewImportPrc = rt.exec(importCmd);
			BufferedReader renewOutput = new BufferedReader(new InputStreamReader(renewImportPrc.getInputStream()));
			String line = "";
			while ((line = renewOutput.readLine()) != null) {
				System.out.println(line);
			}
			renewImportPrc.waitFor();
			Process renewExportPrc = rt.exec(exportCmd);
			renewOutput = new BufferedReader(new InputStreamReader(renewExportPrc.getInputStream()));
			while ((line = renewOutput.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return netFile;
	}

	private static File writePNML(String pnmlNetSerialization) {
		String baseFileName = jsonInput.getName();
		String pnmlOutput = baseFileName.substring(0, baseFileName.lastIndexOf(".")) + ".pnml";
		File outputFile = new File(workDir, pnmlOutput);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write(pnmlNetSerialization);
			bw.close();
			System.out.println("Transformation complete, written to: "
					+ outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputFile;
	}

}
