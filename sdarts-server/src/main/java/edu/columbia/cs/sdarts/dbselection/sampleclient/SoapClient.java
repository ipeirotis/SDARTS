package edu.columbia.cs.sdarts.dbselection.sampleclient;

import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;

/**
 * This is a test class, to test the dbselection componennt runs under soap
 * This class assumes the soap server is running at:<br>
 * http://persival.cs.columbia.edu:8181/soap/servlet/rpcrouter<br>
 * and the dbselection server is named db-selection-server<br>
 * and the exported method is processInput(String type, String input);
 * <p>
 * to run from commandline, provied parameter as<br>
 * query "algorithm=cori food"<br>
 * or <br>
 * query food<br>
 * or <br>
 * add "http://sdarts.cs.columbia.edu:8080/sdarts noah"<br>
 * or <br>
 * remove "http://sdarts.cs.columbia.edu:8080/sdarts noah"
 * <p>
 * @author <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class SoapClient {
	/**
	 * the command line program entry, takes two arguments,
	 * <ul>
	 * <li>the first one is command, can be <code>query</code>,
	 * <code>add</code>, <code>remove</code>
	 * <li>the second one depends on the command
	 * <ul>for 
	 * <li>query - the term(s) to be queried, optionally can specify
	 *             ranking algorithm by writing "algorithm=name" at the beginning
	 * <li>add - the sdarts server url, optionally, one or more collection names
	 * <li>remove - the sdarts sver url, one or more collection names
	 * </ul>
	 * </ul>
	 *
	 * @param args the command arguments
	 * @throws Exception a general exception for different types of errors 
	 */
	public static void main(String[] args) throws Exception {

		String soapServerURL = "http://persival.cs.columbia.edu:8181/soap/servlet/rpcrouter";

		if (args.length != 2) {
			System.err.println("Usage: " + SoapClient.class.getName() + " <type> <input>");
			System.exit(1);
		}

		String type = args[0];
		String terms = args[1];

		Call call = new Call();
		call.setTargetObjectURI("urn:db-selection-server");
		call.setMethodName("processInput");
		call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
		Vector params = new Vector();
		params.addElement(new Parameter("type", type.getClass(), type, null));
		params.addElement(new Parameter("input", terms.getClass(), terms, null));
		call.setParams(params);

		Response resp = call.invoke(new URL(soapServerURL), "");

		// Check the response.
		if (resp.generatedFault()) {
			Fault fault = resp.getFault();
			System.out.println("Ouch, the call failed: ");
			System.out.println("  Fault Code   = " + fault.getFaultCode());
			System.out.println("  Fault String = " + fault.getFaultString());
			Vector details = fault.getDetailEntries();
			if (details != null) {
				Enumeration detailsEnum = details.elements();
				while (detailsEnum.hasMoreElements()) {
					System.out.println(detailsEnum.nextElement().toString());
				} 
			}
		} else {
			byte[] retval = (byte[]) resp.getReturnValue().getValue();
			System.out.println(new String(retval));
		}
	}

}
