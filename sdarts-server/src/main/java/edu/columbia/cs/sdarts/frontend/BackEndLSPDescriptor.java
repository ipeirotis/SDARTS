
package edu.columbia.cs.sdarts.frontend;

import java.io.IOException;

import edu.columbia.cs.sdarts.common.LSPObject;
import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * Descibes one wrapped subcollection in a SDARTS server configuration.
 * This descriptor is used by the
 * {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP FrontEndLSP} to instantiate a
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}. It is basically an
 * objectified form of the &ltback-end-lsp&gt tag inside of a
 * <code>sdarts_config.xml</code> file. There is one descriptor for
 * each of these tags present in the file, and they are all stored inside the
 * {@link edu.columbia.cs.sdarts.frontend.SDARTSConfig SDARTSConfig} object.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class BackEndLSPDescriptor extends LSPObject {
  private String classname;
  private String name;
  private String description;
  private String[] queryLanguages;

  /**
   * Instantiates a descriptor. Typically, descriptors are instantiated
   * by the {@link edu.columbia.cs.sdarts.frontend.SDARTSConfigBuilder SDARTSConfigBuilder}
   * as it assembles the {@link edu.columbia.cs.sdarts.frontend.SDARTSConfig SDARTSConfig.}
   * @param classname the fully-qualified classname of the
   * <code>BackEndLSP</code> subclass to be instantiated
   * @param name the name by which the back-end collection will be known
   * @param description a description of the back-end collection
   * @param queryLanguages the (computer) query lanagues the back-end
   * subcollection understands
   */
  public BackEndLSPDescriptor (String classname, String name,
                               String description,
                               String[] queryLanguages) {
    this.classname = classname;
    this.name = name;
    this.description = description;
    this.queryLanguages = queryLanguages;
  }

  /**
   * Returns the fully-qualified classname of the
   * <code>BackEndLSP</code> subclass to be instantiated
   * @return the fully-qualified classname of the
   * <code>BackEndLSP</code> subclass to be instantiated
   */
  public String getClassname () {
    return classname;
  }


  /**
   * Returns the name by which the back-end collection will be known
   * @return the name by which the back-end collection will be known
   */
  public String getName () {
    return name;
  }

  /**
   * Returns the description of the back-end collection
   * @return the description of the back-end collection
   */
  public String getDescription () {
    return description;
  }

  /** Returns the (computer) query lanagues the back-end
   * subcollection understands
   * @return the (computer) query lanagues the back-end
   * subcollection understands
   */
  public String[] getQueryLanguages () {
    return queryLanguages;
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.printStartElement("back-end-lsp");
    writer.indent();
    writer.printEntireElement ("classname", classname);
    writer.printEntireElement ("name", name);
    writer.printEntireElement ("description", description);
    int len = queryLanguages.length;
    for (int i = 0 ; i < len ; i++) {
      writer.printEntireElement ("query-language", queryLanguages[i]);
    }
    writer.unindent();
    writer.printEndElement ("back-end-lsp");
  }
}