/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.lang.ProcessBuilder;
/**
 * @author rami.eidsabbagh
 *
 */
public class StartLola {
	String path ="C:\\renew-2.3\\plugins\\lola-2.2-SE_0.7.1\\lib\\lola.exe";
	List<String> myList;
	public List<String> runLola(String param) throws IOException{
		Process process = new ProcessBuilder(path).start();
		//Process process = new ProcessBuilder(path,param).start();
		//InputStream is = process.getInputStream();
		//InputStreamReader isr = new InputStreamReader(is);
		//BufferedReader br = new BufferedReader(isr);
		//String line;

		//System.out.printf("Output of running %s is:", Arrays.toString(args));

		//while ((line = br.readLine()) != null) {
		 // System.out.println(line);
		//}
		return myList;
	}
	
	
}
