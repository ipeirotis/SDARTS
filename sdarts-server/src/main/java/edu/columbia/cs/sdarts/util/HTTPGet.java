package edu.columbia.cs.sdarts.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

/**
 * Represents a GET request to an HTTP server.
 * This class is used particularly in the
 * {@link edu.columbia.cs.sdarts.backend.www} package to represent a call to a
 * CGI-bin script on a web search engine.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class HTTPGet extends HTTPRequest {
	private BufferedReader reader;
	private InputStream inputStream;

	/**
	 * Create the request, with a <code>String</code> URL that includes
	 * the parameters to the CGI script, e.g.:<br>
	 * <code>http://www.searchme.com/search.cgi?title="things"</code><br>
	 * This class can also be used to just fetch a regular web page,
	 * e.g.:<br>
	 * <code>http://www.searchme.com/index.html</code><br>
	 * @param urlString a <code>String</code> URL that includes
	 * the parameters to the CGI script
	 */
	public HTTPGet(String urlString) throws MalformedURLException, IOException {
		super(urlString);
	}

	/**
	 * Create the request, with a <code>String</code> URL that includes
	 * only the domain and script name. Parameters are passed as 
	 * name/value 
	 * pairs in a <code>Vector</code>. So in this case the <code>urlString</code>
	 * might be:<br>
	 * <br><code>http://www.searchme.com/search.cgi</code><br>
	 * @param urlString a <code>String</code> URL that includes
	 * only the domain and script name.
	 * @param parameters a <code>Vector</code> of name/value vector 
	 * pairs representing
	 * the parameters passed to the script
	 * @author <i>modified by</i> <a href="jb605@cs.columbia.edu">Jiangcheng Bao</a>
	 */
	public HTTPGet(String urlString, Vector parameters) throws MalformedURLException, IOException {
		super(urlString);
		if (parameters != null && parameters.size() != 0) {
			String paramString = paramString(parameters);
			init(paramString);
		} else {
			;
		}
	}

	/**
	 * Create the request, with a <code>String</code> URL that includes
	 * only the domain and script name. Parameters are in a <code>String</code>
	 * that is assumed to already be URL-encoded.
	 * So in this case the <code>urlString</code>
	 * might be:<br>
	 * <br><code>http://www.searchme.com/search.cgi</code><br>
	 * And the <code>paramString</code> would be:<br>
	 * <code>name=things+to+find</code><br>
	 * @param urlString a <code>String</code> URL that includes
	 * only the domain and script name.
	 * @param paramString an already-URL-encoded <code>String</code>
	 * representing the parameters.
	 */
	public HTTPGet(String urlString, String paramString) throws MalformedURLException, IOException {
		this(urlString, paramString, true);
	}

	/**
	 * Create the request, with a <code>String</code> URL that includes
	 * only the domain and script name. Parameters are in a <code>String</code>.
	 * The caller also indicates whether this <code>String</code> has been
	 * URLEncoded or not.
	 * So in this case the <code>urlString</code>
	 * might be:<br>
	 * <br><code>http://www.searchme.com/search.cgi</code><br>
	 * And the <code>paramString</code> would be either:<br>
	 * <code>name=things+to+find</code><br>
	 * Or:<br>
	 * <code>name=things to find</code></br>
	 * depending on the value of <code>isAlreadyEncoded</code>
	 * @param urlString a <code>String</code> URL that includes
	 * only the domain and script name.
	 * @param paramString a <code>String</code>
	 * representing the parameters.
	 * @param isAlreadyEncoded whether <code>paramString</code> is
	 * already URL encoded or not.
	 */
	public HTTPGet(String urlString, String paramString, boolean isAlreadyEncoded) throws MalformedURLException, IOException {
		super(urlString);
		if (paramString != null) {
			if (!isAlreadyEncoded) {
				paramString = URLEncoder.encode(paramString, "UTF-8");
			}
			init(paramString);
		}
	}

	private void init(String paramString) throws MalformedURLException, IOException {
		url = new URL(url.toString() + "?" + paramString);
	}

	public BufferedReader getReader() throws IOException {
		reader = new BufferedReader(new InputStreamReader(new LegalCharsInputStream(url.openStream())));
		return reader;
	}

	public InputStream getInputStream() throws IOException {
		inputStream = new LegalCharsInputStream(url.openStream());

		return inputStream;
	}

	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		} else {
			inputStream.close();
		}
	}

	/* covert a vector of name / value pair into string
	 */
	private String paramString(Vector p) {
		StringBuffer sb = new StringBuffer();
		try {
			int index = 0;
			for (int i = 0; i < p.size(); i++) {
				String key = URLEncoder.encode(((String[]) p.elementAt(i))[0], "UTF-8");
				String val = URLEncoder.encode(((String[]) p.elementAt(i))[1], "UTF-8");
				if (index > 0) {
					sb.append("&");
				}
				sb.append(key);
				sb.append("=");
				sb.append(val);
				index++;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}
