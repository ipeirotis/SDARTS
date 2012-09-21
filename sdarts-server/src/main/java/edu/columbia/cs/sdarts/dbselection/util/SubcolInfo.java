package edu.columbia.cs.sdarts.dbselection.util;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * This class represents a group of collections, with server url, 
 * collection name, collection description, and ranking score.
 * <p>
 * This class is used by {@link edu.columbia.cs.sdarts.dbselection.DBSelectionServer DBSelectionServer}
 * to write subcol infos to client. Also used by web client to parse subcol info
 * returned from dbselection server
 *
 * @author <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class SubcolInfo
{

  /**
   * This method is for testing purpose
   * it takes a filename, and parse it into SubcolInfo, and spit the xml back.
   * @param args the command line arguments, only need filename
   */
  public static void main (String[] args) throws JDOMException {
    if (args.length == 1) {
      File f = new File(args[0]);
      Document doc = (new SAXBuilder()).build(f);
      Element root = doc.getRootElement();
      SubcolInfo sci = new SubcolInfo(root);
      System.out.println(sci.toXML());
    } else {
      System.err.println("Missing arguments!");
    }
  }

  /**
   * default construtor do nothing
   */
  public SubcolInfo () {
    this.subcols = new Vector();
  }

  /**
   * construct a SubcolInfo from an {@link java.io.InputStream InputStream}
   * <code>is</code>.
   *
   * @param is the input stream to read from
   */
  public SubcolInfo (InputStream is) throws JDOMException {
    this((new SAXBuilder()).build(is).getRootElement());
  }

  /**
   * construct a SubcolInfo from a string
   *
   * @param sci the string to create SubcolInfo from 
   */
  public SubcolInfo (String sci) throws JDOMException {
    this((new SAXBuilder()).build(new StringReader(sci)).getRootElement());
  }

  /**
   * constructor,
   * takes a element, whose name is subcolInfo,
   * parse it and create an SubcolInfo, with all namespace ignored.
   *
   * @param scie the element to create SubcolInfo from 
   */
  public SubcolInfo(Element scie) {
    this();
    if ((scie != null) && (scie.getName().equals("subcolInfo"))) {
      this.namespace = scie.getNamespace();
      List scl = scie.getChildren("subcol", namespace);
      if (scl != null) {
        ListIterator li = scl.listIterator();
        while (li.hasNext()) {
          Element sce = (Element) li.next();
          if (sce != null) {
            Subcol sc = new Subcol(sce);
            subcols.add(sc);
          }
        }
      } else {
        System.err.println("SubcolInfo: no children named a:subcol found!");
      }
    } else {
      System.err.println("SubcolInfo: null input element found!");
    }
  }

  /**
   * add one {@link edu.columbia.cs.sdarts.dbselection.util.Subcol Subcol} <code>sc</code>
   * into this info list
   *
   * @param sc the Subcol to add
   */
  public void addSubcol(Subcol sc) {
    this.subcols.add(sc);
  }

  /**
   * bubble sort subcols on their ranking score
   *
   * @param descending sorting order is descending?
   */
  public void sort(boolean descending) {
    int size = subcols.size();
    if (descending) {
      for (int i=subcols.size(); --i>=0; ) {
        boolean swapped = false;
        for (int j=0; j<i; j++) {
          Subcol sc1 = (Subcol) subcols.elementAt(j);
          Subcol sc2 = (Subcol) subcols.elementAt(j+1);
          if (sc1.score < sc2.score) {

            // int t = a[j]
            Object t = subcols.elementAt(j);

            // a[j] = a[j+1]
            subcols.removeElementAt(j);
            subcols.insertElementAt(sc2, j);

            // a[j+1] = t;
            subcols.removeElementAt(j+1);
            subcols.insertElementAt(t, j+1);

            swapped = true;
          } // if
        } //for j
        if (!swapped) {
          return;
        }
      } // for i
    } else { // ascending
      for (int i=subcols.size(); --i>=0; ) {
        boolean swapped = false;
        for (int j=0; j<i; j++) {
          Subcol sc1 = (Subcol) subcols.elementAt(j);
          Subcol sc2 = (Subcol) subcols.elementAt(j+1);
          if (sc1.score > sc2.score) {

            // int t = a[j]
            Object t = subcols.elementAt(j);

            // a[j] = a[j+1]
            subcols.removeElementAt(j);
            subcols.insertElementAt(sc2, j);

            // a[j+1] = t;
            subcols.removeElementAt(j+1);
            subcols.insertElementAt(t, j+1);

            swapped = true;
          } // if
        } //for j
        if (!swapped) {
          return;
        }
      } // for i
    }

  }

  /**
   * returns an element representation of this SubcolInfo with element name 
   * <code>name</code>
   *
   * @param name the name of the result element
   * @return the element created
   */
  public Element toElement (String name) {
    if (name == null) { return null; }
    Element scie = new Element(name, namespace);
    for (int i=0; i<subcols.size(); i++) {
      Subcol sc = (Subcol) subcols.elementAt(i);
      scie.addContent(sc.toElement("subcol"));
    }
    return scie;
  }

  /**
   * returns an element representation of this SubcolInfo
   * with default element name "subcolInfo"
   *
   * @return the element created
   */
  public Element toElement () {
    return this.toElement(defaultName);
  }

  /**
   * returns a string representation of this SubcolInfo,
   * with it's root element name <code>name</code>
   *
   * @param name the root element name
   * @return the string created
   */
  public String toXML (String name) {
    if (name == null || name.equals("")) { return null; }
    XMLOutputter xo = new XMLOutputter("  ", true);
    return xo.outputString(this.toElement(name));
  }

  /**
   * returns a string representation of this SubcolInfo,
   * with default root element name "subcolInfo"
   *
   * @return the string created
   */
  public String toXML () {
    return this.toXML(defaultName);
  }

  private Namespace namespace = null;
  private String defaultName = "subcolInfo";
  public Vector subcols;
}
