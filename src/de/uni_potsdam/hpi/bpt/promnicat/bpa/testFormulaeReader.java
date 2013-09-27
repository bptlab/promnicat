/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbpt.petri.NetSystem;

/**
 * @author rami.eidsabbagh
 *
 */
public class testFormulaeReader {

	/**
	 * @param args
	 */
	private static final File workDir = new File(System.getenv("userprofile")
			+File.separator+	 ".bpa");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BPA bpa = BPAExamples.complexBPA();
		//BPA bpa = BPAExamples.simpleBPA();
		new BPATransformer().transform(bpa);
		BPATransformer trans = new BPATransformer();
		NetSystem pns = trans.transform(bpa);
		pns.setName(bpa.getName() != null ? bpa.getName() : "Testnetz");
		
		File netFile = new File(workDir+File.separator+ pns.getName() +"Testnetz.net");
		FileWriter fw;
		try {
			fw = new FileWriter(netFile);
			fw.write(LolaNetSerializer.serialize(pns));
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		List<Formula> formula = new ArrayList<Formula>();
		formula = trans.getAllFormulae();
					
		
		File testfile = new File(workDir+File.separator+"Testnetz.net");
			
		
		CorrectnessChecker checker = new CorrectnessChecker(testfile,formula);
		
		
		
		
		checker.checkBPA();
				
		//results = checker.checkModel(testfile.getPath(),task.getPath());
		
	}

}
