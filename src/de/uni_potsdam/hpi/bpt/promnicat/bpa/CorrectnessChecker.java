/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.lang.ProcessBuilder;

import weka.classifiers.bayes.net.search.fixed.FromFile;

/**
 * @author rami.eidsabbagh
 *
 */
public class CorrectnessChecker {

	private final static File workDir = new File(System.getenv("userprofile")
			+ File.separator + ".bpa");; 
	private final static String renewPath;
	private final static String lolaPath;
	private final static Properties configuration = new Properties();
	private static final String LIVELOCK_FILENAME = "liveTransition";
	private static final String DEADPROCESS_FILENAME = "deadProcess";
	private static final String TERMINATION_FILENAME = "terminatingRun";
	
	static {
		try {
			configuration.load(new FileReader(new File(workDir,".properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		renewPath = configuration.getProperty("renew.path");
		lolaPath = configuration.getProperty("lola.path");	
	}
	
	/**
	 * The lola net file this checker will check.
	 */
	private final File toCheck;
	private final List<Formula> formulae;
	
	Enum<CheckerType> lola;
	private ArrayList<String> params = new ArrayList<String>();
	private int liveLockCounter = 0;
	private int deadProcessCounter = 0;
	private int terminationCounter = 0;
	private Runtime rt;
	
	public enum CheckerType{
		LOLA("lola.exe"),
		LOLABOUNDEDNET("lola-bounded-net.exe"),
		LOLABOUNDEDPLACE("lola-bounded-place.exe"),
		LOLADEADLOCK("lola-deadlock.exe"),
		LOLADEADTRANSITION("lola-dead-transition.exe"),
		LOLAHOMESTATE("lola-home-state.exe"),
		LOLALIVEPROP("lola-liveprop.exe"),
		LOLAMODELCHECKING("lola-model-checking.exe"),
		LOLAREACHMARK("lola-reach-mark.exe"),
		LOLASTATEPREDICTE("lola-state-predicate.exe");
		
		private CheckerType(String lolacomp) {
			this.lolacomp = lolacomp;
			}
		private final String lolacomp;
		public String toString() {
			return lolacomp;
		}
		
	}
	
	/**
	 * Create a new CorrctnessChecker to check a given net file.
	 * @param netFile, the {@link File} to check.
	 */
	public CorrectnessChecker(File netFile, List<Formula> formulae) {
		toCheck = netFile;
		this.formulae = formulae;
		rt = Runtime.getRuntime(); 
	}
	
	public static void main(String[] args) throws Exception {
		File netFile = new File(workDir, "simpleNet.net");
		Formula f = new Formula("FORMULA p >= 1", CorrectnessCriteria.NoLiveLocks);
		CorrectnessChecker checker = new CorrectnessChecker(netFile, Arrays.asList(f));
		File taskFile = checker.writeTaskFile(f);
		List<String> result =  checker.checkModel(netFile.getAbsolutePath(), taskFile.toString());
		//List<String> result = checker.checkFormula(f);
		for (String string : result) {
			System.out.println(string);
		}
	}
	
	
	
	public HashMap<Integer, ArrayList<String>> checkForLivenessTransitions(String param, File directoryPath) throws Exception{
		File[] liveTransitionTasks = directoryPath.listFiles();
		HashMap<Integer, ArrayList<String>> liveTransitions = new HashMap<Integer, ArrayList<String>>();
		for (File file : liveTransitionTasks) {
		 String taskfile = file.getPath();
		 ArrayList<String> results = checkModel(param, taskfile);
		 String line = readTaskContent(taskfile);
		 String[] lines =  line.split(" ");
		 if(lines[1].contains("intern")){
			 			 
			 if (lines.length > 5) {
				 addStringToArrayList(liveTransitions, Integer.parseInt(results.get(2)),lines[1].substring(7)+" "+lines[5]);
				 System.out.println("Live Transition if >5: "+lines[1].substring(7)+" "+lines[5]);
			}else {
			 addStringToArrayList(liveTransitions, Integer.parseInt(results.get(2)), lines[1].substring(7));
		 //liveTransitions.put(Integer.parseInt(results.get(2)),lines[1].substring(7));
			}
		 }else{
			 if (lines.length > 5) {
				 addStringToArrayList(liveTransitions, Integer.parseInt(results.get(2)),lines[1]+" "+lines[5]);
			}else {
			 addStringToArrayList(liveTransitions, Integer.parseInt(results.get(2)), lines[1]);
			 //liveTransitions.put(Integer.parseInt(results.get(2)),lines[1]);
			}
		 }
		}
		return  liveTransitions;
	}
	
	public HashMap<Integer, ArrayList<String>> checkForDeadProcesses(String param, File directoryPath) throws Exception{
		File[] deadProcessTasks = directoryPath.listFiles();
		HashMap<Integer,ArrayList<String>> deadProcesses = new HashMap<Integer, ArrayList<String>>();
		for (File file : deadProcessTasks) {
		 String taskfile = file.getPath();
		 ArrayList<String> results = checkModel(param, taskfile);
		 String line = readTaskContent(taskfile);
		 String[] lines =  line.split(" ");
		 if(lines[1].contains("intern")){
			 //deadProcesses.put(Integer.parseInt(results.get(2)),lines[1].substring(7));
			 addStringToArrayList(deadProcesses, Integer.parseInt(results.get(2)), lines[1].substring(7));
			 System.out.println("Dead Process if"+lines[1].substring(7)+" "+lines[5]);
		 }else{
			 addStringToArrayList(deadProcesses, Integer.parseInt(results.get(2)), lines[1]);
			 //deadProcesses.put(Integer.parseInt(results.get(2)),lines[1]);
		 }
		}
		return  deadProcesses;
	}
	
	
	public HashMap<Integer, ArrayList<String>> addStringToArrayList(HashMap<Integer, ArrayList<String>> hashMap, int returncode, String place)
	{
	    if(!hashMap.containsKey(returncode)) // no key found?
	    {
	    ArrayList<String> lx = new ArrayList<String>();
	    lx.add(place);
	    hashMap.put(returncode, lx);
	    }
	    else // the key pre-exists so we just add the (new) value to the existing arraylist
	    {
	    ArrayList<String> lx = hashMap.get(returncode)    ;
	    lx.add(place);
	    hashMap.put(returncode, lx);
	    }
	    return hashMap;
	}
	
	
	public String readTaskContent(String filepath) throws IOException{
		
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(filepath);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  String result =  "";
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
			  result = strLine;
			  }
			  //Close the input stream
			  in.close();
			  System.out.println (result);
			  return result;
	  }
		
	
	
	/**
	 * @param pathtofile
	 * @return
	 * @throws Exception
	 * Takes the name of the file
	 */
	public ArrayList<String> checkDeadlock(String pathtofile)throws Exception{
		
		lola = CheckerType.LOLADEADLOCK;
		System.out.println(lola.toString());
		params.clear();
		params.add(lolaPath+lola.toString());
		params.add(pathtofile);
		params.add("-m");
		//params.add("deadlock-graph");
		params.add("-P");
		//params.add("deadlock-path");
		Process process = createProcess(params);
		return getOutput(process);
		
	}
	
	/**
	 * @param pathtofile
	 * @param pathtotask
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> checkModel(String pathtofile, String pathtotask) throws Exception{
		lola = CheckerType.LOLAMODELCHECKING;
		//lola = CheckerType.LOLALIVEPROP;
		System.out.println(lola.toString());
		params.clear();
		params.add(lolaPath+lola.toString());
		params.add(pathtofile);
		params.add("-P");
		params.add("-a");
		params.add(pathtotask);
		Process process = createProcess(params);
		return getOutput(process);
	}
	
	
	private List<String> checkModel(CheckerType whichLola, List<String> additionalParams, Formula formula) {
		System.out.println("Checking formula: " + formula.getContent());
		StringBuilder cmd = new StringBuilder();
		//params.clear();
		cmd.append(lolaPath);
		cmd.append(whichLola.toString());
		cmd.append(" ");
		cmd.append(toCheck.getAbsolutePath());
		cmd.append(" -a ");
		cmd.append(formula.getFilepath().getAbsolutePath());
		//cmd.append(additionalParams);
		System.out.println(cmd);
		List<String> result = new ArrayList<String>();
		try {
			Process process = rt.exec(cmd.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			result.add("Result: " + process.waitFor());
			while ((line=br.readLine()) != null) {
				result.add(line);
			}
			
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Process proc = createProcess(params); 		
		return result;
	}



	public HashMap <String, HashMap<Integer, ArrayList<String>>> analyseAllProperties(String param) throws Exception{
		HashMap <String, HashMap<Integer, ArrayList<String>>> completeResults = new HashMap<String, HashMap<Integer,ArrayList<String>>>();
		HashMap<Integer, ArrayList<String>> deadProResult = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, ArrayList<String>> liveTransResults = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, ArrayList<String>> directResults = new HashMap<Integer, ArrayList<String>>();
		ArrayList<String> terminRunResults = new ArrayList<String>();
		ArrayList<String> results = new ArrayList<String>();
		String taskfile = "";
		
		File[] files = workDir.listFiles();
		for (File file : files) {
			if(file.isDirectory()&& file.getName().contains("deadProcess")){
			deadProResult =	checkForDeadProcesses(param, file);
				
			}
			if(file.isDirectory()&& file.getName().contains("liveTransition")){
			liveTransResults = checkForLivenessTransitions(param, file);
				
			}
			if(file.getName().contains("terminatingRun")){
				taskfile =  file.getPath();
				terminRunResults = checkModel(param, taskfile);				
				//terminRunResults.add(results.get(2));
			}
			
		}
		
		
		
		results = checkDeadlock(param);
		// Print out Result for Deadlock
		if(results.get(2).equals("0")){
			System.out.println(param+" has a deadlock");
		}
		else if(results.get(2).equals("1")){
			System.out.println(param+" has no deadlock");
			System.out.println();
		}else{
			System.out.println("Some error occurred: Return code: "+results.get(2)+" Standard Output: "+results.get(0)+" Error Output: "+results.get(1));
		}
		
		
		// Print out Results for LiveTransitions
		if (liveTransResults.get(0)!= null) {
			System.out.println(liveTransResults.get(0).size()+" Transitions are live. Their input places are :"+liveTransResults.get(0));	
		}
		if (liveTransResults.get(1)!= null) {
			System.out.println(liveTransResults.get(1).size()+" Transitions are not live. Their input places are :"+liveTransResults.get(1));	
		}
		
		//Print Out Results for DeadProcesses
		if (deadProResult.get(0)!= null) {
			System.out.println(deadProResult.get(0).size()+" Processes are dead. Their input places are: "+deadProResult.get(0));	
		}
		if (deadProResult.get(1)!= null) {
			System.out.println(deadProResult.get(1).size()+" Processes are not dead. Their input places are: "+deadProResult.get(1));	
		}
		
		//Print Out Results for Terminating Run
		if (terminRunResults.get(2).equals("0")) {
			System.out.println(param+" has a terminting run");
		}else if (terminRunResults.get(2).equals("1")) {
			System.out.println(param+" has no terminating run");
			System.out.println("Standard Output: "+terminRunResults.get(0)+" Error Output: "+terminRunResults.get(1));
		}else{
			System.out.println("The terminating run check caused an error. Return code: "+terminRunResults.get(2)+" Standard Output: "+terminRunResults.get(0)+" Error Output: "+terminRunResults.get(1));
		}
		
		//Collect all results and return for further processing
		directResults.put(0, results);
		directResults.put(1, terminRunResults);
		
		completeResults.put("live", liveTransResults);
		completeResults.put("dead", deadProResult);
		completeResults.put("deadlock0-terminating1",directResults);
		return completeResults;
		//return completeAnalysis;
	}
	
	/**
	 * @param pathtofile
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> runLolaFull(String pathtofile,ArrayList<String> params) throws Exception{
		lola = CheckerType.LOLA;
		params.add(0, lolaPath+lola.toString());
		params.add(1,pathtofile);
		Process process = createProcess(params);
		return getOutput(process);
	}
	
	
	
	private Process createProcess(ArrayList<String> params) {
		ProcessBuilder builder = new ProcessBuilder(params);
		Process process = null;
		try {
			process = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return process;
	}
	
	/**
	 * Get the output produced by LoLA as a list of Strings.
	 * @param process
	 * @return
	 */
	private ArrayList<String> getOutput(Process process) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			String output = loadStream(process.getInputStream());
			String error  = loadStream(process.getErrorStream());
			int rc = process.waitFor();
			results.add(output);
			results.add(error);
			results.add("Result: " + rc);
		} catch (Exception e) {
			System.out.println("Failed to get results for process " + process);
			e.printStackTrace();
		}
        return results;
	}
	
	/**
	 * Read a stream into a String.
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private static String loadStream(InputStream s) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=br.readLine()) != null)
            sb.append(line).append("\n");
        return sb.toString();
    }
	
	/**
	 * Checks a given {@link Formula}. If the formula has not been
	 * written to file yet, this is done by {@link CorrectnessChecker.writeTaskFile()}.
	 * @param formula
	 * @return the output from LoLA, as a list of Strings
	 */
	public List<String> checkFormula(Formula formula) {
		if (! formula.hasFile()) { // write to file
			File taskFile = writeTaskFile(formula);
			if (taskFile != null) 
				formula.setFilepath(taskFile);
		}
		CheckerType whichLola;
		List<String> additionalParams = new ArrayList<String>();
		switch (formula.getType()) {
		case Termination:
			whichLola = CheckerType.LOLAMODELCHECKING;
			additionalParams.add("-P");
			break;
		case NoDeadProcesses:
			whichLola = CheckerType.LOLAREACHMARK;
			break;
		case NoLiveLocks:
			whichLola = CheckerType.LOLALIVEPROP;
			break;
		default: 
			whichLola = CheckerType.LOLA;
		}
		return checkModel(whichLola, additionalParams, formula);
	}


	/** 
	 *  Writes the task file for the given formula. Does not set the 
	 *  filepath field of the formula.
	 *  @param a formula
	 *  @return the written task file
	 */
	private File writeTaskFile(Formula formula) {
		StringBuilder taskFileName = new StringBuilder();
		switch (formula.getType()) {
		case Termination:
			terminationCounter++;
			taskFileName.append(TERMINATION_FILENAME);
			taskFileName.append(terminationCounter);
			break;
		case NoDeadProcesses:
			deadProcessCounter++;
			taskFileName.append(DEADPROCESS_FILENAME);
			taskFileName.append(deadProcessCounter);
			break;
		case NoLiveLocks:
			liveLockCounter++;
			taskFileName.append(LIVELOCK_FILENAME);
			taskFileName.append(liveLockCounter);
			break;
		}
		taskFileName.append(".task");
		File taskFile = new File(workDir, taskFileName.toString());
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(taskFile));
			bw.write(formula.getContent());
			bw.close();
			System.out.println("Task file " + taskFile
					+ " successfully written.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return taskFile;
	}
//		File liveTransitionDir = new File(workDir +File.separator + "liveTransitions");
//		boolean exists = liveTransitionDir.exists();
//		if (!exists) {
//			liveTransitionDir.mkdir();
//		} else { // It returns true if File or directory exists
//			System.out.println("the file or directory you are searching does exist : "
//							+ exists);
//		}
//		File deadProcessDir = new File(workDir + File.separator+"deadProcess");
//		exists = deadProcessDir.exists();
//		if (!exists) {
//			deadProcessDir.mkdir();
//		} else { // It returns true if File or directory exists
//			System.out.println("the file or directory you are searching does exist : "
//							+ exists);
//		}
//		int i = 1;
//		File taskFile;
//		 for (String formula : formulae) {
//			if(type.equals("liveTransitions")) {
//				taskFile = new File(liveTransitionDir.getPath(), type + i + ".task");
//			} else if(type.equals("deadProcess")){
//				taskFile = new File(deadProcessDir.getPath(), type + i + ".task");
//			} else {
//				taskFile = new File(workDir, type + i + ".task");
//			}
//			try {
//				BufferedWriter bw = new BufferedWriter(new FileWriter(taskFile));
//				bw.write(formula);
//				bw.close();
//				i++;
//				System.out.println("Task file " + taskFile
//						+ " successfully written.");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}



	public void checkBPA() {
		List<String> result = new ArrayList<String>();
		for (Formula formula : formulae) {
			 result = checkFormula(formula);
			 for (String string : result) {
				 System.out.println(string);
			 }
		}
	}
}
