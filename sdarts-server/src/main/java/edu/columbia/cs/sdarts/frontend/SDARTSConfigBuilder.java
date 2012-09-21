
package edu.columbia.cs.sdarts.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.AttributeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sdlip.SDLIPException;
import edu.columbia.cs.sdarts.util.IntStack;
import edu.columbia.cs.sdarts.util.SDARTSHandlerBase;
import edu.columbia.cs.sdarts.util.UnsynchStack;


/**
 * Builds a {@link edu.columbia.cs.sdarts.frontend.SDARTSConfig SDARTSConfig} descriptor
 * by reading in XML, in the <code>sdarts_config.xml</code> file, which is
 * in the {@link edu.columbia.cs.sdarts.frontend.SDARTS#CONFIG_DIRECTORY} directory.
 * This class's one method is static and the class need not be instantiated.
 * <p>
 * The XML must conform to the
 * <a href="http://www.cs.columbia.edu/~dli2test/dtd/sdarts_config.dtd">
 * starts_config DTD </a>.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class SDARTSConfigBuilder {

  /** The constant name for the SDARTS configuration file,
   * <code>sdarts_config.xml</code> is stored here. Change this
   * value if the filename should ever change.
   */
  public static final String CONFIG_FILENAME = "sdarts_config.xml";
  private static SDARTSConfig config = null;

  /**
   * Create a <code>SDARTSConfig</code> object by reading XML
   * in the <code>sdarts_config.xml</code> file, which is
   * in the {@link edu.columbia.cs.sdarts.frontend.SDARTS#CONFIG_DIRECTORY} directory.
   * @exception SDLIPException if something goes wrong
   */
  public static SDARTSConfig fromXML () throws SDLIPException 
  {
      try 
      {
      	// cache configuration for better performance 
      	if ( config == null )
      	{
			BufferedReader reader =
	          new BufferedReader (
	            new InputStreamReader (
	              new FileInputStream
	                (SDARTS.CONFIG_DIRECTORY + File.separator + CONFIG_FILENAME)));
	        SCBHandler handler = new SCBHandler();
	        config = handler.parse (reader);
      	}
        return config;
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC,
                                  e.getMessage());
      }
  }

  private static class SCBHandler extends SDARTSHandlerBase {
    // ------------ FIELDS ------------
    // -------- CONSTANTS --------
    private static final int UNKNOWN = -1;
    private static final int SDARTS_CONFIG  = 0;
    private static final int SDLIP_DTD_URL  = 1;
    private static final int STARTS_DTD_URL = 2;
    private static final int BACK_END_LSP   = 3;
    private static final int CLASSNAME      = 4;
    private static final int NAME           = 5;
    private static final int DESCRIPTION    = 6;
    private static final int QUERY_LANGUAGE = 7;

    // -------- PARSING --------
    private IntStack state = new IntStack();
    private SDARTSConfig sdartsConfig;
    private String sdlipDtdURL;
    private String startsDtdURL;
    private UnsynchStack backEndLSPDescriptors = new UnsynchStack();
    private String classname;
    private String lspName;
    private String description;
    private UnsynchStack queryLanguages = new UnsynchStack();
    private String configPath;



    public SCBHandler () throws SAXException, javax.xml.parsers.ParserConfigurationException 
    {
    }



    // ------------ METHODS ------------
    // -------- CONSTRUCTOR --------
    SDARTSConfig parse (Reader reader) throws IOException, SAXException 
    {
      super.parse (new InputSource (reader));
	  super.EnableSchemaChecking(true);
	  super.EnableValidation(true);
      return sdartsConfig;
    }


    // -------- DOCUMENT HANDLER INTERFACE --------
    public void startElement (String name, AttributeList atts)
	throws SAXException {
      if (name.equals ("sdarts-config")) {
        state.push (SDARTS_CONFIG);
      }
      else if (name.equals ("sdlip-dtd-url")) {
        state.push (SDLIP_DTD_URL);
      }
      else if (name.equals ("starts-dtd-url")) {
        state.push (STARTS_DTD_URL);
      }
      else if (name.equals ("back-end-lsp")) {
        state.push (BACK_END_LSP);
      }
      else if (name.equals ("classname")) {
        state.push (CLASSNAME);
      }
      else if (name.equals ("name")) {
        state.push (NAME);
      }
      else if (name.equals ("description")) {
        state.push (DESCRIPTION);
      }
      else if (name.equals ("query-language")) {
        state.push (QUERY_LANGUAGE);
      }
      else {
        throw new SAXException ("unknown element");
      }
    }

    public void characters (char[] ch, int start, int length)
	throws SAXException {
      String value = new String (ch, start, length);
      int currentState = state.peek();
      switch (currentState) {
        case SDLIP_DTD_URL:
          sdlipDtdURL = value;
        break;

        case STARTS_DTD_URL:
          startsDtdURL = value;
        break;

        case CLASSNAME:
        	if (classname != null)
          		classname += value;
          	else
				classname = value;
        break;

        case NAME:
          lspName = value;
        break;

        case DESCRIPTION:
          description = value;
        break;


        case QUERY_LANGUAGE:
          queryLanguages.push (value);
        break;
      }
    }

    public void endElement (String name) throws SAXException {
      int currentState = state.pop();
      switch (currentState) {
        case SDARTS_CONFIG:
          BackEndLSPDescriptor[] descriptors =
            (BackEndLSPDescriptor[]) backEndLSPDescriptors.toArray (new BackEndLSPDescriptor[0]);
          sdartsConfig = new SDARTSConfig (sdlipDtdURL, startsDtdURL,
                                           descriptors);
          backEndLSPDescriptors.clear();
          sdlipDtdURL = null;
          startsDtdURL = null;
        break;

        case BACK_END_LSP:
          String[] queryLangs =
            (String[]) queryLanguages.toArray (new String[0]);
          queryLanguages.clear();
          BackEndLSPDescriptor descriptor =
            new BackEndLSPDescriptor (classname, lspName, description, queryLangs);
          classname = null;
          name = null;
          description = null;
          backEndLSPDescriptors.push (descriptor);
        break;
      }
    }
  }
}
