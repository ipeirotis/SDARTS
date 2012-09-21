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
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPMetaAttributesBuilder;
import edu.columbia.cs.sdarts.common.STARTS;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Saves and loads an {@link edu.columbia.cs.sdarts.common.LSPMetaAttributes LSPMetaAttributes}
 * as a flat STARTS XML file. It builds on its superclass, which already knows how
 * to turn the STARTS XML file into a <code>LSPMetaAttributes</code>. In this
 * package, the notion is that since the document collection is stored as text,
 * so should its meta-attributes.<p>
 * <p>
 * The filename under which the content summary
 * is persisted is always the same: it is a constant inside
 * {@link edu.columbia.cs.sdarts.backend.doc.DocConstants DocConstants.}
 * <p>
 * The directory is the same place where the
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} that owns the
 * <code>LSPMetaAttributes</code> keeps its <code>doc_config.xml</code>
 * file: <code>SDARTS_HOME/config/<i>backEndLSPName</i></code>
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class DocMetaAttributesBuilder extends LSPMetaAttributesBuilder {
  /**
   * Save an <code>LSPMetaAttributes</code>.
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPMetaAttributes</code> belongs
   * @param contentSummary the <code>LSPMetaAttributes</code> to save
   * @exception BackEndException if something goes wrong during saving
   */
  public void save (String backEndLSPName, LSPMetaAttributes metaAttributes)
  throws BackEndException {
    String metaAttributesFilename =
      SDARTS.CONFIG_DIRECTORY + File.separator + backEndLSPName +
      File.separator + DocConstants.META_ATTRIBUTES_FILENAME;

    try {
      XMLWriter writer =
        new XMLWriter (new BufferedWriter
                        (new OutputStreamWriter
                          (new FileOutputStream (metaAttributesFilename))),
                      new String[] {STARTS.NAMESPACE_NAME + ":smeta-attributes"}, true );
      metaAttributes.toXML (writer);
      writer.flush();
      writer.close();
    }
    catch (IOException e) {
      throw new BackEndException (e.getMessage());
    }
  }

  /**
   * Load an <code>LSPContentSummary</code>.
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPMetaAttributes</code> belongs
   * @return the <code>LSPMetaAttributes</code> to load
   * @exception BackEndException if something goes wrong during loading
   */
  public LSPMetaAttributes load (String backEndLSPName)
  throws BackEndException {
    String metaAttributesFilename =
      SDARTS.CONFIG_DIRECTORY + File.separator + backEndLSPName +
      File.separator + DocConstants.META_ATTRIBUTES_FILENAME;

    LSPMetaAttributes metaAttributes = null;
      try {
        BufferedReader br =
          new BufferedReader (
            new InputStreamReader (
              new FileInputStream (metaAttributesFilename)));
        metaAttributes = fromXML (br);
      }
      catch (IOException e) {
        throw new BackEndException (e.getMessage());
      }

    return metaAttributes;
  }
}