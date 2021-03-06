

package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Represents the "sqresults" header, as defined in STARTS 1.0.
 * This object is generated by the
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}, and returned
 * as a respone to a query from the
 * {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP FrontEndLSP}.
 * <p>
 * An <code>LSPResults</code> contains the following elements:<br>
 * <ul>
 * <li>The actual filter used in the query
 * ({@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter})
 * <li>The actual ranking used in the query
 * ({@link edu.columbia.cs.sdarts.common.LSPFilter LSPRanking})
 * <li>The name of the <code>BackEndLSP</code> where these results are coming
 * from ({@link edu.columbia.cs.sdarts.common.LSPSource LSPSource})
 * <li>The documents in the response
 * ({@link edu.columbia.cs.sdarts.common.LSPDoc LSPDoc})
 * </ul>
 * If the collection supports the ranking and filter that were requested
 * in the incoming query, the back end developer can simply pass these
 * same objects into the <code>LSPResults</code> constructor. However,
 * if only a subset is supported, the developer must instantiate new
 * filters and rankings representing what actually was used, and pass
 * them in. See the {@link edu.columbia.cs.sdarts.backend.QueryProcessor} and
 * {@link edu.columbia.cs.sdarts.backend.StandardQueryProcessor} for structures that
 * help in doing this.
 * <p>
 * The interface is designed so that the <code>LSPResults</code> is
 * instantiated with the filter, ranking, and source. Documents can
 * be added to it incrementally using the <code>addDoc()</code> method.<p>
 * There are accessor methods for finding out what is in the results but
 * in general these are not used. Usually, the framework will just call
 * the <code>toXML()</code> method since that is the only way the framework
 * reads the results.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class LSPResults extends LSPObject {
  // ------------ FIELDS ------------
  private LSPFilter    filter;
  private LSPRanking   ranking;
  private LSPSource    source;
  private LSPDoc[]     docs;
  private int 		   numavailable;

  private static final String version = "Starts 1.0";
  private static final int BATCH_SIZE = 500;


  // ------------ METHODS ------------
  // -------- CONSTRUCTOR --------
  /**
   * Creates a new instance.
   * @param filter the actual filter that was used in the query
   * @param ranking the actual ranking that was used in the query
   * @param source the name of the back end these results come from
   * @param numavailable the number of results that would be returned
   *  if we were asking for <i>all</i> results.
   */
  public LSPResults (LSPFilter filter, LSPRanking ranking,
                     LSPSource source, LSPDoc[] docs, int numavailable) {
    this.filter  = filter;
    this.ranking = ranking;
    this.source  = source;
    this.docs    = docs;
    this.numavailable = numavailable;
  }


  /**
   * Return the actual filter used in the query
   * @return the actual filter used in the query
   */
  public LSPFilter getFilter () {
    return filter;
  }

  /**
   * Return the actual ranking used in the query
   * @return the actual ranking used in the query
   */
  public LSPRanking getRanking () {
    return ranking;
  }

  /**
   * Return the source where these results come from. This
   * is just the name of the <code>BackEndLSP</code>
   * generating the results.
   * @return the source where these results come from
   */
  public LSPSource getSource () {
    return source;
  }

  /**
   * Return the documents in the result set
   * @return the documents in the result set
   */
  public LSPDoc[] getDocs () {
    return docs;
  }

  /**
   * Return the number of documents in the result set
   * @return the number of documents in the result set
   */
  public int getNumDocs () {
    return docs.length;
  }

  /**
   * Return the number of documents from the source that
   * matched the search criteria
   *
   */
   public int getNumAvailable() {
	   return numavailable;
   }

  /**
   * Return the current version of the protocol. Fixed at
   * "Starts 1.0".
   * @return the current version of the protocol. Fixed at
   * "Starts 1.0".
   */
  public String getVersion () {
    return version;
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.printStartElement ("sqresults", true);
    writer.printNamespaceDeclaration
      (STARTS.NAMESPACE_NAME, STARTS.NAMESPACE_VALUE);
	writer.printAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	writer.printAttribute("xsi:schemaLocation", STARTS.NAMESPACE_VALUE + " " + writer.GetDocTypeMapEntry(STARTS.NAMESPACE_NAME + ":smeta-attributes"));
    writer.printAttribute ("version", version);
    writer.printAttribute ("numdocs", getNumDocs());
    writer.printAttribute ("numavailable", getNumAvailable());
    writer.printStartElementClose();

    writer.indent();
    if (filter != null) {
      filter.toXML (writer);
    }
    if (ranking != null) {
      ranking.toXML (writer);
    }
    source.toXML (writer);

    int numDocs = docs.length;
    int iterations = 0;
    for (int i = 0 ; i < numDocs ; i++) {
      docs[i].toXML (writer);
      if (++iterations == BATCH_SIZE) {
        writer.flush();
        iterations = 0;
      }
    }
    writer.unindent();
    writer.printEndElement("sqresults");
    writer.exitNamespace();
    writer.flush();
  }
}
