
package edu.columbia.cs.sdarts.backend.doc.lucene;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.backend.doc.DocContentSummaryBuilder;
import edu.columbia.cs.sdarts.backend.doc.DocMetaAttributesBuilder;
import edu.columbia.cs.sdarts.backend.doc.DocSearchEngine;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;

/**
 * An implementation of
 * {@link edu.columbia.cs.sdarts.backend.doc.DocSearchEngine DocSearchEngine} that
 * utilizes the <a href="http://www.lucene.com">Lucene</a> search engine
 * to index and access a local collection.
 * <p>
 * This class is initialized first via a
 * {@link edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum DocumentEnum} passed to its
 * constructor - this tells the class where the documents are and how to
 * parse them. Second, it is then initialized via the standard
 * <code>initialize()</code> method that all <code>DocSearchEngines</code>
 * have. So be careful when creating this class, remember that it needs
 * a little more than just the <code>initialize()</code> method.
 * <p>
 * This class is responsible for generating and saving the
 * meta-attributes file and content-summary file, using the
 * {@link edu.columbia.cs.sdarts.backend.doc.DocMetaAttributesBuilder DocMetaAttributesbuilder}
 * and
 * {@link edu.columbia.cs.sdarts.backend.doc.DocContentSummaryBuilder DocContentSummaryBuilder}.
 * It also generates and stores the index. It uses the
 * {@link edu.columbia.cs.sdarts.backend.doc.lucene.LuceneSetup LuceneSetup} class
 * to accomplish this.*
 * <p>
 * The search engine also contains within it an instance of
 * {@link edu.columbia.cs.sdarts.backend.doc.lucene.LuceneQueryProcessor LuceneQueryProcessor},
 * which it uses to fulfill all queries.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LuceneSearchEngine implements DocSearchEngine {
  private DocumentEnum          documentEnum;
  private LSPMetaAttributes     metaAttributes;
  private LuceneQueryProcessor  queryProcessor;
  private String                backEndLSPName;


  /**
   * Create a new search engine, and give it a <code>DocumentEnum</code>
   * as an interface onto the document collection.
   * @param documentEnum the <code>DocumentEnum</code> to use
   */
  public LuceneSearchEngine (DocumentEnum documentEnum) {
    this.documentEnum = documentEnum;
  }

  /**
   * Standard initialization
   * @param sourceName the name of the <code>BackEndLSP</code> using this
   * engine
   * @param sourceDescription the collection description
   * @param docConfig the <code>DocConfig</code> object used by the
   * <code>BackEndLSP</code>
   */
  public void initialize (String backEndLSPName,
                          String sourceDescription,
                          DocConfig docConfig)
                          throws BackEndException {
    this.backEndLSPName = backEndLSPName;

    if (docConfig.reIndex()) {
      LuceneSetup.
          setup(backEndLSPName, sourceDescription, true, docConfig, documentEnum);
    }
    // done with this either way, free up for gc
    documentEnum = null;

    queryProcessor =
      new LuceneQueryProcessor (backEndLSPName, getMetaAttributes());
  }


  /**
   * Uses the Lucene search engine to perform the query.
   * @param query the query
   * @return the results
   * @exception BackEndException if something goes wrong
   */
  public LSPResults query(LSPQuery query) throws BackEndException {
    LSPResults results = queryProcessor.query (query);
    return results;
  }

  /**
   * Lazily instantiates and caches the meta attributes for
   * the collection
   * @return the collection's meta attributes
   */
  public LSPMetaAttributes getMetaAttributes() throws BackEndException {
    if (metaAttributes == null) {
      DocMetaAttributesBuilder builder = new DocMetaAttributesBuilder ();
      metaAttributes = builder.load(backEndLSPName);
    }
    return metaAttributes;
  }

  /**
   * Reads the content summary file from storage, and
   * returns it. Does not cache, as content summaries tend
   * to be huge.
   * @return the collection's content summary
   */
  public LSPContentSummary getContentSummary() throws BackEndException {
    LSPContentSummary contentSummary = null;
    DocContentSummaryBuilder builder = new DocContentSummaryBuilder ();
    contentSummary = builder.load(backEndLSPName);
    return contentSummary;
  }
}
