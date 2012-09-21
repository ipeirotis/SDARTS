
package edu.columbia.cs.sdarts.common;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * The base class for nearly all the object headers in SDARTS.
 * Most of these headers begin with the name <code>LSP</code>, and
 * most of those are objectified pieces of the STARTS XML protocol.
 * Some are objectified versions of XML configuration descriptors
 * for the SDARTS server. What all have in common is that they
 * can be represented in XML, and this base class defines abstract
 * and concrete methods for doing this.
 * <p>
 * In principle, an <code>LSPObject</code> is cloneable, though
 * not all its subclasses are meant to be cloned.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public abstract class LSPObject {
    /**
     * Defines how an <code>LSPObject</code> can be represented
     * in XML. The implementor of this method should write all XML to the
     * <code>XMLWriter</code> that has been passed in.
     * @param writer the <code>XMLWriter</code> that will print out this
     * object's XML representation.
     */
    public abstract void toXML (XMLWriter writer) throws IOException;

    /**
     * Wraps a new <code>XMLWriter</code> around the <code>Writer</code>
     * passed to this method, and then writes the XML to it.
     * @param writer the <code>Writer</code>
     */
    public final void toXML (Writer writer) throws IOException {
      XMLWriter xw = new XMLWriter (writer, null);
      toXML (xw);
    }

    /**
     * This method will call upon the abstract method to generate the
     * XML, and then return it as a giant <code>String</code> containing
     * the XML representation
     */
    public String toXML () throws IOException {
      StringWriter sw = new StringWriter();
      toXML (sw);
      return sw.toString();
    }
}
