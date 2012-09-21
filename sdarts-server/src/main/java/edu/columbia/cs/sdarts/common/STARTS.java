
package edu.columbia.cs.sdarts.common;

import java.text.SimpleDateFormat;


/**
 * A "constants container" class holding constants related to STARTS.
 * When talking about the STARTS namespace, or formatting dates as per
 * the STARTS standard, <b>use the values stored here.</b> This minimizes
 * the impact of change, should any of these values change.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class STARTS {
  /** The name of the STARTS XML namespace: currently "starts" */
  public static final String NAMESPACE_NAME  = "starts";

  /** The URL of the STARTS XML namespace: currently "http://sdarts.cs.columbia.edu/STARTS/" */
  public static final String NAMESPACE_VALUE = "http://sdarts.cs.columbia.edu/STARTS/";

  /** The default URL for the STARTS DTD */
  public static final String STARTS_DTD_URL = "http://sdarts.cs.columbia.edu/dtd/starts.dtd";

  /** The default URL for the SDLIP DTD */
  public static final String SDLIP_DTD_URL = "http://sdarts.cs.columbia.edu/dtd/sdlip2.dtd";

  /** The <code>String</code> describing to <code>java.text.SimpleDateFormat</code>
   * how STARTS dates should be formatted.
   */
  public static final String STANDARD_DATE_PATTERN = "yyyy-MM-dd hh:mm:ss z";

  /** A <code>java.text.SimpleDateFormat</code> for formatting dates the way STARTS
   * specifies. Use this, rather than constantly reinstantiating one.
   */
  public static final SimpleDateFormat STANDARD_DATE_FORMAT =
    new SimpleDateFormat (STANDARD_DATE_PATTERN);

  private STARTS() {}
}
