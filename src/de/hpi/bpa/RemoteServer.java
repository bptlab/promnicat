/**
 * 
 */
package de.hpi.bpa;

/**
 * @author rami.eidsabbagh
 *
 */
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.StaticBucketMap;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONException;

import de.uni_potsdam.hpi.bpt.promnicat.bpa.*;


public class RemoteServer extends UnicastRemoteObject implements ServerInterface
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -4862150313671105244L;
	private Map<String,ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
	RemoteServer() throws RemoteException
  {
    super();
  }
  
	 public static void main(String[] args)
	  {
	    try {
	      LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
	    }
	    
	    catch (RemoteException ex) {
	      System.out.println(ex.getMessage());
	    }
	    try {
	      Naming.rebind("Server", new RemoteServer());
	    }
	    catch (MalformedURLException ex) {
	      System.out.println(ex.getMessage());
	    }
	    catch (RemoteException ex) {
	      System.out.println(ex.getMessage());
	    }
	  }
	
	public String getMessage() throws RemoteException {
	    return("Here is a remote message.");
	  }


	/* (non-Javadoc)
	 * @see de.uni_potsdam.hpi.bpt.promnicat.util.ServerInterface#method()
	 */
	@Override
	public void method() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Server runs");
		
	}
  
	public void run() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("run run runServer runs");
		
	}
	
	public Map<String,ArrayList<String>> checkModel(String jsonString) {
		
		try {
			JSONObject json = new JSONObject(jsonString);
			result = BPAAnalyzer.analyseBPA(json);
			
		} catch (JSONException e) {
			// TODO: handle exception
		}
		
		System.out.println(result);
		return result;
	}
}