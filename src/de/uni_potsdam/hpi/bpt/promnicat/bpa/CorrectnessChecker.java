/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

/**
 * @author rami.eidsabbagh
 *
 */
/**
 * @author rami.eidsabbagh
 *
 */
public class CorrectnessChecker {

	private static File workDir; 
	private static String renewPath;
	private static String lolaPath;
	//File workDir;
	
	//String lolaPath ="C:\\renew-2.3\\plugins\\lola-2.4_0.8.4\\lib\\";
	Enum<CheckerType> lola;
	ArrayList<String> params = new ArrayList<String>();
	
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
	 * @param lola
	 * @param params
	 * 
	 */
	public CorrectnessChecker(File directory) {
		workDir = directory;
		init();
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
	
	
	
	private Process createProcess(ArrayList<String> params)throws Exception{
		ProcessBuilder builder = new ProcessBuilder(params);
		 Process process = builder.start();
		return process;
	}
	
	private ArrayList<String> getOutput(Process process)throws Exception{
		ArrayList<String> results = new ArrayList<String>();
		String output;
		output = loadStream(process.getInputStream());
		String error  = loadStream(process.getErrorStream());
        int rc = process.waitFor();
        
        results.add(output);
        results.add(error);
        results.add(new Integer(rc).toString());
        return results;
		
	}
	
	private static String loadStream(InputStream s) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=br.readLine()) != null)
            sb.append(line).append("\n");
        return sb.toString();
    }
}
