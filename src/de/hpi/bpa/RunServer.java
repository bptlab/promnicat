/**
 * 
 */
package de.hpi.bpa;
import java.rmi.*;
import java.net.*;
/**
 * @author rami.eidsabbagh
 *
 */
public class RunServer {
	public static void main(String[] args) {
	    try {
	      RemoteServer localObject = new RemoteServer();
	      Naming.rebind("rmi:///Rem", localObject);
	    } catch(RemoteException re) {
	      System.out.println("RemoteException: " + re);
	    } catch(MalformedURLException mfe) {
	      System.out.println("MalformedURLException: "
	                         + mfe);
	    }
	  }
}
