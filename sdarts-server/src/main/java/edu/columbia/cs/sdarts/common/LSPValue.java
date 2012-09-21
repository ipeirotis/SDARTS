

package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Wraps some kind of value stored in a STARTS header, for example
 * inside the STARTS "term" ({@link edu.columbia.cs.sdarts.common.LSPTerm LSPTerm}).
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPValue extends LSPObject {
  private String value;

  /**
   * Initialize with stored value
   * @param value the value
   */
  public LSPValue (String value) {
    this.value = value;
  }

  public boolean equals (Object obj) {
    if (obj == null) {
      return false;
    }
    LSPValue other = (LSPValue) obj;
    return (other.getValue().equals (value));
  }

  public int hashCode () {
    return value.hashCode();
  }

  /**
   * Sets the stored value
   * @param value the value
   */
  public void setValue (String value) {
    this.value = value;
  }

  /**
   * Gets the stored value
   * @return the value
   */
  public String getValue () {
    return value;
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.printEntireElement("value", value);
    writer.exitNamespace();
  }

  public String toString () {
    return value;
  }
}
