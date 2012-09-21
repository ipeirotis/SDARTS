

package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;



/**
 * Represents a "field" element as described in the STARTS 1.0
 * specification. A field simply represents the title of a possible
 * search field, as per the <code>basic1</code> set of field names.
 * See the
 * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
 *  class for legal field names and
 * types.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPField extends LSPObject {
    // ------------ FIELDS -------------
//    private int    code;
    private String name;
    private String typeSet;

    // ------------ METHODS ------------
    // -------- ABSTRACT METHODS --------
    /**
     * Create a field of type "basic1", using a name from
     * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
     * @param name the name of the field
     */
    public LSPField (String name) {
      this ("basic1", name);
    }

    /**
     * Create a field of some other non-basic1 type. This does not
     * really happen yet, and may never, so ignore this method.
     * @param typeSet the type of field this is (i.e. "basic1", etc.)
     * @param name the name of the field
     */
    public LSPField (String typeSet, String name) {
      this.name = name;
      this.typeSet = typeSet;
//      this.code = FieldNames.nameToCode(name);
    }

//    /**
//     * Creates a new instance, using a code from
//     * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
//     * @param code the field code
//     */
//    public LSPField (int code) {
//      if (!FieldNames.isLegal (code)) {
//        throw new IllegalArgumentException ("illegal field code: " + code);
//      }
//      typeSet = "basic1";
//      name = FieldNames.codeToName(code);
//      this.code = code;
//    }

    /**
     * @return the type set that the field names come from. This
     * is always <code>basic1</code>.
     */
    public String getTypeSet () {
      return typeSet;
    }

//    /**
//     * Return the code for the field, as specified in
//     * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
//     * @return the code for the field, as specified in
//     * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
//     */
//    public int getCode () {
//      return code;
//    }

    /**
     * Return the name of the field, as specified in
     * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
     * @return the name of the field, as specified in
     * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
     */
    public String getName () {
      return name;
    }

    /**
     * Returns <code>true</code> if two <code>LSPFields</code> are
     * of the same name/type
     * @return <code>true</code> if two <code>LSPFields</code> are
     * of the same name/type, else <code>false</code>
     */
    public boolean equals (Object obj) {
      if (obj == null) {
        return false;
      }
      LSPField other = (LSPField) obj;
      return (name.equals(other.getName()));
    }

    public int hashCode () {
      return name.hashCode();
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("field", true);
      writer.printAttribute ("type-set", typeSet);
      writer.printAttribute ("name", name);
      writer.printEmptyElementClose();
      writer.exitNamespace();
    }
}
