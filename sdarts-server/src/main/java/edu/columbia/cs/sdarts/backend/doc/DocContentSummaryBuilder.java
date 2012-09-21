package edu.columbia.cs.sdarts.backend.doc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.common.LSPContentSummary;
import edu.columbia.cs.sdarts.common.LSPContentSummaryBuilder;
import edu.columbia.cs.sdarts.common.STARTS;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Saves and loads an {@link edu.columbia.cs.sdarts.common.LSPContentSummary LSPContentSummary}
 * as a flat STARTS XML file. It builds on its superclass, which already knows how
 * to turn the STARTS XML file into a <code>LSPContentSummary</code>. In this
 * package, the notion is that since the document collection is stored as text,
 * so should its content summary.
 * <p>
 * The filename under which the content summary
 * is persisted is always the same: it is a constant inside
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConstants DocConstants.}
 * <p>
 * The directory is the same place where the
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} that owns the
 * <code>LSPContentSummary</code> keeps its <code>doc_config.xml</code>
 * file: <code>SDARTS_HOME/config/<i>backEndLSPName</i></code>
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class DocContentSummaryBuilder extends LSPContentSummaryBuilder {
  /**
   * Save an <code>LSPContentSummary</code>.
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPContentSummary</code> belongs
   * @param contentSummary the <code>LSPContentSummary</code> to save
   * @exception BackEndException if something goes wrong during saving
   */
  public void save (String backEndLSPName, LSPContentSummary contentSummary)
  throws BackEndException {
    String contentSummaryFilename =
      SDARTS.CONFIG_DIRECTORY + File.separator + backEndLSPName + File.separator +
      DocConstants.CONTENT_SUMMARY_FILENAME;

    try {
      BufferedWriter bw =
        new BufferedWriter (
          new OutputStreamWriter (
            new FileOutputStream (contentSummaryFilename), XMLWriter.ENCODING));
      XMLWriter w =
        new XMLWriter (bw,
                       new String[]
                        {STARTS.NAMESPACE_NAME + ":scontent-summary"}, true);
      contentSummary.toXML (w);
      w.flush();
      w.close();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new BackEndException (e.getMessage());
    }
  }

  /**
   * Load an <code>LSPContentSummary</code>.
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPContentSummary</code> belongs
   * @return the <code>LSPContentSummary</code> to load
   * @exception BackEndException if something goes wrong during loading
   */
  public LSPContentSummary load (String backEndLSPName) throws BackEndException {
    String contentSummaryFilename =
      SDARTS.CONFIG_DIRECTORY + File.separator + backEndLSPName + File.separator +
      DocConstants.CONTENT_SUMMARY_FILENAME;

      LSPContentSummary contentSummary = null;
      try {
        BufferedReader br =
          new BufferedReader (
            new InputStreamReader (
              new FileInputStream (contentSummaryFilename)));
        contentSummary = fromXML (br);
      }
      catch (IOException e) {
        throw new BackEndException (e.getMessage());
      }

    return contentSummary;
  }
}