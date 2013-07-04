/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.ConnectedEPC;
import de.uni_potsdam.hpi.bpt.promnicat.bpa.Event.EventType;
import java.util.*;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
/**
 * @author rami.eidsabbagh
 *
 */
public class test {
	
	private final static Logger logger = Logger.getLogger(test.class.getName());
	/**
	 * @param args
	 */
	private static final File workDir = new File(System.getenv("userprofile")
			+ File.separator + ".bpa");
	public static void main(String[] args) throws Exception {
		String param = workDir+File.separator+"bpa-test-deadlock.net";
		CorrectnessChecker checker = new CorrectnessChecker();
		//ArrayList<String> resultss = checker.checkDeadlock(param);
		//System.out.println(resultss);
		
		//String param3 = workDir+File.separator+"terminatingRun1.task";
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> deadProResult = new ArrayList<String>();
		 ArrayList<String> liveTransResults = new ArrayList<String>();
		 ArrayList<String> terminRunResults = new ArrayList<String>();
		String taskfile = "";
		File[] files = workDir.listFiles();
		for (File file : files) {
			if(file.isDirectory()&& file.getName().contains("deadProcess")){
				System.out.println(checker.checkForDeadProcesses(param, file));
				//				File[] deadProcessTasks = file.listFiles();
//				for (File file2 : deadProcessTasks) {
//				 taskfile = file2.getPath();
//				 results = checker.checkModel(param, taskfile);
//				 deadProResult.add(results.get(2));
//				}
			}
			if(file.isDirectory()&& file.getName().contains("liveTransition")){
				System.out.println(checker.checkForLivenessTransitions(param, file));
				//				File[] liveTransition = file.listFiles();
//				for (File file2 : liveTransition) {
//				 taskfile = file2.getPath();
//				 results = checker.checkModel(param, taskfile);
//				
//				liveTransResults.add(results.get(2));
//				}
			}
			if(file.getName().contains("terminatingRun")){
				taskfile =  file.getPath();
				results = checker.checkModel(param, taskfile);				
				terminRunResults.add(results.get(2));
			}
			
		}
		//results = checker.checkModel(param, param3);
		//System.out.println(results);
		//System.out.println(liveTransResults);
		//System.out.println(deadProResult);
		//System.out.println(terminRunResults);
//		String param2 = "-a";
////		String param3 = workDir+File.separator+"deadProcess1.task";
//		String param3 = workDir+File.separator+"liveTransitions1.task";
//		//String param2 = "";
//		System.out.println(param+" param 2 "+param2+" param3: "+param3);
//		StartLola lolaStart = new StartLola();
//		try {
//			String list = lolaStart.runLola(param,param2,param3);
//			System.out.println("This is the return of start lola: "+list);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		/*Relation relation = Relation.getInstance();
		ArrayList<StartEvent> triggse3 = new ArrayList<StartEvent>();
		int[] mult = new int[]{1,2,3,4};
		final  StartEvent e0 = new StartEvent(0, 2, "p", new int[]{1});
		final SendingEvent e1 = new SendingEvent(1,2,"q", new int[]{1,2} );
		
		final  StartEvent e2 = new StartEvent(3, 4, "r",new int[]{3,4} );
		final SendingEvent e3 = new SendingEvent(5,4,"s", new int[]{1});
		final SendingEvent e4 = new SendingEvent(6, 4, "t", new int[]{1});

		final  StartEvent e5 = new StartEvent(7, 9, "u", new int[]{1});
		final  StartEvent e6 = new StartEvent(8,9,"v", new int[]{1} );
		final SendingEvent e9 = new SendingEvent(13,9,"z", new int[]{1} );
		
		final  StartEvent e7 = new StartEvent(10, 12, "x", new int[]{1});
		final SendingEvent e8 = new SendingEvent(11,12,"y", new int[]{1} );
		
		triggse3.add(e0);
		triggse3.add(e5);
		triggse3.add(e2);
		relation.addTrigger(e3, triggse3);
		ArrayList<StartEvent> results = relation.getTriggered(e3);
		logger.info("this is in results"+results.size());
		logger.info("This is the arraylist: ");
		
		ListIterator<StartEvent> litr = results.listIterator();
	    while (litr.hasNext()) {
	    //String	label = litr.next().getLabel();
	     //logger.info("Even Labels is: "+label);
	     logger.info("Direkt output: "+litr.next().getLabel());
	    }
		
		
		// TODO Auto-generated method stub
		StartEvent start = new StartEvent(15, 26, "Startevent", mult);
		if(StartEvent.EventType.STARTEVENT.equals(start.getType())){
			logger.info("the startevent :"+start.getLabel());
			logger.info("event id: "+start.getID());
			logger.info("event id: "+start.getType());
			logger.info("All kinds of getters: "+start.getOwner());
			logger.info("All kinds of getters II: "+start.getClass());
			logger.info("All kinds of getters III: "+start.getMultiplicity());
			logger.info("All kinds of getters III: "+start.hasTrivialMultiplicity());
			start.setLabel("newstartlablel");
			logger.info("new label "+start.getLabel());
		} else{
			logger.info("It did not work "+start.getType());
			logger.info("It did not work:"+start.getLabel());
		}
	*/
	}
}
