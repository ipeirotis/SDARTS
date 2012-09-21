package edu.columbia.cs.sdarts.backend.www;

/**
 * A "constants container" that holds the filenames for the stylesheets that
 * should be used with this package. Currently, these are
 * <code>www_query.xsl</code> and <code>www_results.xsl</code>.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by</i> <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public interface WWWConstants {

  /** The fixed filename for the stylesheet used to translate a STARTS XML
   * query into STARTS intermediate form. Currently, this is
   * <code>www_query.xsl</code>.
   */
  public static final String REQUEST_STYLESHEET_FILENAME = "www_query.xsl";

  /**
   * The fixed filename for the stylsheet used to translate an XML-ified
   * version of an HTML page into STARTS intermediate form. Currently, this
   * is <code>www_results</code>.
   */
  public static final String RESULTS_STYLESHEET_FILENAME = "www_results.xsl";

}
