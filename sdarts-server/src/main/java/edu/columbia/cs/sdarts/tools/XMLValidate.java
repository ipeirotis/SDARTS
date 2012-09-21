

package edu.columbia.cs.sdarts.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Used inside the <code>xmlvalidate.sh</code> script. This
 * script will validate any XML document that has a DTD in
 * its &lt;!DOCTYPE...&gt; tag. There is an optional verbose
 * mode that will print each tag and its contents, as well.
 *
 * Here are the parameters that should be passed to the <code>main()</code> method:
 * <table>
 * <tr><td><b>Argument</b></td><td><b>User-entered / hidden</b></td><td><b>Description</b></td><tr>
 * <tr><td><i>scriptname</i></td><td>hidden</td><td>Name of calling script, so a nice usage string
 * can be printed</td></tr>
 * <tr><td>-v</td><td>user-entered (optional)</td><td>verbose mode</td></tr>
 * <tr><td><i>filename</i></td><td>user-entered</td><td>name of XML file to validate</td></tr>
 * </table>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class XMLValidate {

  /**
   * The <code>main()</code> method. See above for parameters.
   */
  public static void main (String args[]) {
    String scriptName = null;
    boolean verbose   = false;
    String documentFilename = null;

    try {
      if (args.length == 2) {
        scriptName       = args[0];
        documentFilename = args[1];
      }
      else if (args.length == 3) {
        scriptName       = args[0];
        if (!args[1].equals("-v")) {
          throw new Exception();
        }
        verbose          = true;
        documentFilename = args[2];
      }
      else {
        throw new Exception();
      }
    }
    catch (Exception e) {
      usage (scriptName);
      System.exit(1);
    }

    try {
      SAXParser parser = new SAXParser();
      parser.setFeature("http://xml.org/sax/features/validation", true);
      if (verbose) {
        XVHandler handler = new XVHandler();
        parser.setDocumentHandler(handler);
        parser.setErrorHandler(handler);
      }

      parser.parse (new InputSource
        (new BufferedReader
          (new InputStreamReader
            (new FileInputStream (documentFilename)))));
    }
    catch (Exception e) {
      System.err.println (e.getMessage());
      System.exit(1);
    }
    System.out.println (documentFilename + " is valid.");
  }

  private static void usage (String scriptName) {
    if (scriptName == null) {
      scriptName = "xmlvalidate.sh";
    }

    String usageString =
      "Usage: " + scriptName + " [-v] <documentName>\n" +
      "Where:\n" +
      "-v if present, verbose output\n" +
      "<documentName> is the name of the XML document to process\n";

    System.err.println (usageString);
  }

  private static class XVHandler extends HandlerBase {
    public void startElement (String name, AttributeList attrs)
      throws SAXException {
        System.out.println ("Start Element: " + name);
        if (attrs != null) {
          int len = attrs.getLength();
          for (int i = 0 ; i < len ; i++) {
            System.out.print   ("Attribute: ");
            System.out.println (attrs.getName(i) + "=" + attrs.getValue(i));
          }
        }
    }

    public void characters (char[] ch, int start, int end)
      throws SAXException {
        String val = new String (ch,start,end).trim();
        System.out.println ("Characters: " + val);
    }

    public void endElement (String name) throws SAXException {
      System.out.println ("End Element: " + name);
    }

    public void error (SAXParseException exception)
      throws SAXException {
        System.err.println(exception.getMessage());
        throw exception;
    }

    public void fatalError(SAXParseException exception)
      throws SAXException {
        System.err.println(exception.getMessage());
        throw exception;
    }

    public void warning(SAXParseException exception)
      throws SAXException {
        System.err.println(exception.getMessage());
    }
  }
}
