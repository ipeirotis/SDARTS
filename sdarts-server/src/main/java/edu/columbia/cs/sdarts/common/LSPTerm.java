
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Represents a STARTS "term", as described by the specification.
 * A term is a major data object used throughout SDARTS, in
 * {@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter},
 * {@link edu.columbia.cs.sdarts.common.LSPRanking LSPRanking},
 * and elsewhere. A term can consist of a field
 * ({@link edu.columbia.cs.sdarts.common.LSPField LSPField}), zero or more
 * modifiers ({@link edu.columbia.cs.sdarts.common.LSPModifier LSPModifier}), and
 * a value {@link edu.columbia.cs.sdarts.common.LSPValue LSPValue}). Or, it can be
 * zero or more modifiers plus one value. So, in plain English, that
 * means a term can also be just one value.
 * <p>
 * A term can have a weight associated with it, but this really only has
 * meaning if the term is being used inside an <code>LSPRanking</code>
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPTerm extends LSPObject {
    // ------------ FIELDS -------------
    private LSPField      field;
    private LSPModifier[] modifiers;
    private LSPValue      value;
    private double        weight;
    private boolean       weighted;


    // ------------ METHODS ------------
    /**
     * Creates an empty term. Note that this is technically an illegal state
     * for the term to be in, so it should be populated as soon as possible
     * using the various <code>set . . .()</code> methods.
     */
    public LSPTerm () {}

    // package-protected, used by some of the builder classes
    LSPTerm (double weight) {
      this();
      if (weight < 0 || weight > 1.0) {
        throw new IllegalArgumentException ("illegal weight");
      }
      this.weight = weight;
      this.weighted = true;
    }

    /**
     * Construct a new <code>LSPTerm</code>. The <code>value</code>
     * parameter cannot be <code>null</code>, but the others can
     * @param field the field of the term
     * @param modifiers the modifiers of the term
     * @param value the value of the term
     */
    public LSPTerm (LSPField field, LSPModifier[] modifiers, String value) {
      this();
      if (value == null) {
        throw new IllegalArgumentException ("term must have a value");
      }
      this.field = field;
      this.modifiers = modifiers;
      this.value = new LSPValue (value);
    }

    /**
     * Construct a new weighted <code>LSPTerm</code>. The <code>value</code>
     * parameter cannot be <code>null</code>, but the others can
     * @param field the field of the term
     * @param modifiers the modifiers of the term
     * @param value the value of the term
     * @param weight the weight of the term
     */
    public LSPTerm (LSPField field, LSPModifier[] modifiers,
                    String value, double weight) {
      this (weight);
      if (value == null) {
        throw new IllegalArgumentException ("term must have a value");
      }
      this.field = field;
      this.modifiers = modifiers;
      this.value = new LSPValue (value);
      this.weighted = true;
    }

    /**
     * Return the field of the term
     * @return the field of the term
     */
    public LSPField getField () {
      return field;
    }

    /**
     * Set the field of the term
     * @param field the field of the term
     */
    public void setField (LSPField field) {
      this.field = field;
    }

    /**
     * Return the modifiers of the term
     * @return the modifiers of the term
     */
    public LSPModifier[] getModifiers() {
      return modifiers;
    }

    /**
     * Set the modifiers of the term
     * @param modifiers the modifiers of the term
     */
    public void setModifiers (LSPModifier[] modifiers) {
      this.modifiers = modifiers;
    }

    /**
     * Return the value of the term
     * @return the value of the term
     */
    public LSPValue getValue () {
      return value;
    }

    /**
     * Set the value of the term. Cannot be <code>null</code>
     * @param value the value of the term
     */
    public void setValue (String value) {
      if (value == null) {
        throw new IllegalArgumentException ("term must have value");
      }
      this.value = new LSPValue (value);
    }

    /**
     * Set the value of the term. Cannot be <code>null</code>
     * @param value the value of the term
     */
    public void setValue (LSPValue value) {
      this.value = value;
    }

    /**
     * Return the weight of the term
     * @return the weight of the term
     */
    public double getWeight () {
      return weight;
    }


    // -------- OBJECT METHODS --------
    /**
     * Two <code>LSPTerms</code> are equal if they have same
     * <code>LSPField</code>, <code>LSPModifiers</code>, and
     * <code>LSPValue</code>.
     * @param obj the other <code>LSPTerm</code> to compare to
     * @return whether the terms are equal
     */
    public boolean equals (Object obj) {
	  LSPTerm other = (LSPTerm) obj;
	  LSPField otherField = other.getField();
      LSPModifier[] otherMods = other.getModifiers();
      LSPValue otherValue = other.getValue();

      if ( (field == null && otherField != null) ||
           (field != null && otherField == null) ||
           (field != null && otherField != null && !field.equals(otherField))){
             return false;
      }
      if ( (modifiers == null && otherMods != null) ||
           (modifiers != null && otherMods == null) ) {
             return false;
      }
      if ( modifiers != null && otherMods != null) {
        int len = otherMods.length;
        if (len != modifiers.length) {
          return false;
        }
        for (int i = 0 ; i < len ; i++) {
          if (!modifiers[i].equals (otherMods[i])) {
            return false;
          }
        }
      }
      if (!value.equals(otherValue)) {
        return false;
      }

      return true;
    }

    public int hashCode () {
      int hashCode = value.hashCode();
      if (field != null) {
        hashCode += field.hashCode();
      }
      if (modifiers != null) {
        int len = modifiers.length;
        for (int i = 0 ; i < len ; i++) {
          hashCode += modifiers[i].hashCode();
        }
      }
      return hashCode;
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      if (weighted) {
        writer.printStartElement ("term", true);
        writer.printAttribute ("weight", weight);
        writer.printStartElementClose();
      }
      else {
        writer.printStartElement ("term");
      }

      if (field != null) {
        writer.indent();
        field.toXML(writer);
        writer.unindent();
      }
      if (modifiers != null) {
        int len = modifiers.length;
        writer.indent();
        for (int i = 0 ; i < len ; i++) {
          LSPModifier m = modifiers[i];
          m.toXML(writer);
        }
        writer.unindent();
      }
      writer.indent();
      value.toXML(writer);
      writer.unindent();
      writer.printEndElement ("term");
      writer.exitNamespace();
    }
}
