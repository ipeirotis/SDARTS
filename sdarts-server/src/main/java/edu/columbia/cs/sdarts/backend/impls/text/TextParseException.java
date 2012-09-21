

package edu.columbia.cs.sdarts.backend.impls.text;

import java.io.File;

import edu.columbia.cs.sdarts.backend.BackEndException;
import gnu.regexp.RE;

/**
 * Used to indicate that something has gone wrong with the regular-
 * expression based parsing of a document.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class TextParseException extends BackEndException {
  public TextParseException (String message, File file, String fieldName,
                             RE start, RE end) {
    super (message + "\n" +
           "Filename: " + file.getPath() + "\n" +
           "Field name: " + fieldName + "\n" +
           "Start regexp: " + start + "\n" +
           "End regexp: " + end);
  }
}