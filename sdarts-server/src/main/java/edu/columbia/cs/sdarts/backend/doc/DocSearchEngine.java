package edu.columbia.cs.sdarts.backend.doc;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;

/**
 * An abstract interface representing a search engine used by a
 * {@link edu.columbia.cs.sdarts.backend.doc.DocBackEndLSP DocBackEndLSP} to index and
 * access its collection. The <code>DocBackEndLSP</code> relies on this
 * class to answer all query, meta-attributes, and content-summary requests.
 * <p>
 * If it is using the filesystem for persistence, a search engine should save its index
 * in the same directory as all the other configuration files for a
 * <code>DocBackEndLSP</code>:
 * <code>SDARTS_HOME/config/<i>backEndLSPName</i></code>.
 * <p>
 * SDARTS includes a reference implementation for the search engine,
 * based on the <a href="http://www.lucene.com">Lucene</a> search engine.
 * This is in the {@link edu.columbia.cs.sdarts.backend.doc.lucene} package.
 */
public interface DocSearchEngine {
  /**
   * Set up the search engine.
   * @param sourceName the name of the <code>BackEndLSP</code>
   * @param sourceDescription the description of the <code>BackEndLSP</code>
   * @param config the <code>DocConfig</code> object used by the
   * <code>BackEndLSP</code>.
   */
  public abstract void              initialize (String sourceName,
                                                String sourceDescription,
                                                DocConfig config)
    throws BackEndException;

  /**
   * Perform a query
   * @param query the query
   * @return the results of the query
   */
  public abstract LSPResults        query (LSPQuery query)
    throws BackEndException;

  /**
   * Obtain the meta-attributes for the collection
   * @return the meta-attributes for the collection
   */
  public abstract LSPMetaAttributes getMetaAttributes ()
    throws BackEndException;

  /**
   * Obtain the content summary for the collection
   * @return the meta-attributes for the collection
   */
  public abstract LSPContentSummary getContentSummary ()
    throws BackEndException;
}
