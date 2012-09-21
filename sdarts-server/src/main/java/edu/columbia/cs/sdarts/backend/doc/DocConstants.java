package edu.columbia.cs.sdarts.backend.doc;


/**
 * A "constants container" for various constants related to text document
 * parsing. These are all filenames; the <code>sdarts.backend.doc</code>
 * package dictates that all indexes, meta-attributes, and content-summaries
 * be saved with constant filenames. Also, the configuration for a
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} using the text package
 * must be called <code>text_config.xml</code>.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class DocConstants {
  /** The index is always saved as "index" */
  public static final String INDEX_FILENAME = "index";

  /** The configuration is always saved as "doc_config.xml" */
  public static final String CONFIG_FILENAME = "doc_config.xml";

  /** The stylesheet is always saved as "doc_style.xsl" */
  public static final String STYLESHEET_FILENAME = "doc_style.xsl";

  /** The meta-attributes are always saved as "meta_attributes.xml" */
  public static final String META_ATTRIBUTES_FILENAME = "meta_attributes.xml";

  /** The content-summary is always saved as "content_summary.xml" */
  public static final String CONTENT_SUMMARY_FILENAME = "content_summary.xml";


  private DocConstants() {}
}
