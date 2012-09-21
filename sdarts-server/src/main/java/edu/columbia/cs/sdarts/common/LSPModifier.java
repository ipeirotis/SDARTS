
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Represents a "modifier" as defined in the STARTS 1.0 specification.
 * Modifiers are used inside of terms
 * ({@link edu.columbia.cs.sdarts.common.LSPTerm LSPTerm}), and also
 * in some metadata. See the
 * {@link edu.columbia.cs.sdarts.common.ModifierNames ModifierNames} class for legal
 * names and codes.
 * <p>
 * Note that these modifiers come from the <code>basic1</code> set.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPModifier extends LSPObject {
  private int code;
  private String name;
  private String typeSet;

  /**
    * Create a modifier of type "basic1", using a name from
    * {@link edu.columbia.cs.sdarts.common.ModifierNames ModifierNames}
    * @param name the name of the modifier
    */
  public LSPModifier (String name) {
    this ("basic1", name);
  }

  /**
    * Create a modifier of some other non-basic1 type. This does not
    * really happen yet, and may never, so ignore this method.
    * @param typeSet the type of modifier this is (i.e. "basic1", etc.)
    * @param name the name of the modifier
    */
  public LSPModifier (String typeSet, String name) {
    if (!ModifierNames.isLegal (name)) {
      throw new IllegalArgumentException ("Illegal modifier name: " + name);
    }

    this.typeSet    = typeSet;
    this.name       = name;
    code = ModifierNames.nameToCode(name);
  }

  /**
    * Creates a new instance, using a code from
    * {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames}
    * @param code the field code
    */
  public LSPModifier (int code) {
    if (!ModifierNames.isLegal (code)) {
      throw new IllegalArgumentException ("Illegal modifier code: " + code);
    }

    typeSet = "basic1";
    this.name = ModifierNames.codeToName(code);
    this.code = code;
  }

 /**
  * Return the code for the modifier, as specified in
  * {@link edu.columbia.cs.sdarts.common.ModifierNames ModifierNames}
  * @return the code for the modifier, as specified in
  * {@link edu.columbia.cs.sdarts.common.ModifierNames ModifierNames}
  */
  public int getCode () {
    return code;
  }

  /**
    * Return the name of the modifier, as specified in
    * {@link edu.columbia.cs.sdarts.common.ModifierNames ModifierNames}
    * @return the name of the modifier, as specified in
    * {@link edu.columbia.cs.sdarts.common.ModifierNames ModifierNames}
    */
  public String getName () {
    return name;
  }

  /**
   * @return the typeset to which this modifier belongs. Normally,
   * this is always <code>basic1</code>.
   */
  public String getTypeSet () {
    return typeSet;
  }

  /**
    * Returns <code>true</code> if two <code>LSPModifiers</code> are
    * of the same name/type
    * @return <code>true</code> if two <code>LSPModifiers</code> are
    * of the same name/type, else <code>false</code>
    */
  public boolean equals (Object obj) {
    if (obj == null) {
      return false;
    }
    LSPModifier other = (LSPModifier) obj;
    return (name.equals(other.getName()));
  }

  public int hashCode () {
    return name.hashCode();
  }

  public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("modifier", true);
      writer.printAttribute ("type-set", typeSet);
      writer.printAttribute ("name", name);
      writer.printEmptyElementClose();
      writer.exitNamespace();
  }
}