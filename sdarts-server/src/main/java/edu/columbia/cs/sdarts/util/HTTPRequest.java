

package edu.columbia.cs.sdarts.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents some kind of request to an HTTP server; either GET or
 * POST. This class is used particularly in the
 * {@link edu.columbia.cs.sdarts.backend.www} package to represent a call to a
 * CGI-bin script on a web search engine.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public abstract class HTTPRequest {
  protected URL url;
  protected String cookie;

  /**
   * Create the request, based on <code>String</code> representing
   * the URL.
   * @param urlString the URL, as a <code>String</code>
   * @exception MalformedURLException if the <code>String</code> is
   * a bad URL.
   */
  public HTTPRequest (String urlString) throws MalformedURLException {
    url = new URL (urlString);
  }

  /**
   * Return the URL that is this request
   * @return the URL that is this request
   */
  public URL getURL () {
    return url;
  }

  /**
   * Return a <code>BufferedReader</code> representing the output of the
   * response to the request. <b>Can only be called once, and cannot
   * be called after an invocation of {@link #getInputStream()}</b>.
   * @return a <code>BufferedReader</code> representing the output of the
   * response to the request.
   */
  public abstract BufferedReader getReader() throws IOException;

  /**
   * Return an <code>InputStream</code> representing the output of the
   * response to the request. <b>Can only be called once, and cannot
   * be called after an invocation of {@link #getReader()()}</b>.
   * @return an <code>InputStream</code> representing the output of the
   * response to the request.
   */
  public abstract InputStream getInputStream() throws IOException;

  /**
   * Release all resources associated with this request.
   */
  public abstract void           close () throws IOException;
}
