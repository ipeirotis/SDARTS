package edu.columbia.cs.sdarts.backend.doc;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.BackEndLSPAdapter;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;

/**
 * An abstract {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} designed to
 * use a search engine to index and access a collection of local, unindexed
 * documents.
 * <p>
 * A <code>DocBackEndLSP</code>:
 * loads its configuration file, <code>doc_config.xml</code>, from
 * the directory <code>SDARTS_HOME/config/<i>backEndLSPName</i></code>.
 * (See {@link edu.columbia.cs.sdarts.backend.doc.DocConfigBuilder DocConfigBuilder}.)
 * <p>
 * An implementation of <code>DocBackEndLSP</code> should save
 * and load its meta-attributes and content-summary files in the
 * same directory, using
 * {@link edu.columbia.cs.sdarts.backend.doc.DocMetaAttributesBuilder DocMetaAttributesbuilder}
 * and
 * {@link edu.columbia.cs.sdarts.backend.doc.DocContentSummaryBuilder DocContentSummaryBuilder}
 * It should also use some kind of search engine to index and access the
 * collection. The abstract interface for this search engine is specified in
 * {@link edu.columbia.cs.sdarts.backend.doc.DocSearchEngine DocSearchEngine}.
 * <p>
 * To make your own implementation of this class, override the
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConfig#getSearchEngine() getSearchEngine()}
 * method. In general, the search engine usually is responsible also for
 * load and saving the meta-attributes and content-summary, using the
 * <code>DocMetaAttributesBuilder</code> and <code>DocContentSummaryBuilder</code>
 * mentioned above.
 * <p>
 * Two reference implementations of this class are:
 * <ul>
 * <li>{@link edu.columbia.cs.sdarts.backend.impls.text.TextBackEndLSP TextBackEndLSP} -
 * uses the search engine from {@link edu.columbia.cs.sdarts.backend.doc.lucene} to serve
 * a collection of plain text documents
 * <li>{@link edu.columbia.cs.sdarts.backend.impls.xml.XMLBackEndLSP XMLBackEndLSP} -
 * uses the search engine from {@link edu.columbia.cs.sdarts.backend.doc.lucene} to serve
 * a collection of XML documents
 * </ul>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public abstract class DocBackEndLSP extends BackEndLSPAdapter {
  private DocSearchEngine searchEngine;


  /**
   * Sets the basic attributes of the collection, loads up the
   * <code>TextConfig</code> and performs indexing,
   * and meta-attributes and content-summary construction if necessary.
   * @param name the name of the subcollection
   * @param description the description of the subcollection
   * @param queryLanguages the (computer) query languages the subcollection
   * responds to
   * @exception BackEndException if something goes wrong
   */
  public void initialize (String name, String description,
                          String[] queryLanguages)
    throws BackEndException {
      super.initialize (name, description, queryLanguages);

      // Load configuration file doc_config.xml
      DocConfig docConfig;
      try {
        docConfig = DocConfigBuilder.load(name);
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new BackEndException (e.getMessage());
      }

      // Set up search engine
      searchEngine = getSearchEngine (docConfig);
  }

  /**
   * Returns an initialized and working search engine to fulfill query,
   * meta-attributes, and content-summary requests.
   * Override this method to create your own concrete implementation of
   * this class. This method gets called by the <code>initialize()</code>
   * method, and should not need to be called directly.
   * @param config the <code>DocConfig</code> object used to configure
   * the <code>DocBackEndLSP</code>
   * @return a search engine
   * @exception BackEndException if something goes wrong
   */
  public abstract DocSearchEngine getSearchEngine (DocConfig config)
    throws BackEndException;

  /**
   * Uses the Lucene search engine to perform the query.
   * @param query the query
   * @return the results
   * @exception BackEndException if something goes wrong
   */
  public LSPResults query(LSPQuery query) throws BackEndException {
    LSPResults results = searchEngine.query (query);
    return results;
  }

  /**
   * Lazily instantiates and caches the meta attributes for
   * the collection
   * @return the collection's meta attributes
   */
  public LSPMetaAttributes getMetaAttributes() throws BackEndException {
    return (searchEngine.getMetaAttributes());
  }

  /**
   * Reads the content summary file from storage, and
   * returns it. Does not cache, as content summaries tend
   * to be huge.
   * @return the collection's content summary
   */
  public LSPContentSummary getContentSummary() throws BackEndException {
    return (searchEngine.getContentSummary());
  }
}