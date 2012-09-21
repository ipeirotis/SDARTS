
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;



/**
 * Represents a "source", as defined in STARTS 1.0. A source is simply the
 * name of the underlying subcollection being searched - another name
 * for the {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPSource extends LSPObject {
  private String name;

  /**
   * Create an <code>LSPSource</code>
   * @param name the name of the underlying collection
   */
  public LSPSource (String name) {
    this.name = name;
  }

  /**
   * Set the name of the source
   * @param name the name of the source
   */
  public void setName (String name) {
    this.name = name;
  }

  /**
   * Return the name of the source
   * @return the name of the source
   */
  public String getName () {
    return name;
  }

  /**
   * Returns <code>true</code> if this source has the same name as
   * the other source, else <code>false</code>
   * @param obj the other source
   */
  public boolean equals (Object obj) {
    LSPSource other = (LSPSource) obj;
    return name.equals (other.getName());
  }

  public int hashCode () {
    return name.hashCode();
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.printEntireElement("source", name);
    writer.exitNamespace();
  }
}