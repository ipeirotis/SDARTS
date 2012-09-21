package edu.columbia.cs.sdarts.frontend;

import edu.columbia.cs.sdarts.common.LSPObject;
import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * The object that SDARTS uses to configure itself upon start-up.
 * It is an objectified version of the <code>sdarts_config.xml</code>
 * file that is read in. This descriptor tells SDARTS where to find
 * the STARTS DTD, where to find the SDLIP DTD, and the characteristics
 * of any {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSPs} it is serving.
 * <p>
 * Typically, this descriptor is created by the
 * {@link edu.columbia.cs.sdarts.frontend.SDARTSConfigBuilder SDARTSConfigBuilder}, which
 * reads in the configuration file. For more information about this
 * configuration file, see the
 * <a href="http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd">
 * sdarts_config DTD</a>.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class SDARTSConfig extends LSPObject {
  private String startsDtdURL;
  private String sdlipDtdURL;
  private BackEndLSPDescriptor[] lspDescriptors;


  /**
   * Creates the configuration object.
   * @param sdlipDtdURL where to find the SDLIP DTD
   * @param startsDtdURL where to find the STARTS DTD
   * @param descriptors objects describing each <code>BackEndLSP</code>
   * the SDARTS server is fronting.
   */
  public SDARTSConfig (String sdlipDtdURL, String startsDtdURL,
                       BackEndLSPDescriptor[] descriptors) {
    this.sdlipDtdURL = sdlipDtdURL;
    this.startsDtdURL = startsDtdURL;
    this.lspDescriptors = descriptors;
  }

  /**
   * Returns the URL for the STARTS DTD
   * @return the URL for the STARTS DTD
   */
  public String getStartsDtdURL () {
    return startsDtdURL;
  }

  /**
   * Returns the URL for the SDLIP DTD
   * @return the URL for the SDLIP DTD
   */
  public String getSdlipDtdURL () {
    return sdlipDtdURL;
  }

  /**
   * Returns objects describing each <code>BackEndLSP</code>
   * the SDARTS server is fronting.
   * @return objects describing each <code>BackEndLSP</code>
   * the SDARTS server is fronting.
   */
  public BackEndLSPDescriptor[] getBackEndLSPDescriptors() {
    return lspDescriptors;
  }

  public void toXML (XMLWriter writer) throws java.io.IOException {
    writer.setDefaultFormat();
    writer.printStartElement("sdarts-config");
    writer.indent();
    writer.printStartElement("sdlip-dtd-url");
    writer.print(sdlipDtdURL);
    writer.printStartElement("starts-dtd-url");
    writer.print(startsDtdURL);
    writer.printEndElement("dtd-url");
    int len = lspDescriptors.length;
    for (int i = 0 ; i < len ; i++) {
      lspDescriptors[i].toXML (writer);
    }
    writer.unindent();
    writer.printEndElement("sdarts-config");
    writer.flush();
  }
}
