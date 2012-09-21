package edu.columbia.cs.sdarts.backend.www;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
//import java.io.StringWriter;

//import org.apache.xalan.xpath.xdom.XercesLiaison;
//import org.apache.xalan.xslt.StylesheetRoot;
//import org.apache.xalan.xslt.XSLTInputSource;
//import org.apache.xalan.xslt.XSLTProcessor;
//import org.apache.xalan.xslt.XSLTProcessorFactory;
//import org.apache.xalan.xslt.XSLTResultTarget;
import org.w3c.tidy.Tidy;
//import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.common.LSPDoc;
import edu.columbia.cs.sdarts.common.LSPField;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.util.HTTPRequest;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.ByteArrayOutputStream;

/**
 * Represents the connection between the
 * {@link edu.columbia.cs.sdarts.backend.www.WWWBackEndLSP WWWBackEndLSP} and some web-based
 * search engine. One session is created for each query. Once the session is
 * created, the {@link edu.columbia.cs.sdarts.backend.www.WWWQueryProcessor}
 * continues to call the
 * {@link #getDocs() getDocs()} method, retrieving documents until either
 * a <code>null</code> is returned, or the {@link #isEmpty() isEmpty()}
 * method returns <code>true</code>.
 * <p>
 * During each invocation of <code>getDocs()</code>, the following happens:
 * <ul>
 * <li>In one thread, the CGI request is invoked and results are sent
 * to <a href="http://www.w3.org/People/Raggett/tidy/">HTML Tidy</a>,
 * a free tool from W3C that converts the HTML into well-formed XML. The
 * output of this thread is piped to a second thread.
 * <li>In the second thread, the <a href="http://xml.apache.org/xalan">
 * Apache Xalan</a> XSL processor uses the <code>www_results.xsl</code>
 * stylesheet to transform the incoming XML into
 * <code>starts_intermediate</code> form. The result should look something
 * like this:
 * <pre>
 *      &lt;starts:intermediate&gt;
 *          &lt;starts:sqrdocument&gt;
 *              &lt;starts:rawscore&gt;0.9&lt;/starts:rawscore&gt;
 *              &lt;starts:doc-term&gt;
 *                &lt;starts:field name="title"&gt;
 *                &lt;starts:value&gt;cardiovascular&lt;starts:value&gt;
 *              &lt;/starts:doc-term&gt;
 *              &lt;starts:doc-term&gt; . . .
 *          &lt;/starts:sqrdocument&gt;
 *          &lt;starts:sqrdocument&gt; . . .
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
 * Each &lt;starts:sqrdocument&gt; represents one result extracted from the page.
 * The &lt;starts:script&gt; represents a translation of a "more" button at
 * the bottom of the HTML results page, and is present only if there was such
 * a button there.
 * <li>The <code>starts_intermediate</code> output is sent as a stream of
 * SAX events, to an instance of
 * {@link edu.columbia.cs.sdarts.backend.www.STARTSIntermediateHandler STARTSIntermediateHandler},
 * which will extract the results as {@link edu.columbia.cs.sdarts.common.LSPDoc LSPDocs} and,
 * if the "more" button was present, will extract a new invocation coded as
 * an {@link edu.columbia.cs.sdarts.util.HTTPRequest}.
 * <li><code>getDocs()</code> returns the <code>LSPDocs</code>
 * <li>If there was no "more" button, or the maximum number of documents required
 * for the query is exceeded, or an <code>Exception</code> happened somewhere,
 * then <code>isEmpty()</code> will return <code>true</code> at its next
 * invocation.
 * <li>Otherwise, calling <code>getDocs()</code> again automatically invokes
 * the new <code>HTTPRequest</code> that was retrieved during translation.
 * <li>This class now handles multi-level results, i.e., we can simulate 
 * multiple clicks on first level page to retrieve results from second or 
 * deeperlevel pages
 * </ul>
 * This class <b>does not deal with errors on the server, or timeouts, etc.</b>
 * A future version should.
 * <p>
 * For more information about the <code>starts_intermediate</code>
 * format, see the
 * <a href="http://www.cs.columbia.edu/~dli2test/dtd/starts_interemdiate.dtd">
 * starts_intermediate.dtd</a>.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @author <i>modified by:</i> <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class WWWSession implements WWWConstants {
	// ------------ FIELDS ------------
	private String backEndLSPName;
	private int maxDocs;
	private int docCount;
	HTTPRequest request;
	private STARTSIntermediateHandler handler;
	private Throwable error;
	//StylesheetRoot                     		stylesheet;
	private Transformer transformer;
	Tidy tidy;
	private int numavailable;

	// ------------ METHODS ------------
	/**
	 * Start a session with a web search engine.
	 * @param backEndLSPName the name of the <code>BackEndLSP</code> using
	 * this session (needed for locating the <code>www_results.xsl</code>
	 * file
	 * @param request the <code>HTTPRequest</code> representing the original
	 * CGI-BIN invocation onto the search engine
	 * @param maxDocs the maximum number of documents to retrieve in the course
	 * of the session
	 * @param answerFields the fields that ought to appear in the results of
	 * the session.
	 */
	public WWWSession(String backEndLSPName, HTTPRequest request, int maxDocs, LSPField[] answerFields)
		throws BackEndException, SAXException {
		this.backEndLSPName = backEndLSPName;
		this.request = request;
		this.maxDocs = maxDocs;
		numavailable = 0;

		try {
			// Get Stylsheet
			TransformerFactory factory = TransformerFactory.newInstance();
			String styleSheetFilename =
				SDARTS.CONFIG_DIRECTORY
					+ File.separator
					+ backEndLSPName
					+ File.separator
					+ RESULTS_STYLESHEET_FILENAME;
			Source xsl = new StreamSource(new FileInputStream(styleSheetFilename));
			transformer = factory.newTemplates(xsl).newTransformer();

			//      XercesLiaison liaison = new XercesLiaison();
			//      liaison.setUseValidation(false);
			//      XSLTProcessor processor = XSLTProcessorFactory.getProcessor(liaison);

			//      stylesheet = processor.processStylesheet (new XSLTInputSource (fis));

			// Set up handler
			handler = new STARTSIntermediateHandler(answerFields);
		} catch (Exception e) {
			throw new BackEndException(e.getMessage());
		}

		// Set up HTML Tidy
		// MAKE SURE THIS CONFIGURATION IS THE SAME AS IN THE
		// edu.columbia.cs.sdarts.tools.HTMLTidy CLASS, AND sdarts.tools.XSLTest CLASS
		// we are now using external configuration file, so it should be the same
		tidy = new Tidy();

		tidy.setConfigurationFromFile(SDARTS.CONFIG_DIRECTORY + File.separator + "tidy_config.txt");
		// This setting can be different from WWWTest class
		tidy.setErrout(new PrintWriter(System.err, true));
	}

	/**
	 * Makes a CGI invocation onto a web search engine, blocks, and then
	 * returns results extracted from the HTML returned by the web site.
	 * Will automatically return <code>null</code> if the <code>isEmpty()</code>
	 * method is returning <code>true</code>. If <code>isEmpty()</code> is not
	 * <code>true</code>, this method can be called repeatedly - it means there
	 * is a "more" button at the bottom of the results HTML page, and the
	 * maximum document limit has not yet been exceeded.
	 * @return documents extracted from the HTML results page, or <code>null</code>
	 * if there are no results.
	 */
	public LSPDoc[] getDocs() throws BackEndException {
		// Check to make sure not empty
		if (isEmpty()) {
			return null;
		}

		// Start the ball rolling
		try {
			// Synchronous version - bring this out if asynch should start
			// acting funny
			//
			/*
			XSLTResultTarget target = new XSLTResultTarget (handler);
			
			String fname = SDARTS.CONFIG_DIRECTORY + File.separator + "temp.xml";
			FileOutputStream fo =
			  new FileOutputStream (fname);
			tidy.parseDOM (request.getInputStream(), fo);
			
			BufferedReader br =
			  new BufferedReader (
			    new InputStreamReader (
			      new FileInputStream (fname)));
			stylesheet.process (new XSLTInputSource (br), target);
			*/
			//

			// Asynch version - take advantage of processing on web server
			//
			final PipedInputStream pis = new PipedInputStream();
			final PipedOutputStream pos = new PipedOutputStream(pis);
			//        final BufferedReader br = new BufferedReader(
			//            new InputStreamReader(pis));
			//        final StringWriter sw = new StringWriter();

			final Source xml = new StreamSource(new BufferedReader(new InputStreamReader(pis)));

			//        final XSLTInputSource is =
			//          new XSLTInputSource (
			//            new InputSource (
			//              new BufferedReader(
			//                new InputStreamReader(pis))));

			final ByteArrayOutputStream oStream = new java.io.ByteArrayOutputStream();
			final StreamResult result = new StreamResult(oStream);

			//        final XSLTResultTarget target = new XSLTResultTarget (handler);

			Thread t1 = new Thread() {
				public void run() {
					try {
						org.w3c.dom.Document doc = tidy.parseDOM(request.getInputStream(), null);
						tidy.pprint(doc, pos);
						pos.flush();
						pos.close();
					} catch (Exception e) {
						setError(e);
					}
				}
			};
			Thread t2 = new Thread() {
				public void run() {
					try {
						transformer.transform(xml, result);
						handler.parse(new org.xml.sax.InputSource(new java.io.StringReader(oStream.toString())));

						//stylesheet.process (is, target);
					} catch (Exception e) {
						setError(e);
					}
				}
			};

			// Start processing the XSL transformation of results
			t1.start();
			t2.start();

			t1.join();
			t2.join();

			//
			request.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			//throw new BackEndException (e.getMessage());
		}

		if (error != null) {
			error.printStackTrace(System.err);
			//throw new BackEndException (error.getMessage());
		}

		// When stylesheet.process() unblocks, we are done!

		// First, see if there was a "more" button
		// VERY IMPORTANT: If no more button, handler returns null
		// so that isEmpty() returns true from now on
		// we are now dealing with multi-level of results,
		// so maybe more than one requests left at given moment
		request = handler.getRequest();

		if (numavailable == 0)
			numavailable = handler.getNumAvailable();

		// Now process the results
		LSPDoc[] docs = null;
		LSPDoc[] temp = handler.getDocs();
		handler.clear();
		int maxBatchSize = maxDocs - docCount;
		int batchSize = temp.length;
		if (batchSize > maxBatchSize) {
			docs = new LSPDoc[maxBatchSize];
			System.arraycopy(temp, 0, docs, 0, maxBatchSize);
		} else {
			docs = temp;
		}
		if (docs != null) {
			docCount += docs.length;
		}

		return docs;
	}

	/**
	 * Checks to see whether more results can be obtained in this session
	 * by another call to <code>getDocs()</code>.
	 * @return <code>true</code> if either the maximum number of desired
	 * documents is exceeded, or there is no "more" button at the bottom
	 * of the last retrieved HTML page, or there has been an
	 * <code>Exception</code>; <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return (docCount >= maxDocs || request == null || error != null);
	}

	void setError(Throwable error) {
		this.error = error;
	}

	public int getNumAvailable() {
		return numavailable;
	}

}
