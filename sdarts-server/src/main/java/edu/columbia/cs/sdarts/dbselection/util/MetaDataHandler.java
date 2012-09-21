package edu.columbia.cs.sdarts.dbselection.util;

import gnu.regexp.RE;
import gnu.regexp.REMatch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class represents a metadata info xml string from sdarts server
 * all meta info is stored in a {@link java.util.HashMap HashMap}
 * <p>
 * To use this class, call it's <code>{@link #parse() parse}</code> method.
 *
 * @author <a href="jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class MetaDataHandler extends DefaultHandler
{
  private int state;
  private static final int NONE = 0;
  private static final int ABSTRACT = 1;
  private static final int LANGUAGE = 2;
  private static final int DATE = 3;
  private static final int STOPWORDS = 4;
  private static final int ALGORITHM = 5;
  private static final int FIELDS = 6;
  private static final int LINKAGE = 7;
  private static final int CONTENT_SUMMARY_LINKAGE = 8;
  private static final int CLASSIFICATION = 9;

  private String serverURL;
  private Vector values;
  private HashMap map;
  private String pcdata = null;

  /**
   * constructor, initialize the class with <code>sdartsServerURL</code> as
   * server url, also initialize HashMap to store all the meta info
   *
   * @param sdartsServerURL the sdarts server url this meta info is from
   */
  public MetaDataHandler(String sdartsServerURL) {
    super();
    this.serverURL = sdartsServerURL;
    this.map = new HashMap();
  }

  /**
   * constructor, initialize the class with meta info <code>m</code> and
   * <code>sdartsServerURL</code>
   *
   * @param m the meta info as a HashMap
   * @param sdartsServerURL the sdarts server url for this meta info
   */
  public MetaDataHandler(HashMap m, String sdartsServerURL) {
    super();
    this.map = m;
    this.serverURL = sdartsServerURL;
  }

  /**
   * given a string representation of meta data info <code>metaInfo</code>,
   * and an associated <code>sdartsServerURL</code>, parse the meta info
   * into a HashMap
   *
   * @param metInfo the string of meta info
   * @param sdartsServerURL the sdarts server url for this meta info
   * @return the HashMap representation of the meta info
   */
  public static HashMap parse (String metaInfo, String sdartsServerURL) {
    return parse(new ByteArrayInputStream(metaInfo.getBytes()),
        sdartsServerURL);
  }

  /**
   * given a {@link java.io.InputStream InputStream} <code>is</code>
   * and an associated <code>sdartsServerURL</code>,
   * read meta info from the <code>is</code>, parse the meta info
   * into a HashMap
   *
   * @param is the InputStream to read meta info from
   * @param sdartsServerURL the sdarts server url for this meta info
   * @return the HashMap representation of the meta info
   */
  public static HashMap parse(InputStream is, String sdartsServerURL) {
    MetaDataHandler mdh = new MetaDataHandler(sdartsServerURL);
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser saxParser = spf.newSAXParser();
      saxParser.parse(is, mdh);
    } catch (ParserConfigurationException pce) {
      pce.printStackTrace();
    } catch (SAXException se) {
      se.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return mdh.map;
  }

  public void setDocumentLocator (Locator l) {
  }

  /**
   * initalize the state
   */
  public void startDocment () {
    state = NONE;
  }

  public void startElement (String uri, String localName,
      String qName, Attributes attrs) {
    String name = removeNameSpace(qName);
    /*
    System.out.println("uri = " + uri);
    System.out.println("localName = " + localName);
    System.out.println("qName = " + qName);
    System.out.println("name = " + name);
    */
    if(name.equals("abstract"))
      state = ABSTRACT;
    else if (name.equals("source-language"))
      state = LANGUAGE;
    else if (name.equals("date-changed"))
      state = DATE;
    else if (name.equals("stop-word-list"))
      values = new Vector();
    else if (name.equals("word"))
      state = STOPWORDS;
    else if (name.equals("ranking-algorithm-id"))
      state = ALGORITHM;
	else if (name.equals("classification"))
	  state = CLASSIFICATION;
    else if (name.equals("query-parts-supported"))
      map.put("query-parts-supported",attrs.getValue("parts"));
    else if (name.equals("fields-supported")) {
      values = new Vector();
      state = FIELDS;
    } else if (name.equals("field") && state==FIELDS)
      values.add(attrs.getValue("name"));
    else if (name.equals("linkage"))
      state = LINKAGE;
    else if (name.equals("content-summary-linkage"))
      state = CONTENT_SUMMARY_LINKAGE;
    else
      state = NONE;
  }


  public void characters (char[] ch, int start, int length) throws SAXException
  {
    if (state != NONE)
      pcdata = new String (ch, start, length).trim();
  }


  public void endElement (String uri, String localName, String qName) {
    String name = removeNameSpace(qName);
    //try {
      switch (state) {
        case ABSTRACT:
          map.put("abstract",pcdata);
          pcdata = null;
          state = NONE;
          break;
        case LANGUAGE:
          map.put("source-language",pcdata);
          pcdata = null;
          state = NONE;
          break;
        case DATE:
          map.put("date-changed",pcdata);
          pcdata = null;
          state = NONE;
          break;
        case STOPWORDS:
          if(name.equals("stop-word-list")) {
            map.put("stop-word-list",values);
            pcdata = null;
            state = NONE;
          } else {
            values.add(pcdata);
            pcdata = null;
          }
          break;
        case ALGORITHM:
          map.put("ranking-algorithm-id",pcdata);
          pcdata = null;
          state = NONE;
          break;
		case CLASSIFICATION:
		  map.put("classification",pcdata);
		  pcdata = null;
		  state = NONE;
		  break;
        case FIELDS:
          if(name.equals("fields-supported")) {
            map.put("fields-supported",values);
            pcdata = null;
            state = NONE;
          }
          break;
        case LINKAGE:
          try {
            URL url = new URL(pcdata);
            map.put("linkage",fixLinkage(url, serverURL));
          } catch (MalformedURLException me) {
          }
          pcdata = null;
          state = NONE;
          break;
        case CONTENT_SUMMARY_LINKAGE:
          try {
            URL url = new URL(pcdata);
            map.put("content-summary-linkage",
                fixLinkage(url,serverURL));
          } catch (MalformedURLException me) {
          }
          pcdata = null;
          state = NONE;
          break;
      }
    /*} catch (Exception e) {
      e.printStackTrace();
    }*/
  }

  /**
   * remove the namespace from a xml node name, indicates by an colon (:)
   *
   * @param input the input string whose namespace is to be removed
   * @return the name after namespace removed
   */
  public String removeNameSpace (String input) {
    int i = input.indexOf(":");
    return (i<0) ? input : input.substring(i+1);
  }

  /**
   * given a string, which might contain a linkage
   * substitute linkage of 'localhost' to the actual host name
   *
   * @param str the string that contains the linkage
   * @param server_URL the actual server url that will be filled in
   * @return the string with "localhost" replaced
   */
  public String fixLinkage(String str, String server_URL)
      throws Exception {
    String result = new String(); // holds the resulting changed string
    // a reg.ex. to look for the needed field
    RE re = new
    RE("<[^>]*field[^>]+name=[\"\']linkage[\"\'].*?<[^>]*?value>(.*?)<"
        ,RE.REG_ICASE);

    URL url;
    URL serverURL = new URL(server_URL);
    int current_string_index = 0; // index into the input string

    // find all matches
    REMatch [] matches = re.getAllMatches(str);
    for(int i=0; i<matches.length; i++) {
      url = new URL(matches[i].toString(1));
      if(url.getHost().equalsIgnoreCase("localhost")) {
        url = new
            URL(url.getProtocol(),serverURL.getHost(),
            url.getPort(),url.getFile());
      }
      result+=
          str.substring(current_string_index,matches[i].getSubStartIndex(1));
      result += url;
      current_string_index = matches[i].getSubEndIndex(1);
    }

    // copy over the remainder of the string
    result+=str.substring(current_string_index);

    return result;
  }

  /**
   * given a url, which might contain reference to "localhost"
   * substitute linkage of 'localhost' to the actual host name
   *
   * @param url the url that contains reference to "localhost"
   * @param server_URL the actual server url that will be filled in
   * @return the string representation of the url with "localhost" replaced
   */
  private String fixLinkage(URL url, String server_URL)
      throws MalformedURLException {
    if(url.getHost().equalsIgnoreCase("localhost"))
        url = new URL(url.getProtocol(),new URL(server_URL).getHost(),
           url.getPort(),url.getFile());

    return url.toString();
  }

}
