package edu.columbia.cs.sdarts.dbselection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jdom.JDOMException;

import sdlip.SDLIPException;
import edu.columbia.cs.sdarts.client.SDARTSBean;
import edu.columbia.cs.sdarts.dbselection.util.ContentSummaryHandler;
import edu.columbia.cs.sdarts.dbselection.util.MetaDataHandler;
import edu.columbia.cs.sdarts.dbselection.util.Subcol;
import edu.columbia.cs.sdarts.dbselection.util.SubcolInfo;

/**
 * <p>
 * this class runs under soap server, export method {@link #processInput() processInput}(String 
 * type, String input), where type can be search: to query the rankings
 * of available collections on given term 
 * add: to add a sdarts server with optional collection names; or remove: to
 * remove a collection from given sdarts server
 * <p>
 * when given a term(s) to query, by default, a CORI ranking score for each collection
 * that has been indexed is calculated, and returned within a XML string 
 * representation of a {@link edu.columbia.cs.sdarts.dbselection.util.SubcolInfo SubcolInfo} object.
 * Other ranking algorithm can be specified by giving 
 * algorithm=algorithm-name at the beginning of the term
 * Currently, only CORI is supported.
 * <p>
 * when given command add or remove, with appropriate sdarts server url and
 * collection names(s), the collection(s) will be added into (removed from)
 * the in-memory indices
 * <p>
 * a sample code for calling to the dbselection server might be like this:
 * <code>
 * <pre>
 *  Call call = new Call();
 *  call.setTargetObjectURI ("urn:db-selection-server");
 *  call.setMethodName ("processInput");
 *  call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC );
 *  Vector params = new Vector();
 *  params.addElement (new Parameter("type", type.getClass(), type, null));
 *  params.addElement (new Parameter("input", input.getClass(), input, null));
 *  call.setParams (params);
 *  Response resp = call.invoke(new URL("http://persival.cs.columbia.edu:8181/soap/servlet/rpcrouter"),"" );
 *  if (resp.generatedFault ()) {
 *   Fault fault = resp.getFault ();
 *   System.out.println ("Ouch, the call failed: ");
 *   System.out.println ("  Fault Code   = " + fault.getFaultCode ());
 *   System.out.println ("  Fault String = " + fault.getFaultString ());
 *   Vector details = fault.getDetailEntries();
 *   Enumeration detailsEnum = details.elements();
 *   while(detailsEnum.hasMoreElements())
 *    System.out.println(detailsEnum.nextElement().toString());
 *  }
 *  else {
 *    byte[] retval = (byte []) resp.getReturnValue().getValue();
      System.out.println(new String(retval));
 * }
 * </pre>
 *</code>
 * 
 * @see edu.columbia.cs.sdarts.dbselection.sampleclient.SoapClient
 * @author <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */

public class DBSelectionServer {

	public static void main(String[] args) throws Exception {

		boolean listening = true;
		ServerSocket serverSocket = null;

		int port = 9090;

		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		final DBSelectionServer dbss = new DBSelectionServer();

		serverSocket = new ServerSocket(port);
		String hostname = InetAddress.getLocalHost().getHostName();
		System.out.println("DBSelection server running at port " + port);
		System.out.println("accessible at: http://" + hostname + ":" + port);

		//System.out.println(new String(dbss.processInput("query", "cancer")));

		while (listening) {
			//new ClientHelper(serverSocket.accept(), dbss.indices).start();
			// create a new thread here to handle this request
			final Socket socket = serverSocket.accept();

			Thread t = new Thread() {
				public void run() {

					try {
						OutputStream out = socket.getOutputStream();
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String inputLine = ""; // we only care for the first input line
						String type = "";
						String input = "";
						byte[] result = new String("No result produced!").getBytes();
						String remoteHostName = socket.getInetAddress().getHostName();

						if ((inputLine = in.readLine()) != null) {
							StringTokenizer st = new StringTokenizer(inputLine, " ");
							if (st.hasMoreTokens()) {
								type = st.nextToken();
								if (st.hasMoreTokens()) {
									input = st.nextToken("\n"); // next token till end of line
								}
							}
							if (type.length() > 0 && input.length() > 0) {
								// got the right input, process it
								result = dbss.processInput(type, input);
							}
						}
						out.write(result);
						out.flush();
						in.close();
						socket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			t.start();
		}

		serverSocket.close();
	}

	/**
	 * default constructor, initialize the index vector,
	 * and index collections from the default sdarts server
	 */
	public DBSelectionServer() {
		this.indices = new Vector();
		String msg = this.index(this.defaultSdartsServerURL);
		System.err.println(msg);
	}

	/**
	 * given a sdarts server url <code>sdartsServerURL</code>,
	 * index all it's sub collections.
	 * 
	 * @param sdartsServerURL the url of the sdarts server whose sub collections
	 *                        are to be indexed
	 * @return                a message indicates whether the index succeeded or
	 *                        not
	 */
	private String index(String sdartsServerURL) {

		String result = "";

		String serverURL = sdartsServerURL;

		try {
			SDARTSBean bean = new SDARTSBean(serverURL);
			String collectionInfo = bean.getSubcollectionInfo();

			// now parse the collectionInfo retrieved using sax
			SubcolInfo collections = new SubcolInfo(collectionInfo);

			// now download the content summary for all collections
			for (int j = 0; j < collections.subcols.size(); j++) {

				Subcol sc = (Subcol) collections.subcols.elementAt(j);
				String collectionName = sc.subcolName;
				String collectionDesc = sc.subcolDesc;

				String s = this.index(bean, serverURL, collectionName, collectionDesc);
				result += s + "\n";

			}

		} catch (SDLIPException se) {
			System.err.println("Error initializing sdarts bean!");
			result += "Error in initializing sdarts bean for server " + serverURL + "\n";
			se.printStackTrace();
		} catch (JDOMException e) {
			System.err.println("Error parsing collection info retrieved");
			result += "Error in parsing collection info for server " + serverURL + "\n";
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * given a SDARTSBean instance <code>bean</code>, it's associated server url
	 * <code>serverURL</code>, a collection name <code>collectionName</code> and
	 * it's description <code>collectionDesc</code>,
	 * get the content summary linkage, then get the content summary
	 * and index it into indicies.
	 * <p>
	 * note: if this collection is already in the index, the index will
	 * be updated.
	 *
	 * @param bean   a {@link edu.columbia.cs.sdarts.client.SDARTSBean SDARTSBean} object,
	 *               by which we talk to the sdarts server
	 * @param serverURL the url of the sdarts server
	 * @param collectionName the name of the collection being indexed
	 * @param collectionDesc the description of the collection being indexed
	 * @return a message indicates whether the index succeed or not
	 */
	private String index(SDARTSBean bean, String serverURL, String collectionName, String collectionDesc) throws SDLIPException {

		String result = "";

		String metaInfo = bean.getPropertyInfo(collectionName);
		HashMap metaInfos = MetaDataHandler.parse(metaInfo, serverURL);
		String contentSummaryLinkage = (String) metaInfos.get("content-summary-linkage");

		// we want to download the content summary file,
		// and save it to local drive
		if (contentSummaryLinkage != null) {
			try {
				URL contentSummaryURL = new URL(contentSummaryLinkage);

				// we now create index for this collection
				System.out.println("\nLoading " + collectionName + ": " + collectionDesc + " ...");
				HashMap index = ContentSummaryHandler.index(contentSummaryURL);
				Index ind = new Index(serverURL, collectionName, collectionDesc, index);

				// see if the same collection exists, if yes, remove it first
				boolean duplicated = false;
				for (int i = 0; i < indices.size(); i++) {
					Index curInd = (Index) indices.elementAt(i);
					if (ind.isSameCollection(curInd)) {
						indices.removeElementAt(i);
						result += "Removing collection " + collectionName + " from server " + serverURL + " successfully!\n";
						i--; // so that we are not skipping the actual next element
					}
				}

				this.indices.add(ind);
				result += "Add collection "
					+ collectionName
					+ " to server "
					+ serverURL
					+ " successfully! ("
					+ index.size()
					+ " terms)\n";

				System.out.println(collectionName + " loaded successfully (" + index.size() + " terms)!");
			} catch (MalformedURLException e) {
				result += "Invalid content summary link url: "
					+ contentSummaryLinkage
					+ " for collection "
					+ collectionName
					+ " on server "
					+ serverURL
					+ "\n";
				System.err.println("Invalid URL: " + contentSummaryLinkage);
				System.err.println("Error creating index for collection " + collectionName);
				e.printStackTrace();
			}
		} else {
			result += "Can not find content summary link url for collection "
				+ collectionName
				+ " on server "
				+ serverURL
				+ "\n"
				+ "The metaInfo I got is: \n"
				+ metaInfo
				+ "\n";
			System.err.println(
				"\nCan not find content summary linkage "
					+ "for collection "
					+ collectionName
					+ " on server "
					+ serverURL
					+ "\nThe metaInfo I got is:\n"
					+ metaInfo);
		}

		return result;

	}

	/**
	 * given a string <code>input</code>, calculate the CORI ranking score for
	 * all available collections by calling
	 * {@link #processInput(String) processInput(String)}.
	 * <p>
	 * This method is synchronized so that search and updating index will not
	 * conflict each other when accessing the in-memory index.
	 *
	 * @param input the input terms to search for
	 * @return a string representation of search result, which is an {@link edu.columbia.cs.sdarts.dbselection.util.SubcolInfo SubcolInfo} object
	 */
	public synchronized String search(String input) {

		return new ClientHelper(this.indices).processInput(input);

	}

	/**
	 * add one or more collections(s) for sdarts server at 
	 * <code>sdartsServerURL</code> into the indecies in memory
	 *
	 * @param sdartsServerURL the sdarts server url for the server who hosts
	 *                        these collections
	 * @param collections     the collection names
	 * @return a message indicates whether the adding succeeded or not
	 */
	public synchronized String add(String sdartsServerURL, String[] collections) {
		String result = "";

		try {
			if (sdartsServerURL == null) {
				result = "no collection to add!";
			} else if ((collections == null) || (collections.length == 0)) {
				String s = this.index(sdartsServerURL);
				result = "adding all collections from sdarts server " + sdartsServerURL + "\n" + s + "\n";
			} else {
				String serverURL = sdartsServerURL;
				SDARTSBean bean = new SDARTSBean(serverURL);
				String collectionInfo = bean.getSubcollectionInfo();

				SubcolInfo cols = new SubcolInfo(collectionInfo);

				// now process all our requested collections
				for (int i = 0; i < collections.length; i++) {
					String collectionDesc = "";
					for (int j = 0; j < cols.subcols.size(); j++) {
						Subcol sc = (Subcol) cols.subcols.elementAt(j);
						if (collections[i].equalsIgnoreCase(sc.subcolName)) {
							collectionDesc = sc.subcolDesc;
						}
					}
					// it will be reindexed if it is already in
					String s = this.index(bean, serverURL, collections[i], collectionDesc);
					result += s + "\n";
				}

				result += collections.length + " collections added for server" + serverURL + "!\n";
			}
		} catch (SDLIPException e) {
			e.printStackTrace();
			result = e.toString();
		} catch (JDOMException e) {
			e.printStackTrace();
			result = e.toString();
		}

		return result;
	}

	/**
	 * remove zero or more collections for the sdarts server at 
	 * <code>sdartsServerURL</code> from the in-memory indecies
	 *
	 * @param sdartsServerURL the sdarts server url for which these collections
	 *                        are to be removed
	 * @param collections     the names of the collections to be removed from
	 *                        the in-memory indices
	 * @return a message indicates whether the removal is successful or not
	 */
	private String remove(String sdartsServerURL, String[] collections) {
		String result = "";

		if (sdartsServerURL == null) {
			result = "attempt to remove from invalid sdarts server!";
		} else if ((collections == null) || (collections.length == 0)) {
			for (int i = 0; i < indices.size(); i++) {
				Index index = (Index) indices.elementAt(i);
				String collectionName = index.name;
				if (index.serverURL.equalsIgnoreCase(sdartsServerURL)) {
					indices.removeElementAt(i);
					result += "Collection "
						+ collectionName
						+ " for server "
						+ sdartsServerURL
						+ " has been successfully removed from "
						+ " the index!\n";
				}
			}
		} else {
			for (int j = 0; j < collections.length; j++) {
				String collectionName = collections[j];
				for (int i = 0; i < indices.size(); i++) {
					Index index = (Index) indices.elementAt(i);
					if (index.name.equalsIgnoreCase(collectionName) && index.serverURL.equalsIgnoreCase(sdartsServerURL)) {
						indices.removeElementAt(i);
						i--; // so that we are not skipping the actual next element
						result += "Collection "
							+ collectionName
							+ " for server "
							+ sdartsServerURL
							+ " has been successfully removed from "
							+ " the index!\n";
					}
				}
			}
		}

		return result;

	}

	/**
	 * given an command <code>type</code>, an input string <code>input</code>,
	 * returns a string
	 * <p>
	 * the command can be one of the following values:
	 * <ul>
	 * <li>query -- then <code>input</code> is terms to be queried about.
	 * optionally, a leading <code>algorithm=algorithm-name</code> will specify
	 * a different ranking algorithm, than the default CORI.
	 * The result will be a string representation of
	 * {@link edu.columbia.cs.sdarts.dbselection.util.SubcolInfo SubcolInfo} is returned.</li>
	 * <li>add -- then <code>input</code> is the sdarts server url followed
	 * by one or more space-separated collection names that is to be added. the
	 * return result will be a message indicates whether the adding is successful
	 * or not. </li>
	 * <li>remove -- then <code>input</code> is the sdarts server url followed
	 * by one or more space-separated collection names that is to be removed. the
	 * return result will be a message indicates whether the remvoal is successful
	 * or not. </li>
	 *
	 * @param type the command type, must be one of "search", "add", "remove"
	 * @param input the parameters for the command
	 * @return a bytearray of string 
	 */
	public byte[] processInput(String type, String input) {
		String result =
			"please provide two parameters\n"
				+ "processInput(String type, String input)\n"
				+ "type must be one of \"query\", \"add\" and \"remove\"\n"
				+ "for \"query\", the algorithm can be specified by preceeding terms"
				+ " with \"algorithm=algorithm-name\", note no leading space\n"
				+ "for \"add\", input can be sdarts server url, with zero or more \n"
				+ "collection name attached, all separated by space\"\n"
				+ " for \"remove\", input must contain the sdarts server url\n"
				+ "with zero or more space-separated collection name(s)";

		if ((type != null) && (input != null)) {

			if (type.equals("query")) {

				result = this.search(input);

			} else if (type.equals("add") || type.equals("remove")) {

				// use default delimiter " \t\n\r\f"
				StringTokenizer st = new StringTokenizer(input);
				int count = st.countTokens();
				String serverURL;

				if (count == 1) {
					serverURL = st.nextToken();
					if (type.equals("add")) {
						result = this.add(serverURL, null);
					} else {
						// remove
						result = this.remove(serverURL, null);
					}
				} else if (count > 1) {
					// read out sdtartsServerURL
					serverURL = st.nextToken();
					String[] collections = new String[count - 1];
					int i = 0;
					while (st.hasMoreTokens()) {
						// read all input collection names
						collections[i] = st.nextToken();
					}
					if (type.equals("add")) {
						result = this.add(serverURL, collections);
					} else {
						result = this.remove(serverURL, collections);
					}

				} else {

					result = "Error in parameter input!";

				}
			}
		}

		return result.getBytes();

	}

	public Vector indices;

	public String defaultSdartsServerURL = "http://sdarts.cs.columbia.edu:8080/sdarts";
}
