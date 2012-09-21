package edu.columbia.cs.sdarts.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMException;
import org.w3c.tidy.Tidy;

/**
 * Called from the <code>xsltest.sh</code> script. Given an XML document
 * and an XSL stylesheet, will produce the transformed output. In addition,
 * if passed the <code>-tidy</code> parameter, the script can pre-process
 * the XML document using
 * <a href="http://www.w3.org/People/Raggett/tidy/">HTML Tidy</a>, just
 * as the {@link edu.columbia.cs.sdarts.backend.www} package does, in order to see how
 * a web page read by that framework would look if stylesheet is applied
 * to it. This tool is useful when developing <code>www_results.xsl</code>
 * and <code>www_query.xsl</code> files. Use the "-tidy" parameter to
 * for the former; don't use it for the latter.  In fact, it's probably
 * best not to use <code>-tidy</code> at all, but rather to get a
 * tidied file using the <code>htmltidy.sh</code> script. Then, you can
 * look at that output, and build the <code>www_results.xsl</code>
 * stylesheet knowing what it is it will be processing.
 * <p>
 * Here are the parameters that should be passed to the <code>main()</code> method:
 * <table>
 * <tr><td><b>Argument</b></td><td><b>User-entered / hidden</b></td><td><b>Description</b></td><tr>
 * <tr><td><i>scriptname</i></td><td>hidden</td><td>Name of calling script, so a nice usage string
 * can be printed</td></tr>
 * <tr><td>-tidy</td><td>user-entered, optional</td><td>Preprocess XML input document with
 * HTML Tidy, just as <code>sdarts.backend.www</code> does with incoming HTML results</td></tr>
 * <tr><td><i>documentfilename</i></td><td>user-entered</td><td>The filename for the XML document
 * to transform</td><tr>
 * <tr><td><i>stylesheetfilename</i></td><td>user-entered</td><td>The filename for the XSL stylsheet
 * to use</td><tr>
 * </table>
 * NOTE: the html tidy configuration file tidy_config.txt must reside in
 * config directory under which this program is started
 *
 * The script does not try to validate the XML document, nor is there
 * any way to set it to do this.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class XSLTest {

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	static final String W3C_XML_SCHEMA ="http://www.w3.org/2001/XMLSchema";


	/**
	 * The <code>main()</code> method. See above for parameters.
	 */
	public static void main(String args[]) {
		boolean useTidy = false;
		String scriptName = null;
		String documentFilename = null;
		String stylesheetFilename = null;

		try {
			scriptName = args[0];
			if (args.length == 3) {
				documentFilename = args[1];
				stylesheetFilename = args[2];
			} else if (args.length == 4) {
				if (!args[1].equals("-tidy")) {
					throw new Exception();
				} else {
					useTidy = true;
				}
				documentFilename = args[2];
				stylesheetFilename = args[3];
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			usage(scriptName);
			System.exit(1);
		}

		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Source xsl = new StreamSource(new FileInputStream(stylesheetFilename));
			Transformer transformer = factory.newTemplates(xsl).newTransformer();
			//StreamResult result = new StreamResult(System.out);
			
			File tempOut = File.createTempFile("sdarts","tmp",new File("."));
			StreamResult result = new StreamResult(tempOut); 


			//	    XercesLiaison liaison = new XercesLiaison ();
			//	    liaison.setUseValidation(false);
			//	    XSLTProcessor processor =
			//		XSLTProcessorFactory.getProcessor(liaison);
			//	    StylesheetRoot stylesheet =
			//		processor.processStylesheet
			//		(new XSLTInputSource
			//		    (new FileInputStream (stylesheetFilename)));
			//	    XSLTResultTarget xmlResult =
			//		new XSLTResultTarget (System.out);

			//XSLTInputSource xmlSource;
			Source xml;

			if (!useTidy) {
				xml = new StreamSource(new FileInputStream(documentFilename));
				//			xmlSource  = new XSLTInputSource(
				//							new FileInputStream(documentFilename));

			} else {
				// Set up HTML Tidy
				// MAKE SURE THIS CONFIGURATION IS THE SAME AS IN THE
				// edu.columbia.cs.sdarts.tools.HTMLTidy CLASS, AND
				// edu.columbia.cs.sdarts.backend.www.WWWSession CLASS
				// we are now using external configuration file, it should be the same
				// tidy config is read from config\tidy_config.txt
				Tidy tidy = new Tidy();
				tidy.setConfigurationFromFile("config" + File.separator + "tidy_config.txt");
				tidy.setErrout(new PrintWriter(System.err, true));

				// Perform tidying and read input file
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				tidy.parse(new FileInputStream(documentFilename), bos);

				// Set up XSL processing and process
				xml = new StreamSource(new ByteArrayInputStream(bos.toByteArray()));

				//			xmlSource  = new XSLTInputSource(
				//							new ByteArrayInputStream (bos.toByteArray()));
			}

			// Perform the transformation.
			transformer.transform(xml, result);
			
			DocumentBuilderFactory  factr = DocumentBuilderFactory.newInstance();
			//SAXParserFactory factr = SAXParserFactory.newInstance();
			factr.setNamespaceAware(true);
			factr.setValidating(true);
			factr.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			DocumentBuilder domParser = factr.newDocumentBuilder();
			try{
				domParser.parse(tempOut);
			}
			catch ( DOMException dome)
			{
			  dome.printStackTrace();
			  System.exit(2);
			}
			BufferedReader fr = new BufferedReader(new FileReader(tempOut));
			String l;
			while ( (l=fr.readLine()) !=null) {
				System.out.println(l);
			}

			
			//saxParser.parse


			//		stylesheet.process (xml, xmlResult);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void usage(String scriptName) {
		if (scriptName == null) {
			scriptName = "xsltest.sh";
		}

		String usageString =
			"Usage: "
				+ scriptName
				+ " [-tidy] <documentName> <stylesheetName>\n"
				+ "Where:\n"
				+ "-tidy indicates to preprocess XML input document with HTML Tidy,\n"
				+ "\tjust as edu.columbia.cs.sdarts.backend.www does with incoming HTML results\n"
				+ "<documentName> is the name of the XML document to process\n"
				+ "<stylesheetName> is the name of the XSL stylesheet to use\n"
				+ "Note: tidy settings are read from config\tidy_config.txt\n"
				+ "where the directory the program starts is current directory\n";

		System.err.println(usageString);
	}

}
