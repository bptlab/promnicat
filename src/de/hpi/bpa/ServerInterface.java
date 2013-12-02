/**
 * @author rami.eidsabbagh
 *
 */
package de.hpi.bpa;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Map;
import java.util.ArrayList;



public interface ServerInterface extends Remote
{
	
	
  public void method() throws RemoteException;
  
  public String getMessage() throws RemoteException;
  
  public Map<String,ArrayList<String>> checkModel(String jsonString) throws RemoteException;
}


