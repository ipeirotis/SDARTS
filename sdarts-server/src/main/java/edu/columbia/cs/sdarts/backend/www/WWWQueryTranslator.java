package edu.columbia.cs.sdarts.backend.www;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;

//import org.apache.xalan.xslt.StylesheetRoot;
//import org.apache.xalan.xslt.XSLTInputSource;
//import org.apache.xalan.xslt.XSLTProcessor;
//import org.apache.xalan.xslt.XSLTResultTarget;
import org.xml.sax.SAXException;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.common.LSPQuery;
//import edu.columbia.cs.sdarts.common.STARTS;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.util.HTTPRequest;
import edu.columbia.cs.sdarts.util.XMLWriter;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.ByteArrayOutputStream;

/**
 * Translates an {@link edu.columbia.cs.sdarts.common.LSPQuery LSPQuery} into a CGI request.
 * The <code>www_query.xsl</code> stylsheet is required for this; it must
 * reside in the <code>SDARTS_HOME/config/<i>backEndLSPName</i></code> directory.
 * The programmer must write this stylesheet; several examples are provided in
 * the SDARTS distribution.
 * XSL processing is performed with the
 * <a href="http://xml.apache.org/xalan">Apache Xalan</a> XSL processor.
 * <p>
 * The way the transformation works is as follows:
 * <ul>
 * <li>The <code>LSPQuery</code> is converted into its STARTS XML representation
 * by calling its {@link edu.columbia.cs.sdarts.common.LSPQuery#toXML toXML} method.
 * <li>The STARTS XML is input into the Xalan XSL processor
 * <li>The <code>www_request.xsl</code> stylesheet describes how to transform
 * a STARTS &lt;starts:squery&gt; into a <code>starts_intermediate</code>
 * document of the following form:
 * <pre>
 *      &lt;starts:intermediate&gt;
 *          &lt;starts:script&gt;
 *          &lt;starts:url method='GET | POST'&gt;
 *              http://www.google.com
 *          &lt;/starts:url&gt;
 *          &lt;starts:variable&gt;
 *              &lt;starts:name&gt;search&lt;starts:name&gt;
 *              &lt;starts:value&gt;cardiovascular&lt;starts:value&gt;
 *          &lt;/starts:variable&gt;
 *          &lt;starts:variable&gt; . . . . .
 *          &lt;/starts:script&gt;
 *      &lt;/starts:intermediate&gt;
 * </pre>
 * <li>This <code>starts_intermediate</code> XML is returned by Xalan as
 * a sequence of SAX events. These events are processed by an instance of
 * {@link edu.columbia.cs.sdarts.backend.www.STARTSIntermediateHandler STARTSIntermediateHandler},
 * which turns them into a {@link edu.columbia.cs.sdarts.util.HTTPRequest HTTPRequest} and
 * returns it.
 * </ul>
 * This class is not used for translating subsequent CGI requests found
 * in the "more" button of an HTML results page; the
 * {@link edu.columbia.cs.sdarts.backend.www.WWWSession WWWSession} handles that. This
 * class is only for translating the initial query coming from the
 * {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP FrontEndLSP}.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class WWWQueryTranslator implements WWWConstants {
	//private StylesheetRoot 	stylesheet;
	private Transformer transformer;
	private STARTSIntermediateHandler handler;

	/**
	 * Create a <code>WWWQueryTranslator</code>. The class needs to know
	 * the name of the {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} using it,
	 * in order to find the <code>www_query.xsl</code> stylsheet. The class
	 * loads the stylesheet and keeps it in a pre-compiled form.
	 * @param backEndLSPName the name of the <code>BackEndLSP</code> using this
	 * class
	 */
	public WWWQueryTranslator(String backEndLSPName) throws BackEndException {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			String styleSheetFilename =
				SDARTS.CONFIG_DIRECTORY
					+ File.separator
					+ backEndLSPName
					+ File.separator
					+ REQUEST_STYLESHEET_FILENAME;

			Source xsl = new StreamSource(new FileInputStream(styleSheetFilename));
			transformer = factory.newTemplates(xsl).newTransformer();

			// Get Stylsheet
			//		XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
			//	  	String styleSheetFilename =
			//	  		SDARTS.CONFIG_DIRECTORY + File.separator +
			//	  		backEndLSPName + File.separator +
			//	  		REQUEST_STYLESHEET_FILENAME;

			//		FileInputStream fis = new FileInputStream (styleSheetFilename);
			//		stylesheet = processor.processStylesheet (new XSLTInputSource (fis));

			// Set up handler
			handler = new STARTSIntermediateHandler();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BackEndException(e.getMessage());
		}
	}

	/**
	 * Converts an incoming <code>LSPQuery</code> into an <code>HTTPRequest</code>,
	 * which can then be invoked on the underlying web search engine collection
	 * @param query the query to translate
	 * @return an <code>HTTPRequest</code> to invoke the CGI-BIN script on the
	 * web server
	 */
	public HTTPRequest translate(LSPQuery query)
		throws BackEndException, MalformedURLException, IOException, SAXException {
		// Get XML from Query, and send to stylsheet processor
		// We could multithread this but since queries are usually not
		// that big there is no point, and we are on one machine
		//XMLWriter.addSystemDocType("starts:squery",STARTS.STARTS_DTD_URL);
		StringWriter sw = new StringWriter();
		XMLWriter writer = new XMLWriter(sw, new String[] { "starts:squery" }, true);
		query.toXML(writer);
		String queryString = sw.toString();

		//      XSLTInputSource  is     = new XSLTInputSource (new StringReader(queryString));
		//      XSLTResultTarget target = new XSLTResultTarget (handler);
		//      stylesheet.process (is, target);

		Source xml = new StreamSource(new StringReader(sw.toString()));
		ByteArrayOutputStream oStream = new java.io.ByteArrayOutputStream();
		StreamResult result = new StreamResult(oStream);

		try {
			transformer.transform(xml, result);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BackEndException(e.getMessage());
		}

		handler.parse(new org.xml.sax.InputSource(new StringReader(oStream.toString())));

		// Now we have turned the LSPQuery into the starts:intermediate
		// form, and the events of that form have populated everything
		HTTPRequest request = handler.getRequest();

		handler.clear();

		return request;
	}
}
