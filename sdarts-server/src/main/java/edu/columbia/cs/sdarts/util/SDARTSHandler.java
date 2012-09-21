
package edu.columbia.cs.sdarts.util;

import org.xml.sax.AttributeList;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributeListImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A subclass of the standard SAX <code>DefaultHandler</code> that provides
 * extra functionality. It has the following extra features:
 * <ul>
 * <li>Helper methods for removing the namespace declarations from
 * elements and attributes - this is helpful in post-processing output
 * from the SDLIP HTTP/DASL layer
 * <li>Built-in SAX parser - just call {@link #getParser() getParser()}
 * to access it
 * <li>Built-in error handling functions that make use of the
 * <code>org.xml.sax.Locator</code> object.
 * <li>Making all <code>org.xml.sax.DocumentHandler</code> methods abstract
 * again, to force developers to pay attention to them
 * </ul>
 * <p>
 * Both the <a href="http://www.megginson.com/SAX/index.html">main SAX
 * site</a> and the <a href="http://www.java.sun.com/xml">Sun JAXP</a>
 * site have information on SAX, the major framework used by SDARTS
 * to parse XML.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>Modified by</i> <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public abstract class SDARTSHandler extends DefaultHandler {
  // ------------ FIELDS ------------
  //protected Parser        parser;
  protected XMLReader     parser;
  protected Locator       locator;

  /**
   * Constructor automatically creates the parser, and links this
   * handler to the parser.
   * @exception SAXException if something goes wrong
   */
  public SDARTSHandler() throws SAXException {
    //this (XMLParserFactory.getParser());
    this (XMLReaderFactory.createXMLReader());
  }

  //public SDARTSHandler(Parser parser) throws SAXException {
  public SDARTSHandler(XMLReader parser) throws SAXException {
    this.parser = parser;
    //this.parser.setDocumentHandler(this);
    this.parser.setContentHandler(this);
    this.parser.setErrorHandler(this);
  }

  /**
   * @return the SAX parser inside this handler
   */
  //public Parser getParser () {
  public XMLReader getParser () {
    return parser;
  }

  /**
   * Standard SAX method
   */
  public void setDocumentLocator (Locator l) {
    this.locator = l;
  }

  /**
   * Standard SAX method
   */
  public abstract void startElement (String uri, String localName,
      String qName, AttributeList atts) throws SAXException;

  /**
   * Standard SAX method
   */
  public abstract void characters (char[] ch, int start, int length)
    throws SAXException;

  /**
   * Standard SAX method
   */
  public abstract void endElement (String uri, String localName,
      String qName) throws SAXException;


  // -------- ERROR HANDLER INTERFACE --------
  /**
   * Standard SAX method. Passes on the incoming exception.
   * @param exception the exception
   * @exception SAXException the same exception received in the parameter
   */
  public void error (SAXParseException exception) throws SAXException {
    throw exception;
  }

  /**
   * Standard SAX method. Passes on the incoming exception.
   * @param exception the exception
   * @exception SAXException the same exception received in the parameter
   */
  public void fatalError (SAXParseException exception) throws SAXException {
    throw exception;
  }

  /**
   * Standard SAX method. Does not pass on the exception, but prints it
   * to <code>stderr</code>
   * @param exception the exception
   */
  public void warning (SAXParseException exception) throws SAXException {
    System.err.println ("WARNING: " + exception.getMessage());
  }


  // -------- HELPER METHODS --------
  /**
   * Tells what the namespace of qualified XML element name is.
   * @param name the element
   * @return the namespace, if the element has one, or <code>null</code>
   */
  public String getNamespace (String name) {
    int i = name.indexOf(':');
    if (i > 0) {
      return name.substring (0, i);
    }
    return null;
  }

  /**
   * Returns a qualified XML element's name, shorn of its namespace
   * @param name the element
   * @return the universal name for the element, or the same name if
   * it didn't have a namespace qualifier
   */
  public String removeNamespace (String name) {
    int i = name.indexOf(':');
    if (i > 0) {
      return name.substring(i+1, name.length());
    }
    return name;
  }

  /**
   * Goes through a SAX <code>AttributeList</code>, removes the namespace
   * name from each qualified attribute, and returns a new
   * <code>AttributeList</code>. It does not modify the incoming list.
   * @param attrs a SAX <code>AttributeList</code>
   * @return a new list whose names do not have namespace qualifiers
   */
  public AttributeList removeNamespace (AttributeList attrs) {
    AttributeListImpl newAttrs = new AttributeListImpl ();
    int len = attrs.getLength();
    for (int i = 0 ; i < len ; i++) {
      String name  = attrs.getName(i);
      String type  = attrs.getType(i);
      String value = attrs.getValue(i);
      name = removeNamespace (name);
      newAttrs.addAttribute(name, type, value);
    }
    return newAttrs;
  }

  /**
   * Creates a <code>SAXException</code> with the incoming message,
   * and also makes use of the SAX <code>Locator</code> in the
   * exception
   * @param msg the message for the exception
   * @exception automatically throws a new <code>SAXException</code>
   * with the message. If the <code>Locator</code> is set, it sends
   * a <code>SAXParseException</code> using the <code>Locator</code>
   */
  protected void processException (String msg)
      throws SAXException {
    if (locator != null) {
      throw new SAXParseException (msg, locator);
    } else {
      throw new SAXException (msg);
    }
  }

  /**
   * Creates a <code>SAXException</code> with the incoming message,
   * and also makes use of the SAX <code>Locator</code> in the
   * exception
   * @param msg the message for the exception
   * @param e the exception to nest inside this new exception
   * @exception automatically throws a new <code>SAXException</code>
   * with the message. If the <code>Locator</code> is set, it sends
   * a <code>SAXParseException</code> using the <code>Locator</code>
   */
  protected void processException (String msg, SAXException e)
      throws SAXException {
    if (locator != null) {
      throw new SAXParseException (msg, locator, e);
    } else {
      throw e;
    }
  }
}
