
package edu.columbia.cs.sdarts.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.omg.CORBA.IntHolder;

import sdlip.SDLIPException;
import sdlip.XMLObject;
import sdlip.helpers.ClientTransportModule;
import edu.columbia.cs.sdarts.common.STARTS;
import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * The main client class. SDARTS clients, be they Servlet, JSP, application,
 * or Applet based, instantiate a <code>SDARTSBean</code> in order to talk
 * to one single SDARTS LSP. A client must instantiate multiple
 * <code>SDARTSBeans</code> to talk to multiple LSPs.
 * <p>
 * The class includes a simplified interface that
 * reflects SDARTS's synchronous nature - all methods return
 * <code>Strings</code> that contain SDLIP/STARTS XML. See individual method
 * signatures for XML descriptions. There are methods for searching,
 * getting the sub-collection information, getting the property list for
 * one "back-end / sub-collection" of an LSP, and the getting the supported
 * interfaces for the LSP - these are all analagous to the methods in
 * <code>sdlip.Search</code> and <code>sdlip.MetaData</code>.
 * <p>
 * The <code>SDARTSBean</code> uses SDLIP's HTTP/DASL transport layer
 * (see <code>sdlip.helpers.ClientDaslTransport</code> and
 * <code>sdlip.helpers.ClientTransportModule</code>). It does not use
 * the SDLIP CORBA transport layer.
 * <p>
 * A <code>SDARTSBean</code> is initialized in its constructor with URLs
 * for the SDARTS LSP itself, the location of the STARTS DTD, and the
 * location of the SDLIP DTD. All XML outputted by the bean includes
 * &lt !DOCTYPE ... &gt headers pointed at the DTDs, and will pass any validation
 * against these DTDs. The SDLIP namespace is the default namespace;
 * all STARTS elements will have a "starts:" namespace prefix.
 * <p>
 * There is also a <code>main()</code> method, for use by the
 * <code>testclient.sh</code> script that comes with SDARTS.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class SDARTSBean {
  private ClientTransportModule tm;
  private String startsDTDURL;
  private String sdlipDTDURL;

  /**
   * Instantiates a <code>SDARTSBean</code> and immediately connects it
   * to the SDARTS LSP it is meant to communicate with. It looks for the
   * STARTS and SDLIP DTDs in their default locations.
   * @param lspURL the URL of the SDARTS LSP this bean is communicating with
   */
  public SDARTSBean (String lspURL) throws SDLIPException {
    this (lspURL, STARTS.STARTS_DTD_URL, STARTS.SDLIP_DTD_URL);
  }

  /**
   * Instantiates a <code>SDARTSBean</code> and immediately connects it
   * to the SDARTS LSP it is meant to communicate with
   * @param lspURL the URL of the SDARTS LSP this bean is communicating with
   * @param startsDTDURL the URL for the STARTS DTD, which is mentioned as
   * an external parameter entity in all outputted XML
   * @param sdlipDTDURL the URL for the SDLIP DTD, which is mentioned as the
   * primary &lt !DOCTYPE &gt for all outputted XML
   */
  public SDARTSBean (String lspURL, String startsDTDURL, String sdlipDTDURL)
    throws SDLIPException {
      ClientTransportModule.register(sdlip.SDLIP.NameServerURISchema, "sdlip.helpers.ClientCorbaTransport");
      tm = ClientTransportModule.create (lspURL);
      //tm.setDBG(new DBG (DBG.VERBOSE));
      this.startsDTDURL = startsDTDURL;
      this.sdlipDTDURL  = sdlipDTDURL;

      // INITIALIZE XMLWRITER
      XMLWriter.addSystemDocType("STARTS",startsDTDURL);
      XMLWriter.addSystemDocType
        (STARTS.NAMESPACE_NAME + ":smeta-attributes",startsDTDURL);
      XMLWriter.addSystemDocType
        (STARTS.NAMESPACE_NAME + ":scontent-summary",startsDTDURL);
      XMLWriter.addSystemDocType("SearchResult",sdlipDTDURL);
      XMLWriter.addSystemDocType("subcols",sdlipDTDURL);
      XMLWriter.addSystemDocType("subcolInfo",sdlipDTDURL);
      XMLWriter.addSystemDocType("SDLIPInterface",sdlipDTDURL);
      XMLWriter.addSystemDocType("redirect",sdlipDTDURL);
      XMLWriter.addSystemDocType("propList",sdlipDTDURL);
  }

  /**
   * Performs a search. Results returned as a combined SDLIP/STARS XML string
   * of the form:
   * <pre>
   * &lt;SearchResult&gt;
   *    &lt;doc&gt;
   *       &lt;DID&gt; 0...n &lt;/DID&gt;
   *           &lt;propList&gt;
   *              &lt;starts:sqresults&gt;
   *                 . . . . . . .
   *              &lt;/starts:sqresults&gt;
   *           &lt;/propList&gt;
   *    &lt;/doc&gt;
   *    .....
   * &lt;/SearchResult&gt;
   * </pre><br>
   * There will be one <code>&lt;doc&gt;</code> element, with associated
   * embedded STARTS XML, for each subcollection/back-end that is queried.
   * <p>
   * @param subcols an SDLIP header that lists the subcollections to be
   * queried. This should be of &lt;subcols&gt; type - see the SDLIP DTD.
   * This overlaps with the "sources" element of the STARTS XML
   * query. The policy is <b>to search the union of sources in this parameter
   * and what is in the STARTS header.</b> Subcollections or sources are
   * another name for Back End LSPs.
   * @param query the actual query. This should be STARTS XML in the form
   * of &lt;starts:squery&gt; .... &lt;/starts:squery&gt;. See the STARTS DTD.
   * @param numDocs number of documents to be returned. This overlaps with the
   * <code>max-docs</code> attribute of the STARTS query. The policy is to
   * <b>use the maximum of these two numbers</b> for number of documents returned.
   * @param expectedTotal an <b>OUT</b> parameter. Will contain the sum of
   * all <code>numDocs</code> attributes of the returned
   * STARTS <code>starts:sqresults</code> objects. This cannot be
   * <code>null</code> - a caller of this class must pass in an empty
   * <code>org.omg.CORBA.IntHolder</code>.
   * @return A combined SDLIP/STARTS XML string
   * of the form:
   * &lt;SearchResult&gt; ... &lt;starts:sqresults&gt; .... &lt;/starts:sqresults&gt;
   * &lt;/SearchResult&gt;
   */
  public String search (String sdlipSubCollections,
                        String startsXMLQuery,
                        IntHolder expectedTotal,
                        int numDocs)
    throws SDLIPException {
      if (startsXMLQuery == null) {
        throw new SDLIPException (SDLIPException.BAD_QUERY_EXC, "null query");
      }
      if (expectedTotal == null) {
        throw new SDLIPException (SDLIPException.BAD_QUERY_EXC,
          "expected total is an OUT parameter and cannot be null");
      }

      XMLObject subCols = new sdlip.xml.dom.XMLObject (sdlipSubCollections);
      XMLObject query   = new sdlip.xml.dom.XMLObject (startsXMLQuery);
      IntHolder stateTimeout  = new IntHolder (); // unused but can't be null
      IntHolder serverSID     = new IntHolder (); // unused but can't be null
      XMLObject serverDelegate =
        new sdlip.xml.dom.XMLObject(); // unused but can't be null
      XMLObject result = new sdlip.xml.dom.XMLObject();

      tm.search (0, subCols, query, numDocs, null,
                 0, null, expectedTotal, stateTimeout,
                 serverSID, serverDelegate, result);


      if (result != null) {
       // return postProcess (result,"SearchResult",true);
        return result.getString();
      }
      else {
        return null;
      }
  }

  /**
   * Calls the equivalent of the <code>sdlip.Metadata.getInterface()</code>
   * method. See the SDLIP documentation.
   * @return an SDLIP XML string of doctype &lt SDLIPInterface &gt
   */
  public String getInterface () throws SDLIPException {
    XMLObject theInterface = new sdlip.xml.dom.XMLObject();
    tm.getInterface(theInterface);
    //return postProcess (theInterface, "SDLIPInterface", false);
    return theInterface.getString();
  }

  /**
   * Calls the equivalent of the <code>sdlip.Metadata.getPropertyInfo()</code>
   * method. The caller specifies which "subcollection" or "SDARTS backend"
   * to search. The method returns an SDLIP XML string of doctype
   * &lt;propList&gt;, with the STARTS header &lt;starts:smeta-attributes&gt;
   * embedded inside. See the SDLIP documentation for description of the
   * SDLIP method, and the STARTS documentation for information about the
   * &lt;starts:smeta-attributes&gt; tag
   * @param subcolName which subcollection (aka back-end) of the SDARTS
   * LSP to get property information about
   * @return an SDLIP XML string of doctype &lt;propList&gt;, with the
   * STARTS header &lt;starts:smeta-attributes&gt; embedded inside.
   */
  public String getPropertyInfo (String subcolName)
    throws SDLIPException {
      XMLObject propInfo = new sdlip.xml.dom.XMLObject();
      tm.getPropertyInfo(subcolName, propInfo);
     // return postProcess (propInfo, "propList", true);
     return propInfo.getString();
  }

  /**
   * Calls the equivalent of the
   * <code>sdlip.Metadata.getSubcollectionInfo()</code>
   * method. This returns an SDLIP string containing an
   * SDLIP &lt;subcolInfo&gt; element. Note that the subcollections
   * listed in this response correspond to the names of the SDARTS
   * LSP's back-end LSPs. See the SDLIP documentation for more
   * information about this reply.
   * @return an SDLIP string containing an
   * SDLIP &lt;subcolInfo&gt; element
   */
  public String getSubcollectionInfo () throws SDLIPException {
    XMLObject subcolInfo = new sdlip.xml.dom.XMLObject();
    tm.getSubcollectionInfo(subcolInfo);
//    return postProcess (subcolInfo, "subcolInfo", false);
    return subcolInfo.getString();
  }


    /**
     * This <code>main()</code> method is used by the
     * <code>testclient.sh</code> script included with the
     * SDARTS distribution. It assumes that the STARTS
     * DTD resides at
     * <a href="http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd">
     * http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd</a>, and the
     * SDLIP DTD resides at
     * <a href="http://www.cs.columbia.edu/~dli2test/dtd/sdlip.dtd">
     * http://www.cs.columbia.edu/~dli2test/dtd/sdlip.dtd</a>.
     * It accepts two arguments, the second of which is optional.
     * The first argument is the name of the LSP
     * to contact. This can be either an unqualified LSP name, or a URL.
     * The second argument is optional - the filename for an XML file
     * containing a &ltstarts:squery&gt. If given, the method will load
     * this file and attempt to query underlying collections with it.
     * <p>
     * This method will call the <code>search()</code> and
     * <code>getSubcollectionInfo()</code> methods on the LSP contacted.
     * From the output of the second method, it will then determine what
     * subcollections/back-ends are present, and will call
     * <code>getPropertyInfo()</code> for each one. Again, if a query
     * filename has been specified, the method will then try to execute
     * the query on all sources requested in the query.
     * <p>
     * @param args the arguments to the method.
     */
    public static void main (String args[]) {
	  String lspName = args[0];
	  if (!lspName.startsWith ("http://")) {
	    lspName = "http://localhost:8080/" + lspName;
	  }

      String queryFilename = null;
      if (args.length == 2) {
        queryFilename = args[1];
      }

      try {
        SDARTSBean startsBean =
    	  new SDARTSBean (lspName);

        System.out.println ("-----------------------------");
        System.out.println ("Finding supported interfaces of " + lspName);
        String result1 = startsBean.getInterface();
        System.out.println (result1);
        System.out.println ("-----------------------------");
        System.out.println ("Getting subcollection info for " + lspName);
        String result2 = startsBean.getSubcollectionInfo();
        System.out.println (result2);
        System.out.println ("-----------------------------");
        String[] subcolNames = getSubCollectionNames (result2);
        int len = subcolNames.length;
        for (int i = 0 ; i < len ; i++) {
          String name = subcolNames[i];
          System.out.println ("Getting subcollection info for " + lspName +
                              ":" + name);
          String result3 = startsBean.getPropertyInfo(name);
          System.out.println (result3);
          System.out.println ("-----------------------------");
        }

        if (queryFilename != null) {
          StringBuffer sb = new StringBuffer();
          String testQuery = "";
          BufferedReader br =
            new BufferedReader (
              new InputStreamReader (
                new FileInputStream (queryFilename)));
          String line = null;
          while ( (line = br.readLine()) != null ) {
            sb.append (line);
          }

          testQuery = sb.toString();
          IntHolder total = new IntHolder();
          String result3 = startsBean.search (null,testQuery,total,1000);
          System.out.println ("-----------------------------");
          System.out.println ("Found " + total.value + " hits.");
          System.out.println (result3);
          System.out.println ("-----------------------------");
        }
      }
      catch (SDLIPException e) {
        System.out.println (e.getCode());
        e.printStackTrace();
        XMLObject details = e.getDetails();
        if (details != null) {
          try {
            System.out.println ("DETAILS:");
            System.out.println (details.getString());
          }
          catch (Exception e2) {
            e2.printStackTrace();
          }
        }
        System.exit(1);
      }
      catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    // Helper method for use with main()
    private static String[] getSubCollectionNames (String subcolInfo) {
      List nameList = new LinkedList();
      int lastIndex = subcolInfo.lastIndexOf("</subcolName>");
      int index = 0;
      while (index < lastIndex) {
        index = subcolInfo.indexOf("<subcolName>", index);
        int endIndex = subcolInfo.indexOf("</subcolName>", index);

        String name = subcolInfo.substring(index+12, endIndex).trim();
        nameList.add (name);
        index = endIndex;
      }
      return (String[]) nameList.toArray (new String[0]);
    }
}
