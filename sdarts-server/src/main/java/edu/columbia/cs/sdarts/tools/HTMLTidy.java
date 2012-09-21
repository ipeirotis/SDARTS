
package edu.columbia.cs.sdarts.tools;

import java.io.File;
import java.io.FileInputStream;

import org.w3c.tidy.Tidy;

/**
 * Called from the <code>htmltidy.sh</code> script. Given an HTML document,
 * it will produce a version that has been turned into XML (not XHTML) by
 * <a href="http://www.w3.org/People/Raggett/tidy/">HTML Tidy</a>, just
 * as the {@link edu.columbia.cs.sdarts.backend.www} package does, in order to see how
 * a web page read by that framework would look before a stylesheet is applied
 * to it. This tool is useful when developing <code>www_results.xsl</code>
 * files. The output of this script should be piped to a file, and then run
 * through the <code>xsltest.sh</code> script, which uses the
 * {@link edu.columbia.cs.sdarts.tools.XSLTest XSLTest} class.
 * <p>
 * Here are the parameters that should be passed to the <code>main()</code>method:
 * <table>
 * <tr><td><b>Argument</b></td><td><b>User-entered / hidden</b></td><td><b>Description</b></td><tr>
 * <tr><td><i>scriptname</i></td><td>hidden</td><td>Name of calling script, so a nice usage string
 * can be printed</td></tr>
 * <tr><td>filename</td><td>user-entered</td><td>The name of the HTML file to tidy</td></tr>
 * </table>
 * <p>
 * The script does not try to validate the HTML document, nor is there
 * any way to set it to do this.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */

public class HTMLTidy {
  /**
   * The <code>main()</code> method. See above for parameters.
   */
  public static void main (String args[]) {
    String scriptName = null;
    String documentFilename = null;

    // Get params
    try {
      scriptName = args[0];
      if (args.length != 2) {
        throw new Exception();
      }
      documentFilename = args[1];
    }
    catch (Exception e) {
      usage (scriptName);
      System.exit(1);
    }

    try {
      // Set up HTML Tidy
      // MAKE SURE THIS CONFIGURATION IS THE SAME AS IN THE
      // edu.columbia.cs.sdarts.tools.XSLTest CLASS, AND sdarts.backend.www.WWWSession CLASS
      // we are now using external configuration file, it should be same
      // read from config\tidy_config.txt
      Tidy tidy = new Tidy();

      tidy.setConfigurationFromFile("config" + File.separator + "tidy_config.txt");
      // Perform tidying and read input file
      tidy.parse (new FileInputStream (documentFilename), System.out);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void usage (String scriptName) {
    if (scriptName == null) {
      scriptName = "xmlvalidate.sh";
    }

    String usageString =
      "Usage: " + scriptName + " <documentName>\n" +
      "Where:\n" +
      "<documentName> is the name of the HTML document to tidy\n" +
      "Note: tidy configuration is read from config\tidy_config.txt\n" +
      "where the directory the program starts is current directory\n";


    System.err.println (usageString);
  }
}
