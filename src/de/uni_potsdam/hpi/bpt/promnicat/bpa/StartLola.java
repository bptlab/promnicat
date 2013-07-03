/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.lang.ProcessBuilder;
/**
 * @author rami.eidsabbagh
 *
 */
public class StartLola {
	//String path ="C:\\renew-2.3\\plugins\\lola-2.2-SE_0.7.1\\lib\\lola-model-checking.exe";
	String path ="C:\\renew-2.3\\plugins\\lola-2.2-SE_0.7.1\\lib\\lola-dead-transition.exe";
	String myList;
	
	
	public String runLola(String param, String param2, String param3) throws Exception{
		//Process process = new ProcessBuilder(path).start();
		ProcessBuilder builder = new ProcessBuilder(path,param,param2,param3);
		 Map<String, String> environment = builder.environment();
		 Process process = builder.start();
		//InputStream is = process.getInputStream();
		String output;
		output = loadStream(process.getInputStream());
		
			
		
        String error  = loadStream(process.getErrorStream());
        int rc = process.waitFor();
        System.out.println("Process ended with rc=" + rc);
        System.out.println("\nStandard Output:\n");
        System.out.println(output);
        System.out.println("\nStandard Error:\n");
        System.out.println(error);
		//		InputStreamReader isr = new InputStreamReader(is);

		return output;
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
