package edu.columbia.cs.sdarts.dbselection.util;

/*
 * MyErrorHandler.java
 *
 * By: Sergey Sigelman (ss1792@cs.columbia.edu)
 */

// JAXP packages
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

  // Error handler to report errors and warnings
  // TAKEN STRAIGHT FROM JAXP SAMPLES
public class MyErrorHandler implements ErrorHandler
{
  /** Error handler output goes here */
  private PrintWriter out;

  /**
   * default constructor
   * print error to System.err
   */
  public MyErrorHandler () {
    this(new PrintWriter(new OutputStreamWriter(System.err)));
  }

  public MyErrorHandler (PrintWriter out) {this.out = out;}

  /**
   * Returns a string describing parse exception details
   */
  private String getParseExceptionInfo(SAXParseException spe) {
      String systemId = spe.getSystemId();
      if (systemId == null) {
          systemId = "null";
      }
      String info = "URI=" + systemId +
          " Line=" + spe.getLineNumber() +
          ": " + spe.getMessage();
      return info;
  }

  // The following methods are standard SAX ErrorHandler methods.
  // See SAX documentation for more info.

  public void warning(SAXParseException spe) throws SAXException {
      out.println("Warning: " + getParseExceptionInfo(spe));
  }

  public void error(SAXParseException spe) throws SAXException {
      String message = "Error: " + getParseExceptionInfo(spe);
      throw new SAXException(message);
  }

  public void fatalError(SAXParseException spe) throws SAXException {
      String message = "Fatal Error: " + getParseExceptionInfo(spe);
      throw new SAXException(message);
  }
}
