package edu.columbia.cs.sdarts.dbselection.util;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * This class represents a subcollection (also called collection).
 * This class can be instantiated from an XML doc
 * {@link org.jdom.Element Element} <code>subcol</code>
 * or it can be created from all it's memebers, including
 * <code>{@link #serverURL serverURL}</code>,
 * <code>{@link #subcolName subcolName}</code>,
 * <code>{@link #subcolDesc subcolDesc}</code>
 * <p>
 * This class is used by {@link edu.columbia.cs.sdarts.dbselection.DBSelectionServer DBSelectionServer}
 * to write subcol info to client, also used by web client to parse this info
 *
 * @author <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class Subcol
{

  /**
   * constructor,
   * takes a element, whose name is subcol,
   * parse it and get back all the members,
   * with all namespace ignored.
   * <p>
   * This is used to parse info from sdarts server.
   *
   * @param sce the element with name "subcol" to be parsed
   */
  public Subcol (Element sce) {
    if ((sce != null) && (sce.getName().equals("subcol"))) {
      this.namespace = sce.getNamespace();
      // the serverURL won't come from the sdarts server
      // maybe we should just leave this out?
      this.serverURL = sce.getChildTextTrim("serverURL", namespace);
      this.subcolName = sce.getChildTextTrim("subcolName", namespace);
      this.subcolDesc = sce.getChildTextTrim("subcolDesc", namespace);
      try {
        String s = sce.getChildTextTrim("score", namespace);
        if ((s != null) && !(s.equals(""))) {
          this.score = Double.parseDouble(s);
        } else {
          this.score = 0.0;
        }
      } catch (NumberFormatException nfe) {
        this.score = 0.0;
      }
      // we ignore queryLangs at this moment
    } else {
      System.err.println("Subcol: null input element found!");
    }
  }

  /**
   * construct a Subcol given a server url <code>url</code>,
   * a subcol Name <code>name</code>, a subcol description <code>desc</code>,
   * and a ranking score <code>s</code>.
   * <p>
   * this method is used to write out to xml string
   *
   * @param url the server url for this collection
   * @param name the collection name
   * @param desc the description of this collection
   * @param s the ranking score of this collection
   */
  public Subcol (String url, String name, String desc, double s) {
    this.serverURL = url;
    this.subcolName = name;
    this.subcolDesc = desc;
    this.score = s;
  }

  /**
   * given a name for the Element,
   * returns an Element representation of this subcol
   *
   * @param name specify a name for the element, defaults "subcol"
   * @return the element created.
   */
  public Element toElement (String name) {
    if (name == null) { return null; }
    Element sce = new Element(name, namespace);
    sce.addContent(new Element("serverURL",
        namespace).setText(this.serverURL));
    sce.addContent(new Element("subcolName",
        namespace).setText(this.subcolName));
    sce.addContent(new Element("subcolDesc",
        namespace).setText(this.subcolDesc));
    String s = Double.toString(this.score);
    sce.addContent( new Element("score", namespace).setText(s));
    return sce;
  }

  /**
   * returns an Element representation of this subcol with default name "subcol"
   * 
   * @return the element created.
   */
  public Element toElement () {
    return this.toElement(defaultName);
  }

  /**
   * returns a xml string representation of this subcol,
   * with the element name given as <code>name</code>.
   *
   * @param name the name of the element which enclose all the members
   * @return the string
   */
  public String toXML (String name) {
    if (name == null) { return null; }
    XMLOutputter xo = new XMLOutputter("", false);
    return xo.outputString(this.toElement(name));
  }

  /**
   * returns a xml string representation of this subcol,
   * with the default element name "subcol"
   *
   * @return the string
   */
  public String toXML () {
    return this.toXML(defaultName);
  }
  
  private Namespace namespace = null;
  private String defaultName = "subcol";
  public String serverURL = "";
  public String subcolName = "";
  public String subcolDesc = "";
  public double score = 0.0;
}
