package edu.columbia.cs.sdarts.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

/**
 * Represents a POST request to an HTTP server.
 * This class is used particularly in the
 * {@link edu.columbia.cs.sdarts.backend.www} package to represent a call to a
 * CGI-bin script on a web search engine.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class HTTPPost extends HTTPRequest {
    private BufferedReader     reader;
    private InputStream        inputStream;
    private HttpURLConnection  connection;
    private String             paramString;

    /**
     * Create the request, with a <code>String</code> URL. This
     * constructor is for the rare instance of using POST to call
     * a script without any parameters. So, the <code>urlString</code>
     * might look something like this:<br>
     * <code>http://www.searchme.com/search.cgi"</code><br>
     * @param urlString a <code>String</code> URL
     */
    public HTTPPost (String urlString)
      throws MalformedURLException, IOException {
      super (urlString);
    }

    /**
     * Create the request, with a <code>String</code> URL that includes
     * only the domain and script name. Parameters are passed as name/value 
     * pairs in a <code>Vector</code>. So in this case the <code>urlString</code>
     * might be:<br>
     * <br><code>http://www.searchme.com/search.cgi</code><br>
     * @param urlString a <code>String</code> URL that includes
     * only the domain and script name.
     * @param parameters a <code>Vector</code> of name/value pairs 
     * representing the parameters passed to the script
     * @author <i>modified by</i>: <a href="jb605@cs.columbia.edu">Jiangcheng Bao</a>
     */
    public HTTPPost (String urlString, Vector parameters)
	  throws MalformedURLException, IOException {
      super (urlString);
      if (parameters != null && parameters.size() != 0) {
	    paramString = paramString (parameters);
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
    public HTTPPost (String urlString, String paramString)
	      throws MalformedURLException, IOException {
	    this (urlString, paramString, true);
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
    public HTTPPost (String urlString, String paramString,
		     boolean isAlreadyEncoded)
	  throws MalformedURLException, IOException {
      super (urlString);
      if (paramString != null) {
	    if (!isAlreadyEncoded) {
	        paramString = URLEncoder.encode (paramString);
	    }
      }
    }

    private void init () throws IOException {
      URL url = getURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput (true);
      connection.setRequestMethod ("POST");
      connection.setUseCaches (false);
      connection.setRequestProperty
        ("Content-Type",
         "application/x-www-form-urlencoded");

      DataOutputStream printout = new DataOutputStream
        (connection.getOutputStream ());
      printout.writeBytes (paramString);
      printout.flush();
      printout.close();
    }

    public BufferedReader getReader() throws IOException {
      init();

      reader = new BufferedReader
        (new InputStreamReader (new LegalCharsInputStream(connection.getInputStream())));
      return reader;
    }

    public InputStream getInputStream() throws IOException {
      init();

	    inputStream = new LegalCharsInputStream(connection.getInputStream());
      return inputStream;
    }

    public void close () throws IOException {
      if (reader != null) {
	    reader.close();
      }
      else {
        inputStream.close();
      }

	    connection.disconnect();
    }

    /* convert a map contains name / value pair into string
     * @author <i>modified by</i>: <a href="jb605@cs.columbia.edu">Jiangcheng Bao</a>
     */
    private String paramString (Vector p) {
    StringBuffer sb = new StringBuffer();
    int index = 0;
    for (int i=0; i<p.size(); i++) {
      String key = URLEncoder.encode (((String[])p.elementAt(i))[0]);
      String val = URLEncoder.encode (((String[])p.elementAt(i))[1]);
      if (index > 0) {
        sb.append ("&");
      }
      sb.append (key);
      sb.append ("=");
      sb.append (val);
      index++;
    }
    return sb.toString();
    }
}
