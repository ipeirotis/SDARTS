
package edu.columbia.cs.sdarts.backend.impls.xml;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.doc.DocBackEndLSP;
import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.backend.doc.DocSearchEngine;
import edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum;
import edu.columbia.cs.sdarts.backend.doc.lucene.LuceneSearchEngine;

/**
 * A fully-implemented <code>BackEndLSP</code> that uses the
 * <a href="http://www.lucene.com">Lucene</a> search engine to access
 * an unindexed, XML document collection. It builds on classes from
 * the {@link edu.columbia.cs.sdarts.backend.doc} package, using the
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConfig DocConfig} descriptor to
 * tell it where to find the documents to parse, and some of the parsing
 * characteristics. In addition, it uses an administrator-written
 * XSL stylsheet, <code>doc_style.xsl</code>, to extract fields from the
 * document. To learn how to write such a stylsheet, refer to the
 * SDARTS design document and the sample <code>doc_style.xsl</code> stylesheet
 * in the SDARTS distribution.
 * <p>
 * If the <code>DocConfig</code>'s <code>reIndex</code> property is
 * <code>true</code>, then upon initialization, this <code>BackEndLSP</code>
 * will index the collection, and construct and store the meta-attributes and
 * content-summary. If not, it is assumed that some offline script has
 * done this (see the <code>xmlsetup.sh</code> script in the SDARTS
 * distribution), or that the collection has not changed since the last
 * time it was indexed.
 * <p>
 * This class uses and produces the following files, all of them in the
 * <code>SDARTS_HOME/config/<i>backEndLSPName</i></code> directory:
 * <ul>
 * <li><code>doc_config.xml</code> - the standard configuration file
 * for any <code>DocBackEndLSP</code>. Written by the administrator.
 * <li><code>doc_style.xsl</code> - tells SDARTS how to parse and extract
 * fields from an XML document in the collection.
 * <li><code>/index</code> - a subdirectory containing the Lucene index
 * <li><code>meta-attributes.xml</code> - the meta-attributes for the
 * collection (generated, but can also be edited by hand)
 * <li><code>content-summary.xml</code> - the content-summary for the
 * collection (generated)
 * </ul>
 * <p>
 * It should be stressed that this whole package can be treated as a black
 * box. The programmer need not write new code to wrap a collection, but
 * only alter the <code>doc_config.xml</code> and
 * <code>doc_style.xsl</code> files describing it.
 *
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class XMLBackEndLSP extends DocBackEndLSP {
  /**
   * Builds a Lucene search engine and returns it. It creates a
   * {@link edu.columbia.cs.sdarts.backend.impls.xml.XMLDocumentEnum XMLDocumentEnum}
   * to help set up the search engine.
   * @param config the configuration object for this <code>BackEndLSP</code>
   * @return the searche engine
   */
  public DocSearchEngine getSearchEngine (DocConfig config) throws BackEndException {
        DocumentEnum de = new XMLDocumentEnum ();

		// since config will now only be used if
		// re-indexing is requested, do not initialize
		// it otherwise
        if(config.reIndex()) {
			de.initialize (config);
		}

        LuceneSearchEngine engine = new LuceneSearchEngine (de);
        engine.initialize (getName(), getDescription(), config);
        return engine;
  }
}
