

package edu.columbia.cs.sdarts.common;

/**
 * A "constants container" for all legal STARTS modifier names and codes.
 * Whenever referring to STARTS modifiers by name or code, <b>always use
 * these constants</b>. This minimizes the impact of change, should
 * a modifier name ever change.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class ModifierNames {

  // NOTE TO DEVELOPERS - KEEP THE STRING NAMES, THE INT CODES, AND
  // THE STRING ARRAY IN THE SAME ORDER, OR ELSE THE CODETONAME()
  // AND NAMETOCODE() METHODS WILL BREAK DOWN!


  public static final String LT                = "lt";
  public static final String LTE               = "lte";
  public static final String EQ                = "eq";
  public static final String GTE               = "gte";
  public static final String GT                = "gt";
  public static final String NE                = "ne";
  public static final String PHONETIC          = "phonetic";
  public static final String STEM              = "stem";
  public static final String THESAURUS         = "thesaurus";
  public static final String LEFT_TRUNCATION   = "left-truncation";
  public static final String RIGHT_TRUNCATION  = "right-truncation";
  public static final String CASE_SENSITIVE    = "case-sensitive";

  public static final int LT_CODE                = 0;
  public static final int LTE_CODE               = 1;
  public static final int EQ_CODE                = 2;
  public static final int GTE_CODE               = 3;
  public static final int GT_CODE                = 4;
  public static final int NE_CODE                = 5;
  public static final int PHONETIC_CODE          = 6;
  public static final int STEM_CODE              = 7;
  public static final int THESAURUS_CODE         = 8;
  public static final int LEFT_TRUNCATION_CODE   = 9;
  public static final int RIGHT_TRUNCATION_CODE  = 10;
  public static final int CASE_SENSITIVE_CODE    = 11;


  public static final String[] MODIFIER_NAMES =
  {LT, LTE, EQ, GTE, GT, NE, PHONETIC, STEM, THESAURUS,
    LEFT_TRUNCATION, RIGHT_TRUNCATION, CASE_SENSITIVE};

  /**
   * Convert a modifier name to its code
   * @name the modifier name
   * @return the modifier code
   */
    public static final int nameToCode (String name) {
    int len = MODIFIER_NAMES.length;
    for (int i = 0 ; i < len ; i++) {
      if (MODIFIER_NAMES[i].equals (name)) {
        return i;
      }
    }
    throw new IllegalArgumentException ("name: " + name);
  }

  /**
   * Convert a modifier code to its name
   * @code the modifier code
   * @return the modifier name
   */
  public static final String codeToName (int code) {
    return MODIFIER_NAMES[code];
  }

  public static boolean isLegal (String modifierName) {
    int len = MODIFIER_NAMES.length;
    for (int i = 0 ; i < len ; i++) {
      if (MODIFIER_NAMES[i].equals (modifierName)) {
        return true;
      }
    }

    return false;
  }

  public static boolean isLegal (int code) {
    return (code >=0 && code < MODIFIER_NAMES.length);
  }


  private ModifierNames () {}
}