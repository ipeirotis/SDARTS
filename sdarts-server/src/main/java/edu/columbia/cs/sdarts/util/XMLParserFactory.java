
package edu.columbia.cs.sdarts.util;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;

// Uncomment this if switching to SAX
//import javax.xml.parsers.*;

/**
 * Generates a SAX XML parser. This isolates the often-library-specific
 * way in which the parser is generated, so that it can easily be
 * changed. For example, SDARTS currently uses the Microstar XML parser
 * included with SDLIP. However, should this change in the future
 * (a good idea, since Microstar no longer exists, and Sun has the
 * standard JAXP parser), only this class need be altered. Use this
 * class to get any SAX parser needed.
 * <p>
 * Both the <a href="http://www.megginson.com/SAX/index.html">main SAX
 * site</a> and the <a href="http://www.java.sun.com/xml">Sun JAXP</a>
 * site have information on SAX, the major framework used by SDARTS
 * to parse XML.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class XMLParserFactory {
  /**
   * Create and return a SAX XML parser
   * @return the parser
   * @exception SAXException if the parser cannot be generated
   */
  public static Parser getParser () throws SAXException {
    // Sun implementation. Can't use becaues SDLIP distribution
    // is incompatible with JAXP.
    /*
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setValidating (true);
      return parserFactory.newSAXParser().getParser();
    }
    catch (ParserConfigurationException e) {
      throw new SAXException (e);
    }
    */

      // Microstar Implementation
      Parser p = null;
      try {
      	p = new org.apache.xerces.parsers.SAXParser(); 
        //ParserFactory.makeParser ("com.microstar.xml.SAXDriver");
      }
      catch (Exception e) {
        throw new SAXException (e);
      }
      return p;
  }

  private XMLParserFactory() {}
}
