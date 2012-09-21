
package edu.columbia.cs.sdarts.frontend;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.IntHolder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sdlip.Metadata;
import sdlip.SDLIP;
import sdlip.SDLIPException;
import sdlip.Search;
import sdlip.XMLObject;
import sdlip.xml.dom.DOMUtil;
import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.BackEndLSP;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPQueryBuilder;
import edu.columbia.cs.sdarts.common.LSPResults;
import edu.columbia.cs.sdarts.common.LSPSource;
import edu.columbia.cs.sdarts.common.STARTS;
import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * The gateway into the entire SDARTS server - essentially an SDLIP LSP
 * that understands STARTS. As a fully-compliant SDLIP LSP,
 * <code>FrontEndLSP</code> implements the <code>sdlip.Search</code> and
 * <code>sdlip.MetaData</code> interfaces. It also acts as a front to
 * one or more underlying wrapped collections, each of which is accessible
 * via an instance of {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}.
 * <p>
 * A <code>FrontEndLSP</code> initializes itself by getting a
 * {@link edu.columbia.cs.sdarts.frontend.SDARTSConfig SDARTSConfig} from a
 * {@link edu.columbia.cs.sdarts.frontend.SDARTSConfigBuilder SDARTSConfigBuilder}. It does
 * this by reading in the <code>sdarts_config.xml</code> file, which must
 * reside in the <code>/config</code> subdirectory of the server installation.
 * During initialization, the <code>FrontEndLSP</code> creates each of the
 * <code>BackEndLSPs</code> it is fronting, and initializes them.
 * <p>
 * The configurations of each <code>BackEndLSP</code> must reside in
 * subdirectories of <code>/config</code>, each with the same name that
 * the <code>BackEndLSP</code> is registered under.
 * <p>
 * Once it is initialized, the class performs the following functions:
 * <ul>
 * <li>Receives STARTS XML requests via SDLIP method calls from clients
 * <li>Passes these requests as {@link edu.columbia.cs.sdarts.common.LSPObject} headers
 * to one or more underlying <code>BackEndLSPs</code>
 * <li>Receives <code>LSPObject</code> replies from the <code>BackEndLSPs</code>
 * <li>Converts these into SDLIP/STARTS XML, and returns them to the clients.
 * </ul>
 * <p>
 * Communication with the <code>BackEndLSP</code>, which resides in the
 * {@link edu.columbia.cs.sdarts.backend} layer, happens using <code>LSPObject</code> headers.
 * These headers are all defined in the {@link edu.columbia.cs.sdarts.common} package.
 * <p>
 * In terms of communication with the client layer, the
 * <code>FrontEndLSP</code> returns all of its data in a hybrid of STARTS
 * and SDLIP XML format. See the
 * {@link edu.columbia.cs.sdarts.client} package for more details, as well as the SDARTS
 * design document.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class FrontEndLSP implements Search, Metadata {
  // ------------ FIELDS ------------
  protected Map backEndLSPs;



  // ------------ METHODS ------------
  // -------- CONSTRUCTOR --------
  /**
   * Creates a new FrontEndLSP. This should be done in the context of
   * a server process - for example, the class <code>SDARTS</code> does
   * this.
   * <p>
   * The <code>FrontEndLSP</code> will immediately look for its configuration
   * file and configure itself. This file is of the XML format
   * <code>sdarts_config</code>. The <code>FrontEndLSP</code> will look
   * in the {@link edu.columbia.cs.sdarts.frontend.SDARTS#CONFIG_DIRECTORY} directory for
   * the file, which is called <code>sdarts_config.xml</code>.
   * <p>
   * It will then configure each of its <code>BackEndLSPs</code>, as they
   * are described in its <code>sdarts_config.xml</code> file. The configurations
   * of each <code>BackEndLSP</code> must reside in
   * subdirectories of <code>/config</code>, each with the same name that
   * the <code>BackEndLSP</code> is registered under.
   * @exception SDLIPException if something goes wrong during configuration
   */
  public FrontEndLSP () throws SDLIPException {
    try {
      SDARTSConfig config = SDARTSConfigBuilder.fromXML ();

      // Store URL for all DTDs and initialize XMLWriter
      String startsDTDURL = config.getStartsDtdURL();
      String sdlipDTDURL  = config.getSdlipDtdURL();
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

      // Initialize all BackEndLSPs
      BackEndLSPDescriptor[] descriptors = config.getBackEndLSPDescriptors();
      int len = descriptors.length;
      backEndLSPs = new HashMap (len);
      for (int i = 0 ; i < len ; i++) {
        BackEndLSPDescriptor descriptor = descriptors[i];
        String name = descriptor.getName();
        String description = descriptor.getDescription();
        String[] queryLanguages = descriptor.getQueryLanguages();

        BackEndLSP lsp =
          (BackEndLSP) Class.forName (descriptor.getClassname()).newInstance();
        System.out.println("Initializing " + name + " back-end");
        lsp.initialize (name, description, queryLanguages);
        System.out.println("Initialized!");
        backEndLSPs.put (name, lsp);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC,
                                e.getMessage());
    }
  }


  // -------- GETTERS / SETTERS --------
  /**
   * Returns all <code>BackEndLSP</code> instances that this
   * <code>FrontEndLSP</code> can serve
   * @return all <code>BackEndLSP</code> instances that this
   * <code>FrontEndLSP</code> can serve
   */
  public BackEndLSP[] getBackEndLSPs () {
    return (BackEndLSP[]) backEndLSPs.values().toArray(new BackEndLSP[0]);
  }

  /**
   * Returns the names of all <code>BackEndLSP</code> instances that this
   * <code>FrontEndLSP</code> can serve
   * @return the names of all <code>BackEndLSP</code> instances that this
   * <code>FrontEndLSP</code> can serve
   */
  public String[] getBackEndLSPNames () {
    return (String[]) backEndLSPs.keySet().toArray (new BackEndLSP[0]);
  }



  // -------- SDLIP INTERFACES --------
  // -------- SEARCH INTERFACE --------
  /**
   * Implements the SDLIP <code>Search</code> interface method. The result
   * of the search will be written into <code>result</code>, which is an
   * OUT parameter.
   * <p>
   * See the SDLIP documentation for more information about this method.
   * <p>
   * Results will be returned as a combined SDLIP/STARS XML string
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
   * The merging of STARTS and SDLIP means that there are some parameters
   * in this method that duplicate items that appear in the STARTS
   * XML <code>query</code> parameter. In addition, this framework does not
   * support asynchronous or stateful searches. Below is the policy that
   * results, described parameter by parameter.
   * @param clientSID not used as this implementation is stateless
   * @param subcols an SDLIP header that lists the subcollections to be
   * queried. This overlaps with the "sources" element of the STARTS XML
   * query. The policy is <b>to search the union of sources in this parameter
   * and what is in the STARTS header.</b> Subcollections or sources are
   * another name for <code>BackEndLSPs</code>.
   * @param query the actual query. This should be STARTS XML in the form
   * of &lt;starts:squery&gt; .... &lt;/starts:squery&gt;.
   * @param numDocs number of documents to be returned. This overlaps with the
   * <code>max-docs</code> attribute of the STARTS query. The policy is to
   * <b>use the maximum of these two numbers</b> for number of documents returned.
   * @param docPropList this overlaps with the <code>answer-fields</code>
   * element of STARTS. Because all metadata is also in STARTS format, it is
   * possible that the fields in this parameter are not valid. Therefore,
   * the policy is <b>to ignore this parameter</b> and use the information
   * only in the STARTS header.
   * @param stateTimeoutReq not used as this implementation is stateless
   * @param queryOptions ignored, superseded by the STARTS header
   * @param expectedTotal an <b>OUT</b> parameter. Will contain the sum of
   * all <code>numDocs</code> attributes of the returned
   * STARTS <code>sqresults</code> objects
   * @param stateTimeout not used as this implementation is stateless
   * @param serverSID not used as this implementation is stateless
   * @param serverDelegate no delegation is implemented in this release
   * @param result an <b>OUT</b> parameter. Contains a STARTS XML header
   * of the form:
   * &lt starts-header &gt &lt sqresults &gt .... &lt /sqresults &gt &lt /starts-header &gt.
   */
  public void search (int clientSID,
			       XMLObject subcols,
			       XMLObject query,
			       int numDocs,
			       XMLObject docPropList,
			       int stateTimeoutReq,
			       XMLObject queryOptions,
			       IntHolder expectedTotal,
			       IntHolder stateTimeout,
			       IntHolder serverSID,
			       XMLObject serverDelegate,
			       XMLObject result ) throws SDLIPException {
    // First, get a string name list of the subcollections from the
    // subcols parameter
    String[] subcolNames = null;
    if (subcols != null) {
      Element topLevel = ((sdlip.xml.dom.XMLObject) subcols).getElement();
      if (topLevel != null) {
        NodeList children = topLevel.getChildNodes();
        int len = children.getLength();
        List temp = new LinkedList();

        for (int i = 0 ; i < len ; i++) {
          Element child = (Element) children.item(i);
          if (child.getNodeName().equals("subcolName")) {
            temp.add (DOMUtil.getText (child));
          }
        }
        subcolNames = (String[]) temp.toArray (new String[0]);
      }
    }

    // Turn query into LSPObject, passing the SDLIP parameters as well
    LSPQuery queryObj = null;
    try {
      String queryString = query.getString();
      StringReader sr = new StringReader (queryString);

      queryObj = LSPQueryBuilder.fromXML (sr, numDocs, subcolNames);
    }
    catch (Exception e) {
      throw new SDLIPException (SDLIPException.BAD_QUERY_EXC, e.getMessage());
    }

    // Get the list of back-end LSPs from the merged subcollection list names
    LSPSource[] sources = queryObj.getSources();
    if (sources == null) {
      throw new SDLIPException
        (SDLIPException.BAD_QUERY_EXC, "No specified sources!");
    }
    int len = sources.length;
    if (len == 0) {
      throw new SDLIPException
        (SDLIPException.BAD_QUERY_EXC, "No specified sources!");
    }

    // Get the collections that are present
    List collList = new LinkedList();
    for (int i = 0 ; i < len ; i++) {
      BackEndLSP backEnd = (BackEndLSP) backEndLSPs.get (sources[i].getName());
      if (backEnd != null) {
        collList.add (backEnd);
      }
    }
    if (collList.size() == 0) {
       throw new SDLIPException
        (SDLIPException.BAD_QUERY_EXC, "No valid sources in query!");

    }
    BackEndLSP[] collections =
      (BackEndLSP[]) collList.toArray (new BackEndLSP[0]);

    // Perform queries
    int totalDocs = 0;
    List results = new ArrayList (len);
    for (int i = 0 ; i < len ; i++) {
      LSPResults resultObj = null;
      try {
        // TODO : MAKE LSPQUERY CLONEABLE!!!!  AND CLONE IT HERE !!!!!
        resultObj = collections[i].query (queryObj);
      }
      catch (BackEndException e) {
        throw new SDLIPException (SDLIPException.BAD_QUERY_EXC, e.getMessage());
      }
      totalDocs += resultObj.getNumDocs();
      if (resultObj != null) {
        results.add (resultObj);
      }
    }

    // Merge results
    String resultString = null;
    try {
      resultString = mergeResults (results);
    }
    catch (IOException e) {
      throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC, e.getMessage());
    }


   expectedTotal.value = totalDocs;
   result.setString (resultString);
   System.out.println ("processed search request");
  }


  // -------- METADATA INTERFACE --------
  /**
   * Implements the <code>sdlip.Metadata.getInterface()</code> method.
   * It writes the response as SDLIP XML into <code>theInterface</code>,
   * which is an OUT parameter. See the SDLIP documentation for more
   * information on how the response is formatted.
   * @param theInterface an OUT parameter in which to write the response
   */
  public void getInterface (XMLObject theInterface) throws SDLIPException {
    String xmlString = null;
    System.out.println ("receiving getInterface request");
    try {
      StringWriter sw = new StringWriter();
      //XMLWriter writer = new XMLWriter (sw, new String[] {"SDLIPInterface"});
      XMLWriter writer = new XMLWriter (sw);
      writer.enterNamespace("sdlip");
      writer.printStartElement ("SDLIPInterface", true);
      writer.printNamespaceDeclaration("sdlip",SDLIP.Namespace);
      writer.printStartElementClose();
      writer.indent();
      writer.printStartElement("SearchInterface");
      writer.indent();
      writer.printEntireElement ("version", "1.0");
      writer.unindent();
      writer.printEndElement ("SearchInterface");
      writer.printStartElement("MetadataInterface");
      writer.indent();
      writer.printEntireElement ("version", "1.0");
      writer.unindent();
      writer.printEndElement ("MetadataInterface");
      writer.unindent();
      writer.printEndElement ("SDLIPInterface");
      writer.flush();
      xmlString = sw.toString();
      writer.exitNamespace();
      writer.close();
    }
    catch (Exception e) {
      throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC, e.getMessage());
    }

    theInterface.setString (xmlString);
    System.out.println ("processed getInterface request");
  }

  /**
   * Implements the <code>sdlip.Metadata.getPropertyInfo()</code>
   * method. The caller specifies which "subcollection" or "SDARTS backend"
   * to search.
   * <p>
   * The method's result is an SDLIP XML string of doctype
   * &lt propList &gt, with the STARTS header &lt starts:smeta-attributes &gt
   * embedded inside. See the SDLIP documentation for description of the
   * SDLIP method, and the STARTS documentation for information about the
   * &lt starts:smeta-attributes &gt tag
   * <p>
   * @param subcolName which subcollection (aka <code>BackEndLSP</code>)
   * to return property information for
   * @param propInfo an OUT parameter in which to write the response:
   * an SDLIP XML string of doctype &lt propList &gt, with the
   * STARTS header &lt starts:smeta-attributes &gt embedded inside.
   */
  public void getPropertyInfo (String subcolName, XMLObject propInfo)
    throws SDLIPException {
      System.out.println ("receiving getPropertyInfo request");
      String xmlString = null;
      BackEndLSP backEnd = (BackEndLSP) backEndLSPs.get (subcolName);
      LSPMetaAttributes metaAttributes = null;
      LSPContentSummary contentSummary = null;

      if (backEnd == null) {
        throw new SDLIPException (SDLIPException.INVALID_SUBCOLLECTION_EXC,
                                  "Could not find subcollection: " + subcolName);
      }

      try {
        metaAttributes = backEnd.getMetaAttributes ();
        StringWriter sw = new StringWriter ();
        XMLWriter writer = new XMLWriter (sw, new String[] {"propList"});
        writer.printStartElement("propList", true);
        writer.printNamespaceDeclaration(SDLIP.Namespace);
        writer.printStartElementClose();
        writer.indent();
        metaAttributes.toXML (writer);
        writer.unindent();
        writer.clearNamespaces();
        writer.printEndElement("propList");
        writer.flush();

        xmlString = sw.toString();
        writer.close();
      }
      catch (Exception e) {
        throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC,
                                  e.getMessage());
      }

      propInfo.setString (xmlString);
      System.out.println ("processed getPropertyInfo request");
  }

  /**
   * Implements the <code>sdlip.Metadata.getSubcollectionInfo()</code> method.
   * It writes the response as SDLIP XML into <code>theInterface</code>,
   * which is an OUT parameter. See the SDLIP documentation for more
   * information on how the response is formatted.
   * <p>
   * Basically, each "subcollection" described in the response corresponds
   * to one <code>BackEndLSP</code> that this <code>FrontEndLSP</code>
   * can serve.
   */
  public void getSubcollectionInfo (XMLObject subcolInfo) throws SDLIPException {
    String xmlString = null;
      System.out.println ("receiving getSubcollectionInfo request");
    try {
      StringWriter sw = new StringWriter();
      XMLWriter writer = new XMLWriter (sw, new String[] {"subcolInfo"});

      BackEndLSP[] backEndLSPs = getBackEndLSPs();
      int len = backEndLSPs.length;
      writer.printStartElement ("subcolInfo", true);
      writer.printNamespaceDeclaration(SDLIP.Namespace);
      writer.printStartElementClose();
      writer.indent();
      for (int i = 0 ; i < len ; i++) {
        writer.printStartElement ("subcol");
        writer.indent();
        writer.printStartElement ("subcolName");
        writer.indent();
        writer.println (backEndLSPs[i].getName());
        writer.unindent();
        writer.printEndElement ("subcolName");
        writer.printStartElement ("subcolDesc");
        writer.indent();
        writer.println (backEndLSPs[i].getDescription());
        writer.unindent();
        writer.printEndElement ("subcolDesc");
        writer.printStartElement ("queryLangs");
        writer.indent();
        String langs[] = backEndLSPs[i].getQueryLanguages();
        int len2 = langs.length;
        for (int j = 0 ; j < len2 ; j++) {
          writer.printEmptyElement (langs[j]);
        }
        writer.unindent();
        writer.printEndElement ("queryLangs");
        writer.unindent();
        writer.printEndElement ("subcol");
      }
      writer.unindent();
      writer.printEndElement ("subcolInfo");
      xmlString = sw.toString();
      writer.close();
    }
    catch (Exception e) {
      throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC, e.getMessage());
    }
    subcolInfo.setString (xmlString);
      System.out.println ("processed getSubcollectionInfo request");
  }

  // -------- HELPER METHODS --------
  private String mergeResults (List resultObjs) throws IOException {
    StringWriter sw = new StringWriter ();
    XMLWriter writer =
      new XMLWriter (sw, new String[] {"SearchResult"});
    writer.printStartElement ("SearchResult", true);
    writer.printNamespaceDeclaration (SDLIP.Namespace);
    writer.printStartElementClose();
    writer.indent();

    int docId = 1;
    for (Iterator it = resultObjs.iterator() ; it.hasNext() ; ) {
      LSPResults results = (LSPResults) it.next();
      writer.indent();
      writer.printStartElement("doc");
      writer.printEntireElement("DID", docId++);
      writer.printStartElement ("propList");
      writer.indent();
      results.toXML (writer);
      writer.unindent();
      writer.printEndElement ("propList");
      writer.printEndElement("doc");
      writer.unindent();
    }
    writer.unindent();
    writer.printEndElement ("SearchResult");
    writer.flush();
    String resultString = sw.toString();
    writer.close();

    return resultString;
  }
}


