/**
 * An interface representing the back end of the LSP; the wrapper onto
 * the legacy collection. This interface is the starting point for all
 * back-end wrapper developers - they subclass and implement it, so that
 * it can communicate with the {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP} above
 * it. The two classes communicate using
 * {@link edu.columbia.cs.sdarts.common.LSPObject LSPObjects} of various types.
 * <p>
 * The <code>FrontEndLSP</code> can have one or more <code>BackEndLSPs</code>
 * underneath it. These interfaces all look the same to it, and it queries
 * them without regard to internal implementation. Implementation is up
 * to the developer, but guidelines as described below must be followed.
 * <p>
 * @see edu.columbia.cs.sdarts.frontend
 * @see edu.columbia.cs.sdarts.common
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */

package edu.columbia.cs.sdarts.backend;

import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;

public interface BackEndLSP {
  // -------- QUERY --------
  /**
   * Perform a query operation onto the underlying subcollection.
   * @param query the <code>LSPQuery</code> that contains all query information
   * @return the query results, formatted as an <code>LSPResults</code>
   * @exception BackEndException if something goes wrong
   */
  public LSPResults query (LSPQuery query) throws BackEndException;

  /**
   * Retrieve the meta-attributes of the underlying subcollection
   * @return the meta-attributes, formatted as an <code>LSPMetaAttributes</code>
   * @exception BackEndException if something goes wrong
   */
  public LSPMetaAttributes getMetaAttributes () throws BackEndException;

  /**
   * Retrieve the content-summary of the underlying subcollection
   * @return the content-summary, formatted as an <code>LSPContentSummary</code>
   * @exception BackEndException if something goes wrong
   */
  public LSPContentSummary getContentSummary () throws BackEndException;

  /**
   * According to SDLIP, every subcollection must have a name, a description,
   * and one or more (computer) query languages it responds to. This method
   * is used to set these values when the <code>BackEndLSP</code> is first
   * constructed. It should not be called more than once.
   * <p>
   * This is also a good place for the <code>BackEndLSP</code> to load any
   * configuration files, indexes, etc., that it might need, and to configure
   * itself accordingly.
   * <p>
   * @param name the name of the subcollection; this is the same name
   * that should appear in the <code>sdarts_config.xml</code> file when an
   * instance is registered
   * @param description the description of the subcollection
   * @param queryLanguages the (computer) query languages the subcollection
   * responds to
   * @exception BackEndException if something goes wrong
   */
  public void initialize (String name, String description,
                          String[] queryLanguages)
    throws BackEndException;

  /**
   * Return the name of the subcollection
   * @return the name of the subcollection
   */
  public String getName();

  /**
   * Return the description of the subcollection
   * @return the description of the subcollection
   */
  public String getDescription();

  /**
   * Return the (computer) query languages the subcollection
   * responds to
   * @return the (computer) query languages the subcollection
   * responds to
   */
  public String[] getQueryLanguages();
}

