package edu.columbia.cs.sdarts.dbselection.util;

// JAXP packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class represents a content summary of a collection
 * by storing all terms and their doc-freq in a
 * {@link java.util.HashMap HashMap} <code>map</code>,
 * with the term as key, the doc-freq as value.
 * <p>
 * it adds up doc-freq for terms that shows up in more than one field. So,
 * the actual doc-freq stored is an approximation
 * <p>
 * To use this class, call it's <code>{@link #index index}</code> method
 *
 * @author <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class ContentSummaryHandler extends DefaultHandler {

  private int state;
  private int NONE = 0;
  private int INDEX_THIS_FIELD = 2;
  private int VALUE = 3;
  private int TERM_FREQ = 4;
  private int DOC_FREQ = 5;

  private String term, doc_freq;

  public HashMap map;

  /**
   * default constructor.
   * creates the HashMap to store all terms and their doc-freq
   */
  public ContentSummaryHandler() {
    super();
    // we know this map is only created once, and no need to synchronize
    this.map = new HashMap();
  }

  /**
   * given a <code>url</code> (presummably point to a content summary file)
   * create a HashMap for this collection, with terms as key
   * and terms' doc-freq as value.
   *
   * @param url the url the this content summary file resides.
   * @return the HashMap created
   */
  public static HashMap index(URL url) {
    HashMap m = new HashMap();
    try {
      InputStream is = url.openStream();
      m = index(is);
      // System.out.println(url.toString() + ": index size = " + m.size());
    } catch (IOException e) {
      System.err.println("Error opening url " + url.toString());
      e.printStackTrace();
    }

    return m;
  }

  /**
   * given a string representation of the content summary
   * create a HashMap, with terms as key and doc-freq as value
   *
   * @param src the content summary string
   * @return the HashMap created
   */
  public static HashMap index(String src) {
    HashMap m = new HashMap();
    try {
      InputStream is = new FileInputStream(new File(src));
      m = index(is);
    } catch (FileNotFoundException fnfe) {
      System.err.println("Error opening file " + src);
      fnfe.printStackTrace();
    }
    return m;
  }

  /**
   * given a {@link java.io.InputStream InputStream} <code>is</code>,
   * read the content summary from it, and then create a HashMap, with
   * terms as key and doc-freq as value
   *
   * @param is the InputStream the content summary will be read from
   * @return the HashMap created
   */
  public static HashMap index(InputStream is) {
    ContentSummaryHandler cmh = new ContentSummaryHandler();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(true);
      SAXParser saxParser = spf.newSAXParser();
      saxParser.parse(is, cmh);
    } catch (ParserConfigurationException pce) {
      // System.err.println("Error creating index.");
      pce.printStackTrace();
      // throw new ParserConfigurationException();
    } catch (SAXException se) {
      // System.err.println("Error creating index.");
      se.printStackTrace();
      // throw new Exception();
    } catch (IOException ioe) {
      // System.err.println("Error creating index.");
      ioe.printStackTrace();
      // throw new IOException();
    }
    return cmh.map;
  }

  /**
   * inherite from parent class, initialize <code>state</code>
   */
  public void startDocument() {
    state = NONE;
  }

  public void startElement(String uri, String localName,
      String qName, Attributes attributes) {

    if (qName.equals("starts:field")) {
      state = INDEX_THIS_FIELD;
    } else if(qName.equals("starts:value")) {
        if (state == INDEX_THIS_FIELD)
          state = VALUE;
    } else if(qName.equals("starts:doc-freq")) {
        if (state == INDEX_THIS_FIELD)
          state = DOC_FREQ;
    }
  }

  public void endElement (String uri, String localName, String qName) {

    if (qName.equals("starts:field-freq-info")) {
      state = NONE;
    } else if(qName.equals("starts:doc-freq")) {
      if (state == INDEX_THIS_FIELD) {
        // because the same term may appear in either body-of-text,
        // title or any field,
        // we must allow duplicate appearance of the same term
        if (map.containsKey(term)) {
          // this term is in the map already
          // we should add up it's count
          int count = ((Integer) map.get(term)).intValue();
          int newCount = (new Double(doc_freq)).intValue();
          map.put(term, new Integer(count + newCount));
        } else {
          map.put(term, new Integer(new Double(doc_freq).intValue()));
        }
        // System.out.print(".");
      }
    }
  }

  public void characters (char[] ch, int start, int length) {
    if (state == VALUE) {
      term = new String(ch,start,length).trim();
      state = INDEX_THIS_FIELD;
    } else if (state == DOC_FREQ) {
      doc_freq = new String(ch,start,length).trim(); 
      state = INDEX_THIS_FIELD;
    }
  }


  public void endDocument() {
  }

}
