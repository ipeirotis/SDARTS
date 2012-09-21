

package edu.columbia.cs.sdarts.backend.impls.xml;

import java.io.File;

import org.xml.sax.SAXException;

import edu.columbia.cs.sdarts.backend.BackEndException;

/**
 * Used to indicate that something has gone wrong with the XSL-based
 * parsing of a document.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class XMLParseException extends BackEndException {
  public XMLParseException (String message, File file, String fieldName,
                            SAXException e) {
    super (message + "\n" +
           "Filename: " + file.getPath() + "\n" +
           "Field name: " + fieldName + "\n" +
           "SAXException: " + e.getMessage());
  }
}