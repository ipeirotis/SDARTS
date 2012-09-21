
package edu.columbia.cs.sdarts.backend.doc.lucene;

import edu.columbia.cs.sdarts.common.FieldNames;
import edu.columbia.cs.sdarts.common.ModifierNames;

/**
 * A "constants container" for the package. Many of these are used for
 * constructing the meta-attributes of a Lucene-wrapped collection, and
 * indicate the current capabilities and limitations of the Lucene engine,
 * particularly in terms of fields supported, modifiers supported, and
 * supported combinations of the two. Should Lucene's capabilities ever
 * change, this file may need to be edited. Keeping them here minimizes
 * the impact of change.
 * <p>
 * Right now, the limitations are as follows:
 * <ul>
 * <li>All STARTS fields can be supported
 * <li>Only the lt, gt, eq, ne modifiers are supported
 * <li>Modifiers can only be used with the <code>DATE_LAST_MODIFIED</code>
 * field.
 * </ul>
 * See the design document for more information on what Lucene can and
 * cannot do.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LuceneConstants {
  /** The default weight for a Lucene term */
  public static final double DEFAULT_WEIGHT = 1.5;

  /** Used for storing the document size in KB inside the Lucene index */
  public static final String LUCENE_DOC_SIZE  = "LUCENE_DOC_SIZE";

  /** Used for storing the document count inside the Lucene index */
  public static final String LUCENE_DOC_COUNT = "LUCENE_DOC_COUNT";

  /** The modifiers supported by Lucene */
  public static final String[] MODIFIERS_SUPPORTED =
    new String[] {ModifierNames.LT, ModifierNames.GT,
                  ModifierNames.EQ, ModifierNames.NE};

  /** The field/modifier combinations supported by Lucene */
  public static final String[] FIELD_MODIFIER_COMBINATIONS =
    new String[] { FieldNames.DATE_LAST_MODIFIED,
                   ModifierNames.LT,
                   FieldNames.DATE_LAST_MODIFIED,
                   ModifierNames.GT,
                   FieldNames.DATE_LAST_MODIFIED,
                   ModifierNames.EQ,
                   FieldNames.DATE_LAST_MODIFIED,
                   ModifierNames.NE};

  /** The score range for Lucene results */
  public static final double[] SCORE_RANGE = new double[] {0.0, 1.0};
}