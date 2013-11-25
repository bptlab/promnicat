/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author rami.eidsabbagh
 *
 */
public class Mapper {
	private static Mapper myMapper = null;
	private HashMap<String, String> mapping = new HashMap<String, String>();
	private HashMap<String, String> formulaMapping = new HashMap<String, String>();
	private Mapper(){
		
	}
	
	public static Mapper getInstance(){
        if (myMapper == null)
            myMapper = new Mapper();
        return myMapper;
    }
 
    public void addEventToProcess(String eventSid, String processSid){
        mapping.put(eventSid, processSid);
  
    }
    
    public HashMap<String, String> getAllMapping(){
    	return mapping;
    }
    
    
    public String getProcessIdFromEvent(String key){
		String value = null;
    	if(!mapping.isEmpty()){
			value = mapping.get(key);	
		}
    	
    	return value;
    	
    }
    
    public void setFormulatoProcess(String formulaName, String processSid){
    	 mapping.put(formulaName, processSid);
    	
    }
    
    public String getProcessIdFromFormula(String key){
		String value = null;
    	if(!formulaMapping.isEmpty()){
			value = formulaMapping.get(key);	
		}
    	
    	return value;
    	
    }
    
    
}