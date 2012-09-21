
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * A representation of the "sort-by-field" header of STARTS 1.0
 * This header is found inside an {@link edu.columbia.cs.sdarts.common.LSPQuery LSPQuery},
 * and tells the {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} what
 * fields should be used for sorting the results. A <code>SortByField</code>
 * really only consists of the {@link edu.columbia.cs.sdarts.common.LSPField LSPField} to
 * sort on - or, alternatively, the document score - and whether this
 * sorting be in ascending or descending order. If <code>LSPSortByField</code>
 * is for the document score, the order can only be descending. A
 * <code>BackEndLSP</code> receiving multiple <code>LSPSortByFields</code>
 * should first sort on the first field, then the second, etc.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */

public class LSPSortByField extends LSPObject {
  /** Constant for indicating that the sort is ascending */
  public static final int ASCENDING  = 0;

  /** Constant for indicating that the sort is descending */
  public static final int DESCENDING = 1;

  private LSPField field;
  private boolean  isScore;
  private String   ascendingDescendingString = "d";
  private int      ascendingDescending = DESCENDING;

  /**
   * Create an <code>LSPSortByField</code> indicating that the sort
   * be on document score, in descending order.
   */
  public LSPSortByField () {
    this.isScore = true;
    ascendingDescending = DESCENDING;
  }

  /**
   * Create an <code>LSPSortByField</code> on a given field.
   * @param field the field
   * @param ascendingDesecending must be <code>LSPSortByField.ASCENDING</code>
   * or <code>LSPSortByField.DESCENDING</code>
   */
  public LSPSortByField (LSPField field, int ascendingDescending) {
    if (ascendingDescending != ASCENDING &&
        ascendingDescending != DESCENDING) {
          throw new IllegalArgumentException ("invalid ascendingdescending");
    }
    this.ascendingDescending = ascendingDescending;
    this.field = field;
  }

  // package protected, used in the builders
  LSPSortByField (String ascendingDescendingString) {
    this.isScore = false;
    this.ascendingDescendingString = ascendingDescendingString;
    if (ascendingDescendingString.equals ("a")) {
      ascendingDescending = ASCENDING;
    }
    else {
      ascendingDescending = DESCENDING;
    }
  }

  /**
   * Return the field to sort on
   * @return the field to sort on
   */
  public LSPField getField () {
    return field;
  }

  /**
   * Set the field to sort on. Cannot be called if the <code>LSPSortByField</code>
   * has already been declared as a score field.
   * @param field the field
   */
  public void setField (LSPField field) {
    if (isScore) {
      throw new IllegalArgumentException ("cannot have a field, is score");
    }
    this.field = field;
  }

  /**
   * Whether this <code>LSPSortByField</code> indicates to sort on
   * document score
   * @return whether this <code>LSPSortByField</code> indicates to sort on
   * document score
   */
  public boolean isScore () {
    return isScore;
  }

  /**
   * Return the sort order, either <code>LSPSortByField.ASCENDING</code>
   * or <code>LSPSortByField.DESCENDING</code>
   * @return the sort order, either <code>LSPSortByField.ASCENDING</code>
   * or <code>LSPSortByField.DESCENDING</code>
   */
  public int getAscendingDescending () {
    return ascendingDescending;
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    if (!isScore) {
      writer.printStartElement ("sort-by-field", true);
      writer.printAttribute ("ascending-descending", ascendingDescendingString);
      writer.printStartElementClose ();
      field.toXML (writer);
      writer.printEndElement ("sort-by-field");
    }
    else {
      writer.printEmptyElement ("score");
    }
    writer.exitNamespace();
  }
}
