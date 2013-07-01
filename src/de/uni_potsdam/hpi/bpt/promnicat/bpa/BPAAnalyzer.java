package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.jbpt.petri.NetSystem;

public class BPAAnalyzer {

	private static final File workDir = new File(System.getenv("userprofile")
			+ File.separator + ".bpa");
	// TODO: introduce properties
	private static String renewPath;
	private static String lolaPath;

	/**
	 * I glue all the stuff together: Import the json, transform to pnml and
	 * write to file, import with renew, export to net file, call lola,
	 * interpret result.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		File jsonInput;
		Boolean found = false;
		if (args.length > 0 && args[0] != null) { // filename provided
			jsonInput = new File(args[0]);
			if (jsonInput.exists()) {
				found = true;
			} else { // try relative path
				jsonInput = new File(System.getProperty("user.dir"), args[0]);
				if (jsonInput.exists())
					found = true;
			}
		} else {
			jsonInput = new File(workDir, "bpa-test.xml");
			if (jsonInput.exists())
				found = true;
		}
		if (!found) {
			System.out.println("File " + args[0] + " not found. Aborting.");
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
			writeTaskFiles(trans.getDeadProcessFormulae(),"deadProcess");
			writeTaskFiles(trans.getLivelockFormulae(), "liveTransitions");
			writeTaskFiles(trans.getTerminatingFormula(), "terminatingRun");
			pnmlToNet(pnmlOutput);
		}

	}

	private static void init() {
		Properties props = new Properties();
		try {
			props.load(new FileReader(new File(workDir,".properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		renewPath = props.getProperty("renew.path");
		lolaPath = props.getProperty("lola.path");
	}

	private static void pnmlToNet(File pnmlOutput) {
		// import/export with renew
		System.out.println("Transforming Petri net");
		Runtime rt = Runtime.getRuntime();
		String importCmd = new String("java -jar  " + renewPath + "loader.jar import " + pnmlOutput);
		String exportCmd = new String("java -jar  " + renewPath + "loader.jar ex Lola " + workDir + "/bpa-test.rnw");
		try {
			Process renewImportPrc = rt.exec(importCmd);
			BufferedReader renewOutput = new BufferedReader(new InputStreamReader(renewImportPrc.getInputStream()));
			String line = "";
			while ((line = renewOutput.readLine()) != null) {
				System.out.println(line);
			}
			Process renewExportPrc = rt.exec(exportCmd);
			renewOutput = new BufferedReader(new InputStreamReader(renewExportPrc.getInputStream()));
			while ((line = renewOutput.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeTaskFiles(List<String> formulae, String type) {
		// writing task files to be checked by lola
		int i = 1;
		File taskFile;
		for (String formula : formulae) {
			taskFile = new File(workDir, type + i + ".task");
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(taskFile));
				bw.write(formula);
				bw.close();
				i++;
				System.out.println("Task file " + taskFile
						+ " successfully written.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static File writePNML(String pnmlNetSerialization) {
		File outputFile = new File(workDir, "bpa-test.pnml"); // TODO: use real
																// names
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