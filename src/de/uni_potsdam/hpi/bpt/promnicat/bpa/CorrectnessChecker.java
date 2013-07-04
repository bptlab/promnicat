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

	private static final File workDir = new File(System.getenv("userprofile")
			+ File.separator + ".bpa");
	private static String renewPath;
	private static String lolaPath;

	
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
	public CorrectnessChecker() {
		
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
	
	public HashMap<Integer, String> checkForLivenessTransitions(String param, File directoryPath) throws Exception{
		File[] liveTransitionTasks = directoryPath.listFiles();
		HashMap<Integer, String> liveTransitions = new HashMap<Integer, String>();
		for (File file : liveTransitionTasks) {
		 String taskfile = file.getPath();
		 ArrayList<String> results = checkModel(param, taskfile);
		 String line = readTaskContent(taskfile);
		 String[] lines =  line.split(" ");
		 if(lines[1].contains("intern")){
		 liveTransitions.put(Integer.parseInt(results.get(2)),lines[1].substring(7));
		 }else{
			 liveTransitions.put(Integer.parseInt(results.get(2)),lines[1]);
		 }
		}
		return  liveTransitions;
	}
	
	public HashMap<Integer, String> checkForDeadProcesses(String param, File directoryPath) throws Exception{
		File[] deadProcessTasks = directoryPath.listFiles();
		HashMap<Integer, String> deadProcesses = new HashMap<Integer, String>();
		for (File file : deadProcessTasks) {
		 String taskfile = file.getPath();
		 ArrayList<String> results = checkModel(param, taskfile);
		 String line = readTaskContent(taskfile);
		 String[] lines =  line.split(" ");
		 if(lines[1].contains("intern")){
			 System.out.println(lines[1].substring(7));
			 deadProcesses.put(Integer.parseInt(results.get(2)),lines[1].substring(7));
		 }else{
		 deadProcesses.put(Integer.parseInt(results.get(2)),lines[1]);
		 }
		}
		return  deadProcesses;
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
