/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author rami.eidsabbagh
 *
 */
public class DisplayAnalysisResults extends JFrame {

	JTextField jtfUneditableText;
	JTextArea terminating = new JTextArea(); 
	String disp = "";
	TextHandler handler = null;
	
	//Constructor
	public DisplayAnalysisResults(HashMap <String, HashMap<Integer, ArrayList<String>>> completeResults) {
		super("Display Analysis Box");
		jtfUneditableText = new JTextField("Uneditable text field",20 );
		jtfUneditableText.setEditable(false);
		terminating.setEditable(false);
		Container container = getContentPane();
		container.setLayout(new FlowLayout());
		HashMap<Integer, ArrayList<String>> analysis = new HashMap<Integer, ArrayList<String>>();
		analysis = completeResults.get("deadlock0-terminating1");
		ArrayList<String> results = analysis.get(0);
		System.out.println(results.get(2));
		if(results.get(2).equals("0")){
			jtfUneditableText.setText("The BPA has a deadlock");
		}
		else if(results.get(2).equals("1")){
			jtfUneditableText.setText(" The BPA has no deadlock");
		}else{
			jtfUneditableText.setText("Some error occurred: Return code: "+results.get(2)+" Standard Output: "+results.get(0)+" Error Output: "+results.get(1));
		}
		
		ArrayList<String> terminRunResults = analysis.get(1);
		//Print Out Results for Terminating Run
		if (terminRunResults.get(2).equals("0")) {
			terminating.setText("The BPA has a terminting run");
		}else if (terminRunResults.get(2).equals("1")) {
			terminating.setText("The BPA  has no terminating run"+"\n"); 
			terminating.append("Standard Output: "+terminRunResults.get(0)+" Error Output: "+terminRunResults.get(1));
		}else{
			terminating.setText("The terminating run check caused an error. Return code: "+terminRunResults.get(2)+" Standard Output: "+terminRunResults.get(0)+" Error Output: "+terminRunResults.get(1));
		}
		
		
				
		container.add(jtfUneditableText);
		container.add(terminating);
		handler = new TextHandler();
		jtfUneditableText.addActionListener(handler);
		setSize(500, 500);
		setVisible(true);
	}
	//Inner Class TextHandler
	private class TextHandler implements ActionListener {

	public void actionPerformed(ActionEvent e) {
			if (e.getSource() == jtfUneditableText) {
				disp = "text1 : " + e.getActionCommand();
			
			}
			JOptionPane.showMessageDialog(null, disp);
		}
	}
	
	
}
